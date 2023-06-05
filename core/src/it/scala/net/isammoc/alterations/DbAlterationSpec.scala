package net.isammoc.alterations

import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should
import org.testcontainers.utility.DockerImageName

import java.nio.file.{Files, Path}
import scala.util.Using

class DbAlterationSpec extends AnyFreeSpec with should.Matchers with TestContainerForAll {
  def withConfig[A](block: Alterations.Config => A): A = {
    withContainers { (pgContainer: PostgreSQLContainer) =>
      withTempDir { tmpDir =>
        block(
          Alterations.Config(
            url = pgContainer.jdbcUrl,
            user = pgContainer.username,
            password = pgContainer.password,
            schemaName = "",
            tableName = "alterations",
            path = tmpDir
          )
        )
      }
    }
  }

  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def(
    DockerImageName.parse("postgres:15.1"),
    "db-alteration",
    "postgres",
    "secret"
  )

  def withTempDir[A](block: Path => A): A = {
    val tempDir = Files.createTempDirectory("db-alteration-test")
    tempDir.toFile.deleteOnExit()
    try {
      block(tempDir)
    } finally {
      tempDir.toFile.delete(): Unit
    }
  }

  def copyResources(dest: Path, names: String*): Unit = names.foreach { name =>
    Using(this.getClass.getResourceAsStream(name)) { inputStream =>
      Files.copy(inputStream, dest.resolve(Path.of(name).getFileName))
    }
  }
}
