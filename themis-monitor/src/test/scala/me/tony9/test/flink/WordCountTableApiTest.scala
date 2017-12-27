package me.tony9.test.flink

import org.apache.flink.api.scala._
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.api.scala._
import org.scalatest.{FlatSpec, Matchers}

class WordCountTableApiTest extends FlatSpec with Matchers {

  val filepath = "/Volumes/USB/data/bible/bible.txt"

  "WordCount:Top20" should "" in {

    val env = ExecutionEnvironment.getExecutionEnvironment
    val tableEnv = TableEnvironment.getTableEnvironment(env)

    val dataset = env.readTextFile(filepath)
      .flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
      .map{ (_, 1L) }

    tableEnv.registerDataSet("words", dataset, 'word, 'c)

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


}
