lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

lazy val commonSettings = Seq(
  organization := "me.tony9",
  scalaVersion := "2.11.7",
  version      := "0.1.0-SNAPSHOT"
)

val flinkVersion = "1.4.0"


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
    name := "themis-monitor",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.apache.flink" %% "flink-scala" % flinkVersion,
      "org.apache.flink" %% "flink-streaming-scala" % flinkVersion
    )
  )

// make run command include the provided dependencies
run in Compile := Defaults.runTask(fullClasspath in Compile,
  mainClass in (Compile, run),
  runner in (Compile,run)
).evaluated

// exclude Scala library from assembly
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
