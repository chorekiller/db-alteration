package net.isammoc.alterations

import scala.util.Success

class AlterationsSpec extends DbAlterationSpec {
  "Alterations" - {
    "getVersion" in {
      withConfig { config =>
        val underTest = new Alterations(config)
        underTest.getVersion() shouldBe Success(0)
      }
    }
    "needAlteration" in {
      withConfig { config =>
        val underTest = new Alterations(config)
        copyResources(config.path, "/alteration/1.sql")
        underTest.needAlterations() shouldBe Success(true)
      }
    }
    "apply alterations" in {
      withConfig { config =>
        val underTest = new Alterations(config)
        copyResources(config.path, "/alteration/1.sql")
        underTest.applyAlterations() shouldBe Success(())
        underTest.getVersion() shouldBe Success(1)
      }
    }
  }
}
