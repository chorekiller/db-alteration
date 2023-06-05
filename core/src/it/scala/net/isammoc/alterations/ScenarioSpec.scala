package net.isammoc.alterations

import net.isammoc.alterations.db.DbAlterationTable
import net.isammoc.alterations.file.FileAlterationReader

import java.sql.{Connection, DriverManager}
import scala.util.Success

class ScenarioSpec extends DbAlterationSpec {
  "scenario" - {

    "create alteration table if not exist" in {
      withConfig { config =>
        implicit val conn: Connection = DriverManager.getConnection(config.url, config.user, config.password)
        val dbAlterationTable         = DbAlterationTable("", "alteration")
        dbAlterationTable.checkIssues shouldBe Success(None)
      }
    }

    "apply all ups" in {
      withConfig { config =>
        implicit val conn: Connection = DriverManager.getConnection(config.url, config.user, config.password)
        val dbAlterationTable         = DbAlterationTable("", "alteration")
        val alterations               = FileAlterationReader(config.path).getAlterations()
        dbAlterationTable.diff(alterations) shouldBe Success((Nil, Nil))
      }
    }

    "One down" in {
      withConfig { config =>
        copyResources(config.path, "one-down/1.sql", "one-down/2.sql")
        val underTest = new Alterations(config)
        underTest.getVersion() shouldBe Success(0)
        underTest.needAlterations() shouldBe Success(true)
        underTest.applyAlterations() shouldBe Success(())
        underTest.needAlterations() shouldBe Success(false)
        underTest.getVersion() shouldBe Success(2)
        config.path.resolve("2.sql").toFile.delete()
        underTest.needAlterations() shouldBe Success(true)
        underTest.applyAlterations() shouldBe Success(())
        underTest.needAlterations() shouldBe Success(false)
        underTest.getVersion() shouldBe Success(1)
      }
    }
  }
}
