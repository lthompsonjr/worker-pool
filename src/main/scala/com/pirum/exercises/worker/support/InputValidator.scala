package com.pirum.exercises.worker.support

import com.pirum.exercises.worker.model.{Failed, Successful, Task}
import com.pirum.exercises.worker.support.PirumActorSystem._

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

object InputValidator {

  /**
   *
   * @param tasks
   * @param programTimeout
   * Validates the user input task size and colletive run duration, task size should be between 25 and 60 and
   * the collective task timeout should be greater than the program timeout.
   */
  def validate(tasks: List[Task], programTimeout: FiniteDuration): Try[Unit] = {
    actorSystem.log.info("validating runtime requirements")
    val programTimeoutMs = programTimeout.toMillis

    val collectiveTimeoutForSuccesses = tasks.collect {
      case el@Successful(_, _) => el
    }.map(_.runDuration.toMillis).sum

    val collectiveTimeoutForFailedTasks = tasks.collect {
      case el@Failed(_, _) => el
    }.map(_.runDuration.toMillis).sum

    val collectiveTimeout = collectiveTimeoutForSuccesses + collectiveTimeoutForFailedTasks

    Try(require(tasks.size >= 25 && tasks.size <= 60, "number of tasks should range from 25 to 60")).map(_ =>
      require(collectiveTimeout > programTimeoutMs, "collective task duration must exceed timeout")
    )
  }
}
