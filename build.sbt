lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

lazy val commonSettings = Seq(
  organization := "me.tony9",
  scalaVersion := "2.12.2",
  version      := "0.1.0-SNAPSHOT"
)

lazy val root = (project in file("."))
  .aggregate(agent, monitor)
  .dependsOn(agent, monitor)
  .settings(
    commonSettings,
    name := "themis",
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  )

lazy val agent = (project in file("./themis-agent"))
  .settings(
    commonSettings,
    name := "themis-agent"
  )

lazy val monitor = (project in file("./themis-monitor"))
  .settings(
    commonSettings,
    name := "themis-monitor"
  )