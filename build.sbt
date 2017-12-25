

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

lazy val commonSettings = Seq(
  organization := "me.tony9",
  scalaVersion := "2.11.7",
  version      := "0.1.0-SNAPSHOT"
)

val flinkVersion = "1.4.0"
val kafkaVersion = "1.0.0"

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
      "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.5.0",
//      "org.slf4j" %% "slf4j-api" % "1.7.21",
//      "org.slf4j" %% "slf4j-log4j12" % "1.7.21",
//      "log4j" %% "log4j" % "1.2.17",
//      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.apache.flink" %% "flink-scala" % flinkVersion,
      "org.apache.flink" %% "flink-streaming-scala" % flinkVersion,
      "org.apache.flink" %% "flink-table" % flinkVersion,
      "org.apache.kafka" %% "kafka" % kafkaVersion,
      "org.apache.flink" %% "flink-connector-kafka" % "0.10.2",
      scalaTest % Test,
      "io.netty" % "netty-all" % "4.1.19.Final" % Test
    )
  )

// make run command include the provided dependencies
run in Compile := Defaults.runTask(fullClasspath in Compile,
  mainClass in (Compile, run),
  runner in (Compile,run)
).evaluated

// exclude Scala library from assembly
//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
