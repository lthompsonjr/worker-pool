package com.pirum.exercises.worker.support

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.pirum.exercises.worker.model.{Failed, Successful, TaskCompletionResult}
import com.pirum.exercises.worker.util.TestTaskGenerator
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.duration._
import scala.util.Success

class TaskCompletionResultProcessorSpec extends TestKit(ActorSystem()) with
  AnyFreeSpecLike with Matchers with TestTaskGenerator with ScalaCheckPropertyChecks with Time {
  "TaskCompletionResultProcessor" - {
    "should create results from a list of task completion results" in {
      implicit val programTimeout: FiniteDuration = 2000.seconds
      val taskCompletionResultProcessor = new TaskCompletionResultProcessor()
      val taskGen = taskGenerator(2, 8,
        maxSuccessTimeOut = Some(20.seconds),
        maxFailureTimeOut = Some(40.seconds)
      )
      forAll(taskGen) { tasks =>
        println(tasks)
        val taskCompletionResults = tasks.map(task => TaskCompletionResult(task, currentTimeMs()))
        println(taskCompletionResults)
        val taskCompletionResultsTry = Success(taskCompletionResults)

        val resultTry = taskCompletionResultProcessor.createResultsForPrint(taskCompletionResultsTry)
        resultTry.map{ result =>
          val runDurationsSuccessTasks = result.successTasks.map(_.task.asInstanceOf[Successful]).map(_.runDuration)
          val runDurationsSuccessTasksAsc = result.successTasks.map(_.task.asInstanceOf[Successful]).map(_.runDuration).sorted
          val runDurationsFailedTasks = result.failedTasks.map(_.task.asInstanceOf[Failed]).map(_.runDuration)
          val runDurationsFailedTasksAsc = result.successTasks.map(_.task.asInstanceOf[Failed]).map(_.runDuration).sorted
          runDurationsSuccessTasks should contain theSameElementsInOrderAs runDurationsSuccessTasksAsc
          runDurationsFailedTasks should contain theSameElementsInOrderAs runDurationsFailedTasksAsc
        }
      }
    }
  }
}
