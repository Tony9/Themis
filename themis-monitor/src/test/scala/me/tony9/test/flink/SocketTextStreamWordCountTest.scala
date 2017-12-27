package me.tony9.test.flink

import org.apache.flink.streaming.api.scala._


object SocketTextStreamWordCountTest {

  def main(args: Array[String]) {

    streamWordCount(9999)
  }

  def streamWordCount(port: Integer) : Unit = {
    val hostName = "127.0.0.1"

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val text = env.socketTextStream(hostName, port)
    val counts = text.flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
      .map { (_, 1) }
      .keyBy(0)
      .sum(1)

    counts print

    env.execute("Scala SocketTextStreamWordCount Example")
  }

}
