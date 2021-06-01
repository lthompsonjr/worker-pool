package com.pirum.exercises.worker.model

import akka.actor.ActorSystem
import com.pirum.exercises.worker.support.Printer
import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

trait Program {
  def program(tasks: List[Task], workers: Int, programTimeout: FiniteDuration)
             (implicit actorSystem: ActorSystem, printer: Printer, config: Config): Unit
}
