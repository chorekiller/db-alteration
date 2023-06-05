package net.isammoc.alterations

import net.isammoc.alterations.Alterations._
import net.isammoc.alterations.db.DbAlterationTable
import net.isammoc.alterations.file.FileAlterationReader
import net.isammoc.alterations.util.AlterationException

import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object Alterations {
  final case class Config(url: String,
                          schemaName: String,
                          tableName: String,
                          path: Path,
                          user: String,
                          password: String
  )
}
class Alterations(config: Config) {
  private def inner[A](block: (DbAlterationTable, Connection) => Try[A]): Try[A] = {
    implicit val conn: Connection = DriverManager.getConnection(config.url, config.user, config.password)
    val dbAlterationTable         = DbAlterationTable(config.schemaName, config.tableName)

    dbAlterationTable.checkIssues match {
      case Success(None)          => block(dbAlterationTable, conn)
      case Success(Some(problem)) => Failure(new AlterationException(problem))
      case Failure(e)             => Failure(e)
    }
  }

  def getVersion(): Try[Int] = inner { (dbAlterationTable, connection) =>
    dbAlterationTable.getVersion()(connection)
  }

  def needAlterations(): Try[Boolean] = inner { (dbAlterationTable, connection) =>
    val reader = FileAlterationReader(config.path)
    for {
      (declines, raises) <- dbAlterationTable.diff(reader.getAlterations())(connection)
    } yield {
      declines.nonEmpty || raises.nonEmpty
    }
  }

  def applyAlterations(): Try[Unit] = inner { (dbAlterationTable, connection) =>
    val reader = FileAlterationReader(config.path)
    for {
      // TODO declines
      (declines, raises) <- dbAlterationTable.diff(reader.getAlterations())(connection)
      _ <- declines.foldLeft(Try(())) { case (before, decline) =>
             before.flatMap(_ => dbAlterationTable.applyDecline(decline)(connection))
           }
      _ <- raises.foldLeft(Try(())) { case (before, raise) =>
             before.flatMap(_ => dbAlterationTable.applyRaise(raise)(connection))
           }
    } yield ()
  }
}
