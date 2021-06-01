package com.pirum.exercises.worker.support

import akka.actor.ActorSystem
import com.pirum.exercises.worker.model._

import scala.concurrent.duration._
import scala.util.Random

class TaskGenerator(implicit actorSystem: ActorSystem) {

  /**
   *
   * @param amount
   * @param maxRunDurationSeconds
   * @param idOpt optional task id
   * @return specified amount of tasks
   */
  def generate(amount: Int, maxRunDurationSeconds: Option[Int], idOpt: Option[String] = None): List[Task] = {
    val remainder = amount % 3
    val share = amount / 3

    (1 to (share + remainder)).map { _ =>
      idOpt.map(id => TimedOut(id)).getOrElse(TimedOut())
    }.toList :::
    (1 to share).map { _ =>
      idOpt
        .map(id => Successful(id, runDuration = Random.between(1, maxRunDurationSeconds.getOrElse(10) + 1).seconds))
        .getOrElse(Successful(runDuration = Random.between(1, maxRunDurationSeconds.getOrElse(10) + 1).seconds))
    }.toList :::
    (1 to share).map { _ =>
      idOpt
        .map(id => Failed(id, runDuration = Random.between(1, maxRunDurationSeconds.getOrElse(20) + 1).seconds))
        .getOrElse(Failed(runDuration = Random.between(1, maxRunDurationSeconds.getOrElse(20) + 1).seconds))
    }.toList
  }
}
