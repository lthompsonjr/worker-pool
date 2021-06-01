package com.pirum.exercises.worker

import akka.actor.ActorSystem
import akka.testkit.{EventFilter, TestKit}
import com.pirum.exercises.worker.config.PirumConfig
import com.pirum.exercises.worker.model.{Failed, Successful}
import com.pirum.exercises.worker.support.TestPrinter
import com.pirum.exercises.worker.util.TestTaskGenerator
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class MainSpec extends TestKit(ActorSystem()) with AnyFreeSpecLike with Matchers with ScalaCheckPropertyChecks
  with BeforeAndAfterAll with PirumConfig with TestTaskGenerator {

  val printer = new TestPrinter()

  override def afterAll(): Unit = {
    system.terminate()
  }

  def tasksProcessed(buffer: ListBuffer[String]): Int = {
    buffer.flatMap(_.split("Task")).count(_.contains("taskId"))
  }

  override implicit val system: ActorSystem = ActorSystem("MySpec", ConfigFactory.parseString(
    """
    akka.loggers = ["akka.testkit.TestEventListener"]
    akka.loglevel = "ERROR"
  """))


  "program when ran with validation" - {
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

        EventFilter.error("requirement failed: collective task duration must exceed timeout", occurrences = 1) intercept {
          Main.program(tasks, 4, programTimeout)(system, printer, config)
        }
      }
    }
    "should result in an IllegalArgumentException if the number of tasks is below 25" in {
      implicit val programTimeout: FiniteDuration = 20.seconds
      val taskGen = taskGenerator(0, 24)
      forAll(taskGen) { tasks =>
        EventFilter.error("requirement failed: number of tasks should range from 25 to 60", occurrences = 1) intercept {
          Main.program(tasks, 4, programTimeout)(system, printer, config)
        }
      }
    }
    "should result in an IllegalArgumentException if the number of tasks is grater than 60" in {
      implicit val programTimeout: FiniteDuration = 20.seconds
      val taskGen = taskGenerator(61, 1000)
      forAll(taskGen) { tasks =>
        EventFilter.error("requirement failed: number of tasks should range from 25 to 60", occurrences = 1) intercept {
          Main.program(tasks, 4, programTimeout)(system, printer, config)
        }
      }
    }
    "should process all task and print results to specified printer output if input is valid" in {
      implicit val programTimeout: FiniteDuration = 20.seconds
      val taskGen = taskGenerator(25, 26)
      forAll(taskGen) { tasks =>
        printer.buffer.clear()
        Main.program(tasks, 60, programTimeout)(system, printer, config)
        tasksProcessed(printer.buffer) shouldBe tasks.size
      }
    }
  }
  "program when ran without validation" - {
    "should process all tasks and print results to specified printer output" in {
      val confString =
        """"pirum": {
          |"workers": 40
          |"programTimeOut": 2 seconds
          | "validateInput": false
          |}""".stripMargin
      val conf = ConfigFactory.parseString(confString)

      implicit val programTimeout: FiniteDuration = 20.seconds
      val taskGen = taskGenerator(4, 10)
      forAll(taskGen) { tasks =>
        printer.buffer.clear()
        Main.program(tasks, 60, programTimeout)(system, printer, conf)
        tasksProcessed(printer.buffer) shouldBe tasks.size
      }
    }
  }
}