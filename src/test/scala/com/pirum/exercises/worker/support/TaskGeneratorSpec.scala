package com.pirum.exercises.worker.support

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.pirum.exercises.worker.model._
import com.pirum.exercises.worker.util.TestTaskGenerator
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.duration._

class TaskGeneratorSpec extends TestKit(ActorSystem()) with
  AnyFreeSpecLike with Matchers with TestTaskGenerator with ScalaCheckPropertyChecks {

  val numGen: Gen[Int] = Gen.choose(1, 20000)
  val taskGenerator = new TaskGenerator()

  "TaskGenerator" - {
    "should create tasks with max run duration not surpassing input" in {
      forAll(numGen) { num =>
        val res = taskGenerator.generate(num, maxRunDurationSeconds = Some(num))
        res.map {
          case task: Successful =>
            task.runDuration should be >= 1.second
            task.runDuration should be <= num.seconds
          case task: Failed =>
            task.runDuration should be >= 1.second
            task.runDuration should be <= num.second
          case _: TimedOut => succeed
        }
      }
    }
    "should create the number of tasks specified by the input" in {
      forAll(numGen) { num =>
        val res = taskGenerator.generate(num, maxRunDurationSeconds = Some(num), idOpt = Some("id1"))
        res.size shouldBe num
      }
    }
  }

}
