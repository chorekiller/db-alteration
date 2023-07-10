import Dependencies._

inThisBuild(
  List(
    scalaVersion               := "2.13.10",
    version                    := "0.1.0-SNAPSHOT",
    organization               := "net.isammoc.chorekiller",
    organizationName           := "ChoreKiller",
    semanticdbEnabled          := true, // enable SemanticDB
    semanticdbVersion          := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)

lazy val core = (project in file("core"))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    name := "db-alteration-core",
    libraryDependencies ++= Seq(
      postgresql,
      scalatest                % "test,it",
      logback                  % "test,it",
      testcontainers           % "it",
      testcontainersPostgresql % "it"
    )
  )
