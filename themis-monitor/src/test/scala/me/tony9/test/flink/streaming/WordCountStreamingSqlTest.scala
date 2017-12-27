package me.tony9.test.flink.streaming

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.api.scala._


object WordCountStreamingSqlTest {

  def main(args: Array[String]) {

    streamWordCount(9999)
  }

  def streamWordCount(port: Integer) : Unit = {
    val hostName = "127.0.0.1"

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = TableEnvironment.getTableEnvironment(env)
    val text = env.socketTextStream(hostName, port)

    val words: DataStream[Word] = text
      .flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
      .map{ new Word(_, 1L) }

    tableEnv.registerDataStream("words", words, 'word, 'c)

    val ds = tableEnv.sqlQuery(
      """
        |select word, sum(c) as s
        |from words
        |group by word
      """.stripMargin
    )

    ds.toRetractStream[Word].print

    env.execute("Scala SocketTextStreamWordCount Example")
  }

  case class Word(word: String, c: Long)

}
