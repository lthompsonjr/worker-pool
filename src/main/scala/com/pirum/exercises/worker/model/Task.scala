package com.pirum.exercises.worker.model

import com.pirum.exercises.worker.support.PirumActorSystem

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import PirumActorSystem._


// A task that either succeeds after n seconds, fails after n seconds, or never terminates

sealed trait Task {
  def execute: Future[Unit] = Future.unit
}

final case class Successful(id: String = UUID.randomUUID().toString, runDuration: FiniteDuration) extends Task {
  override def execute: Future[Unit] = akka.pattern.after(runDuration, using = actorSystem.scheduler)(Future.unit)
}

final case class Failed(id: String = UUID.randomUUID().toString,runDuration: FiniteDuration) extends Task {
  override def execute: Future[Unit] =
    akka.pattern.after(runDuration, using = actorSystem.scheduler)(Future.failed(new RuntimeException))
}

final case class TimedOut(id: String = UUID.randomUUID().toString) extends Task

