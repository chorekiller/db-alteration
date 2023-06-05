package net.isammoc.alterations.file

import net.isammoc.alterations.model.Alteration
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should

class AlterationParserSpec extends AnyFreeSpec with should.Matchers {

  "AlterationParser" - {
    import net.isammoc.alterations.file.AlterationParser._
    "parseLines" - {
      "empty alteration" in {
        parseLines(0, Nil, AllScripts) shouldBe Alteration(0, "", "")
      }

      "simple alteration" in {
        val lineUp   = "First line Ups"
        val lineDown = "First line Downs"
        val script =
          s"""
             |-- !Ups hello world
             |$lineUp
             |-- !Downs hello world
             |$lineDown
             |""".stripMargin
        parseScript(0, script, AllScripts) shouldBe Alteration(0, lineUp, lineDown)
      }

      "multiline alteration" in {
        val linesUp =
          """First line Ups
            |Second line Ups""".stripMargin
        val linesDown =
          """First line Downs
            |Second line Downs""".stripMargin
        val script =
          s"""
             |noise
             |-- !Ups hello world
             |$linesUp
             |-- !Downs hello world
             |$linesDown
             |""".stripMargin

        parseLines(0, script.split("\n").toList, AllScripts) shouldBe Alteration(0, linesUp, linesDown)
      }

      "multi ups and down" in {
        val firstUp    = "lineUp1"
        val secondUp   = "lineUp2"
        val firstDown  = "lineDown1"
        val secondDown = "lineDown2"

        val script =
          s"""
             |noise before
             |-- !Ups first
             |$firstUp
             |-- !Downs first
             |$firstDown
             |-- !Ups second
             |$secondUp
             |-- !Downs second
             |$secondDown
             |""".stripMargin

        parseScript(0, script, AllScripts) shouldBe Alteration(0,
                                                               firstUp + "\n" + secondUp,
                                                               firstDown + "\n" + secondDown
        )
      }
    }
  }
}
