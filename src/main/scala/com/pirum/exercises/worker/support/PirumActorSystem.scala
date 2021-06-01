package com.pirum.exercises.worker.support

import akka.actor.ActorSystem

object PirumActorSystem {
  /**
   *
   */
  implicit val actorSystem: ActorSystem = ActorSystem("PirumActorSystem")
}