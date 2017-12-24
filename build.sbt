lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

lazy val commonSettings = Seq(
  organization := "me.tony9",
  scalaVersion := "2.12.2",
  version      := "0.1.0-SNAPSHOT"
)

lazy val root = (project in file("."))
  .aggregate(app1, app2)
  .dependsOn(app1, app2)
  .settings(
    commonSettings,
    name := "scala-project-seed",
    libraryDependencies ++= Seq(
      scalaTest % Test
    )
  )

lazy val app1 = (project in file("./sub-project-1"))
  .settings(
    commonSettings,
    name := "scala-project-seed-sub-project-1"
  )

lazy val app2 = (project in file("./sub-project-2"))
  .settings(
    commonSettings,
    name := "scala-project-seed-sub-project-2"
  )