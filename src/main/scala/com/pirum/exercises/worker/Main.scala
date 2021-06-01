package com.pirum.exercises.worker

import akka.actor.ActorSystem
import com.pirum.exercises.worker.config.PirumConfig
import com.pirum.exercises.worker.core.TaskProcessor
import com.pirum.exercises.worker.exceptions.ProgramTimeoutException
import com.pirum.exercises.worker.model._
import com.pirum.exercises.worker.support.PirumActorSystem.actorSystem
import com.pirum.exercises.worker.support._
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}
import scala.jdk.DurationConverters._
import scala.util.{Failure, Success, Try}

object Main extends App with Program with PirumConfig {

  /**
   *  use to generate a larger task batch
   *
   *  private val taskGenerator = new TaskGenerator()
   *  private val allTasks = taskGenerator.generate(15, Some(5))
   *  private val generatedTasks = Random.shuffle(allTasks)
   *
   * */


  private implicit val printer: MainPrinter = new MainPrinter()
  private implicit val conf: Config = config

  val tasks = List(
    Failed("task3", 3.second),
    Successful("task4", 4.second),
    Successful("task2", 2.seconds),
    Failed("task1", 1.seconds)
  )

  private val programTimeout = config.getDuration("pirum.programTimeOut").toScala
  private val workers = config.getInt("pirum.workers")

  program(tasks, workers, programTimeout)

  def program(tasks: List[Task], workers: Int, programTimeout: FiniteDuration)(
      implicit actorSystem: ActorSystem , printer: Printer, config: Config): Unit = {
    val shouldValidate = config.getBoolean("pirum.validateInput")
    if (shouldValidate) {
      InputValidator.validate(tasks, programTimeout) match {
        case Failure(e) => actorSystem.log.error(e.getMessage)
        case Success(_) => start()
      }
    } else start()

    def start(): Unit = {
      processTasks(tasks, workers, programTimeout) match {
        case Failure(e) =>
          actorSystem.log.error(e.getMessage)
          actorSystem.terminate()
        case Success(results) =>
          printer.printResults(results)
          actorSystem.log.info("program complete")
          actorSystem.terminate()
      }
    }
  }

  private def processTasks(tasks: List[Task], workers: Int, programTimeout: FiniteDuration): Try[Result] = {
    val taskProcessor = new TaskProcessor()
    val resultsProcessor = new TaskCompletionResultProcessor()
    val results = Try(Await.result(taskProcessor.processTasks(tasks, workers), programTimeout).toList) match {
      case Failure(_:TimeoutException) => Failure(ProgramTimeoutException())
      case Failure(e) => Failure(e)
      case Success(res) => Success(res)
    }
    resultsProcessor.createResultsForPrint(results)
  }
}
