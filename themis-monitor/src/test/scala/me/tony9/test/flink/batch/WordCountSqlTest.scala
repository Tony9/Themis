package me.tony9.test.flink.batch

import org.apache.flink.api.scala._
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.api.scala._
import org.scalatest.{FlatSpec, Matchers}

class WordCountSqlTest extends FlatSpec with Matchers {

  val filepath = "/Volumes/USB/data/bible/bible.txt"
  val stopWordsFile = "/Volumes/USB/data/stop-words.txt"

  val env = ExecutionEnvironment.getExecutionEnvironment
  val tableEnv = TableEnvironment.getTableEnvironment(env)

  initTables("words", "stopWords")

  private def initTables(tableNames: String*): Unit = {

    if (tableNames.contains("words")) {
      val words = env.readTextFile(filepath)
        .flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
        .map{ (_, 1L) }

      tableEnv.registerDataSet("words", words, 'word, 'c)
    }

    if (tableNames.contains("stopWords")) {
      val stopWords = env.readTextFile(stopWordsFile)
        .map{ (_, 1L) }

      tableEnv.registerDataSet("stop_words", stopWords, 'word)
    }

  }

  "WordCount:Top20 - OrderBy, Limit" should "count(*)=20" in {

    val ds = tableEnv.sqlQuery(
      """
        |select word, sum(c) as s
        |from words
        |where word not in ('the', 'a', 'and', 'of', 'to', 'that', 'in', 'shall', 'for', 'is', 'be', 'not')
        |group by word
        |order by sum(c) desc
        |limit 20
      """.stripMargin
    ).toDataSet[(String, Long)]

    val r = ds.collect().toArray[(String, Long)]

    r.foreach {println(_)}

    r.size shouldEqual 20
    r.last shouldEqual ("me", 4095)

  }

  "WordCount:Top20 - 关联静态外部数据源" should "" in {

    val ds = tableEnv.sqlQuery(
      """
        |select
        | a.word,
        | sum(a.c) as s
        |from words a
        |left join stop_words b
        |  on a.word=b.word
        |where b.word is null
        |group by a.word
        |order by sum(a.c) desc
        |limit 10
      """.stripMargin
    ).toDataSet[(String, Long)]

    val r = ds.collect().toArray[(String, Long)]

    r.foreach {println(_)}
  }


}
