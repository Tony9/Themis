package me.tony9.test.flink.batch

import org.apache.flink.api.scala._
import org.scalatest.{FlatSpec, Matchers}

class WordCountTest extends FlatSpec with Matchers {

  "WordCount:Using SQL" should "" in {

    // set up the execution environment
    val env = ExecutionEnvironment.getExecutionEnvironment

    // get input data
    val text = env.fromElements("To be, or not to be,--that is the question:--",
      "Whether 'tis nobler in the mind to suffer", "The slings and arrows of outrageous fortune",
      "Or to take arms against a sea of troubles,")

    val counts = text.flatMap { _.toLowerCase.split("\\W+") }
      .map { (_, 1) }
      .groupBy(0)
      .sum(1)

    // execute and print result
    counts.print()

  }
}
