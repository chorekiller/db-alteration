package net.isammoc.alterations.db

import net.isammoc.alterations.model.Alteration

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import scala.annotation.tailrec
import scala.collection.immutable.ArraySeq
import scala.util.Try
import scala.util.Using

case class DbAlterationTable(schemaName: String, tableName: String) {
  private def replace(sql: String): String =
    sql.replace("#{schema}", schemaName).replace("#{alteration_table}", tableName)

  def execute(statement: String)(implicit c: Connection): Try[Boolean] = Using(c.createStatement) { s =>
    s.execute(statement)
  }

  def executeQuery[T](sql: String)(block: ResultSet => T)(implicit c: Connection): Try[T] = Using(c.createStatement) {
    s =>
      Using(s.executeQuery(sql)) { rs =>
        block(rs)
      }
  }.flatten

  def prepareAndExecute(statement: String)(block: PreparedStatement => Unit)(implicit c: Connection): Try[Boolean] =
    Using(c.prepareStatement(statement)) { ps =>
      block(ps)
      ps.execute
    }

  def createTable(implicit c: Connection): Try[Boolean] = execute(
    replace("""CREATE TABLE #{schema}#{alteration_table} (
              |  id             INT          NOT NULL PRIMARY KEY,
              |  hash           VARCHAR(255) NOT NULL,
              |  applied_at     TIMESTAMP    NOT NULL,
              |  raise_script   TEXT,
              |  decline_script TEXT,
              |  state          VARCHAR(255),
              |  last_issue     TEXT
              |);
              |""".stripMargin)
  )

  def diff(alterations: List[Alteration])(implicit c: Connection): Try[(List[Alteration], List[Alteration])] = {
    executeQuery(replace("""SELECT id, hash, decline_script
                           |FROM #{schema}#{alteration_table}
                           |ORDER BY id""".stripMargin)) { rs =>
      @tailrec
      def loop(toUp: List[Alteration]): (List[Alteration], List[Alteration]) =
        if (rs.next) {
          toUp match {
            case h :: t if h.version == rs.getInt(1) && h.hash == rs.getString(2) =>
              loop(t)
            case _ =>
              @tailrec
              def innerLoop(toDown: List[Alteration]): (List[Alteration], List[Alteration]) = {
                val down = Alteration(rs.getInt(1), "", rs.getString(3))
                if (rs.next) {
                  innerLoop(down :: toDown)
                } else {
                  (down :: toDown, toUp)
                }
              }
              innerLoop(Nil)
          }
        } else {
          (Nil, toUp)
        }
      loop(alterations)
    }
  }

  def checkIssues(implicit c: Connection): Try[Option[String]] =
    executeQuery(replace("""SELECT id, hash, raise_script, decline_script, state, last_issue
                           |FROM #{schema}#{alteration_table}
                           |WHERE state LIKE 'applying_%'
                           |""".stripMargin)) { rs =>
      if (rs.next) {
        val (direction, script) = rs.getString(5) match {
          case "applying_raise" => ("Ups", rs.getString(3))
          case _                => ("Downs", rs.getString(4))
        }
        Some(s"""Inconsistent state
                |-- !${direction} REV: ${rs.getInt(1)} - ${rs.getString(2)}
                |$script
                |
                |Problem:
                |${rs.getString(6)}
                |""".stripMargin)
      } else {
        None
      }
    }.orElse(createTable.map(_ => None))

  def getVersion()(implicit c: Connection): Try[Int] = {
    executeQuery(replace("""SELECT max(id)
                           |FROM #{schema}#{alteration_table}
                           |""".stripMargin)) { rs =>
      rs.next()
      rs.getInt(1)
    }
  }

  def toStatements(sql: String): Seq[String] =
    // Regex matches on semicolons that neither precede nor follow other semicolons
    ArraySeq.unsafeWrapArray(sql.split("(?<!;);(?!;)").map(_.trim.replace(";;", ";")).filter(_ != ""))

  def applyRaise(alteration: Alteration)(implicit c: Connection): Try[Unit] = for {
    _ <- prepareAndExecute(replace("""
                                     |INSERT INTO #{schema}#{alteration_table}
                                     |(id, hash, applied_at, raise_script, decline_script, state, last_issue)
                                     |VALUES (?, ?, ?, ?, ?, ?, ?)
                                     |""".stripMargin)) { ps =>
           ps.setInt(1, alteration.version)
           ps.setString(2, alteration.hash)
           ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()))
           ps.setString(4, alteration.sqlRaise)
           ps.setString(5, alteration.sqlDecline)
           ps.setString(6, "applying_raise")
           ps.setString(7, "")
         }
    _ <- toStatements(alteration.sqlRaise)
           .foldLeft(Try(())) { case (before, statement) =>
             before.flatMap(_ => execute(statement).map(_ => ()))
           }
           .recoverWith { case e =>
             prepareAndExecute(replace("""UPDATE #{schema}#{alteration_table}
                                         |SET last_issue = ?
                                         |WHERE id = ?
                                         |""".stripMargin)) { ps =>
               ps.setString(1, e.getMessage)
               ps.setInt(2, alteration.version)
             }.map(_ => ())
           }
    _ <- prepareAndExecute(replace("""UPDATE #{schema}#{alteration_table}
                                     |SET state = 'applied'
                                     |WHERE id = ?""".stripMargin)) { ps =>
           ps.setInt(1, alteration.version)
         }
  } yield ()

  def applyDecline(decline: Alteration)(implicit c: Connection): Try[Unit] = for {
    _ <- prepareAndExecute(replace("""UPDATE #{schema}#{alteration_table}
                                     |SET state = 'applying_decline'
                                     |WHERE id = ?""".stripMargin)) { ps =>
           ps.setInt(1, decline.version)
         }
    _ <- toStatements(decline.sqlDecline)
           .foldLeft(Try(())) { case (before, statement) =>
             before.flatMap(_ => execute(statement).map(_ => ()))
           }
           .recoverWith { case e =>
             prepareAndExecute(replace("""UPDATE #{schema}#{alteration_table}
                                         |SET last_issue = ?
                                         |WHERE id = ?""".stripMargin)) { ps =>
               ps.setString(1, e.getMessage)
               ps.setInt(2, decline.version)
             }.map(_ => ())
           }
    _ <- prepareAndExecute(replace("""DELETE FROM #{schema}#{alteration_table}
                                     |WHERE id = ?""".stripMargin)) { ps =>
           ps.setInt(1, decline.version)
         }
  } yield ()
}
