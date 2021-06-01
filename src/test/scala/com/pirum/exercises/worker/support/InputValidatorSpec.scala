package com.pirum.exercises.worker.support

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.pirum.exercises.worker.model.{Failed, Successful}
import com.pirum.exercises.worker.util.TestTaskGenerator
import org.scalatest.TryValues
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.duration.{FiniteDuration, _}

class InputValidatorSpec extends TestKit(ActorSystem()) with AnyFreeSpecLike
  with Matchers with ScalaCheckPropertyChecks with TestTaskGenerator with TryValues {

  "InputValidator" - {
    "should result in an IllegalArgumentException if collective duration doesn't exceed the timeout" in {
      implicit val programTimeout: FiniteDuration = 2000.seconds
      val taskGen = taskGenerator(25, 60)
      forAll(taskGen) { tasks =>
        println("tasks.size " + tasks.size.toString)
        val collectiveTimeoutForSuccesses = tasks.collect {
          case el@Successful(_, _) => el
        }.map(_.runDuration.toMillis).sum

        val collectiveTimeoutForFailedTasks = tasks.collect {
          case el@Failed(_, _) => el
        }.map(_.runDuration.toMillis).sum
        val programTimeoutMs = programTimeout.toMillis

        collectiveTimeoutForFailedTasks + collectiveTimeoutForSuccesses should be < programTimeoutMs

        InputValidator.validate(tasks, programTimeout).failure

      }
    }
    "should result in an IllegalArgumentException if the number of tasks is below 25" in {
      implicit val programTimeout: FiniteDuration = 20.seconds
      val taskGen = taskGenerator(0, 24)
      forAll(taskGen) { tasks =>
        InputValidator.validate(tasks, programTimeout).failure
      }
    }
    "should result in an IllegalArgumentException if the number of tasks is grater than 60" in {
      implicit val programTimeout: FiniteDuration = 20.seconds
      val taskGen = taskGenerator(61, 1000)
      forAll(taskGen) { tasks =>
        InputValidator.validate(tasks, programTimeout).failure
      }
    }
    "should result in success if input is valid" in {
      implicit val programTimeout: FiniteDuration = 20.seconds
      val taskGen = taskGenerator(25, 26)
      forAll(taskGen) { tasks =>
        InputValidator.validate(tasks, programTimeout).success
      }
    }
  }
}
