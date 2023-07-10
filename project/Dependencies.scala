import sbt._

object Dependencies {
  lazy val logback                  = "ch.qos.logback" % "logback-classic"                % "1.4.6"
  lazy val postgresql               = "org.postgresql" % "postgresql"                     % "42.6.0"
  lazy val scalatest                = "org.scalatest" %% "scalatest"                      % "3.2.15"
  lazy val testcontainers           = "com.dimafeng"  %% "testcontainers-scala-scalatest" % "0.40.15"
  lazy val testcontainersPostgresql = testcontainers.withName("testcontainers-scala-postgresql")
}
