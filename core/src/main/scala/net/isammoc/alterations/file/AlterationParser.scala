package net.isammoc.alterations.file

import net.isammoc.alterations.model.Alteration

import scala.annotation.tailrec

object AlterationParser {
  object AlterationContext {
    object RaiseContext   extends AlterationContext
    object DeclineContext extends AlterationContext

    private val RaiseMarker   = "^(?:#|--)\\s+!Ups(.*)$".r
    private val DeclineMarker = "^(?:#|--)\\s+!Downs(.*)$".r

    def unapply(str: String): Option[(AlterationContext, String)] = str match {
      case RaiseMarker(other)   => Some((RaiseContext, other))
      case DeclineMarker(other) => Some((DeclineContext, other))
      case _                    => None
    }
  }

  sealed trait AlterationContext

  val AllScripts: String => Boolean = _ => true

  def parseScript(version: Int, script: String, filterMode: String => Boolean): Alteration =
    parseLines(version, script.split("\n").toList, filterMode)

  def parseLines(version: Int, lines: List[String], filterMode: String => Boolean): Alteration = {
    @tailrec
    def loop(toVisit: List[String],
             currentContext: Option[AlterationContext],
             reverseUps: List[String],
             reverseDowns: List[String]
    ): Alteration = {
      (toVisit, currentContext) match {
        case (Nil, _) => Alteration(version, reverseUps.reverse.mkString("\n"), reverseDowns.reverse.mkString("\n"))
        case (AlterationContext(ctx, markers) :: tail, _) =>
          if (filterMode(markers)) loop(tail, Some(ctx), reverseUps, reverseDowns)
          else loop(tail, None, reverseUps, reverseDowns)
        case (head :: tail, Some(AlterationContext.RaiseContext)) =>
          loop(tail, currentContext, head :: reverseUps, reverseDowns)
        case (head :: tail, Some(AlterationContext.DeclineContext)) =>
          loop(tail, currentContext, reverseUps, head :: reverseDowns)
        case (_ :: tail, _) => loop(tail, currentContext, reverseUps, reverseDowns)
      }
    }

    loop(lines, None, Nil, Nil)
  }
}
