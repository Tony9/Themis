package me.tony9.test.flink.streaming

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.api.scala._
import org.apache.flink.table.functions.TableFunction

/**
  * - 关联静态数据
  */
object WordCountStreamingSqlTest {

  val stopWordsFile = "/Volumes/USB/data/stop-words.txt"

  def main(args: Array[String]) {

    streamWordCount(9999)
  }

  def streamWordCount(port: Integer) : Unit = {
    val hostName = "127.0.0.1"

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = TableEnvironment.getTableEnvironment(env)

    //stop words: 静态数据
    tableEnv.registerFunction("stop_words", new StopWord(stopWordsFile))
    //word stream: 动态数据
    val text = env.socketTextStream(hostName, port)

    val wordDataStream: DataStream[WordCount] = text
      .flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
      .map{ new WordCount(_, 1L) }

    tableEnv.registerDataStream("words", wordDataStream, 'word, 'c)

    val sql = s"""
        |select a.word, sum(a.c) as s
        |from words a
        |join lateral table(stop_words(a.word)) AS b(word)
        | on TRUE
        |group by a.word
      """.stripMargin


    val ds = tableEnv.sqlQuery(sql)

    ds.toRetractStream[(String, Long)].print

    env.execute("Scala SocketTextStreamWordCount Example")
  }

  case class WordCount(word: String, c: Long)

  class StopWord(filepath: String) extends TableFunction[String] {

    val words = scala.io.Source.fromFile(filepath).getLines().toArray[String]

    def eval(str: String): Unit = {
      if (!words.contains(str)) {
        collect("Y")
      }
    }
  }
}
