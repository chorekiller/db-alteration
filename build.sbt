import Dependencies.*

inThisBuild(
  List(
    scalaVersion               := "2.13.11",
    version                    := "0.1.0-SNAPSHOT",
    organization               := "net.isammoc.chorekiller",
    organizationName           := "ChoreKiller",
    semanticdbEnabled          := true, // enable SemanticDB
    semanticdbVersion          := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)

def baseProject(project: Project): Project = project.settings(
  Test / scalacOptions := (Test / scalacOptions).value.filterNot(_ == "-Wnonunit-statement")
)

lazy val core = (project in file("core"))
  .configure(baseProject)
  .settings(
    name := "db-alteration-core",
    libraryDependencies ++= Seq(
      postgresql,
      scalatest % Test,
      logback   % Test
    )
  )

lazy val coreIt = (project in file("core-it"))
  .configure(baseProject)
  .settings(
    libraryDependencies ++= Seq(
      postgresql,
      scalatest                % Test,
      logback                  % Test,
      testcontainers           % Test,
      testcontainersPostgresql % Test
    )
  )
  .dependsOn(core)

val compileAll = taskKey[Unit]("compile all configurations")
compileAll := compile.?.all(
  ScopeFilter(
    configurations = inAnyConfiguration
  )
).value
