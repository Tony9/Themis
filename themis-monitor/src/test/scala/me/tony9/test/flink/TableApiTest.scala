package me.tony9.test.flink

import org.apache.flink.api.scala._
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.api.scala._
import org.scalatest.{FlatSpec, Matchers}

class TableApiTest extends FlatSpec with Matchers {

  val filepath = "/Volumes/USB/data/csdn/csdn-100.txt"

  "MailCount:Using SQL" should "size=42, max=(\"21cn.com\", 18)" in {

    val r = mailCountBySql()
    r.size shouldEqual 42
    r.last shouldEqual ("21cn.com", 18)
  }

  def mailCountBySql(): Array[(String, Long)] = {

    val env = ExecutionEnvironment.getExecutionEnvironment
    val tableEnv = TableEnvironment.getTableEnvironment(env)

    val dataset = env.readTextFile(filepath).map((r:String) => {
      val account = r.toLowerCase.split("#").map(_.trim)
      val name = account(0)
      val password = account(1)
      val mail = s"${val x=account(2).split("@"); if (x.size > 1) x(1) else "-"}"

      (mail, 1L)
    })

    tableEnv.registerDataSet("mails", dataset, 'mail, 'c)

    val ds = tableEnv.sql(
      """
        |select mail, sum(c) as s
        |from mails
        |group by mail
        |order by sum(c)
      """.stripMargin
    ).toDataSet[(String, Long)]

    ds.collect().toArray[(String, Long)]

  }

}
