package com.pirum.exercises.worker.core

import akka.actor.ActorSystem
import com.pirum.exercises.worker.model._
import com.pirum.exercises.worker.util.TestTaskGenerator
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.concurrent.{Futures, PatienceConfiguration}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.duration._

class TaskProcessorSpec extends AnyFreeSpecLike
  with Matchers with ScalaCheckPropertyChecks with TestTaskGenerator with Futures with BeforeAndAfterEach{


  "TaskProcessor" - {
    "a single worker" - {
      "should process tasks one at a time in order if the tasks received" in {
        implicit val actorSystem: ActorSystem = ActorSystem("TaskProcessor-test-one-actor-system")
        val taskProcessor = new TaskProcessor()
        val tasks = List(
          Successful("success-1", 1.second),
          Failed("failed-1", 5.seconds),
          Failed("failed-2", 4.second),
          Successful("success-2", 3.seconds),
          Failed("failed-3", 1.second),
          Successful("success-3", 2.seconds)
        )


        val result = taskProcessor.processTasks(tasks, 1).futureValue(timeout = PatienceConfiguration.Timeout(Span(60, Seconds)))
        val completionTimes = result.map(_.completionTime)
        (completionTimes.max - completionTimes.min) +- 1.second.toMillis
        result.map(_.task) shouldBe Seq(
          Successful("success-1", 1.second),
          Failed("failed-1", 5.seconds),
          Failed("failed-2", 4.second),
          Successful("success-2", 3.seconds),
          Failed("failed-3", 1.second),
          Successful("success-3", 2.seconds)
        )

        actorSystem.terminate()
      }
    }
    "multiple workers" - {
      "should process tasks one at a time in parallel" in {
        implicit val actorSystem: ActorSystem = ActorSystem("TaskProcessor-test-two-actor-system")
        val taskProcessor = new TaskProcessor()
        val tasks = List(
          Failed("task3", 3.second),
          Successful("task4", 4.second),
          Successful("task2", 2.seconds),
          Failed("task1", 1.seconds)
        )

        val result = taskProcessor.processTasks(tasks, 4).futureValue(timeout = PatienceConfiguration.Timeout(Span(60, Seconds)))
        val completionTimes = result.map(_.completionTime)
        (completionTimes.max - completionTimes.min) +- 1.second.toMillis
        result.map(_.task) shouldBe Seq(
          Failed("task1", 1.seconds),
          Successful("task2", 2.seconds),
          Failed("task3", 3.second),
          Successful("task4", 4.second)
        )
        actorSystem.terminate()
      }
    }
  }
}
