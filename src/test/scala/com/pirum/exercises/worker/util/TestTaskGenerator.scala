package com.pirum.exercises.worker.util

import akka.actor.ActorSystem
import com.pirum.exercises.worker.model.{Failed, Successful, Task, TimedOut}
import org.scalacheck.Gen

import scala.concurrent.duration._

trait TestTaskGenerator {
  def taskGenerator(numTasksMin: Int, numTaskMax: Int,
                    maxSuccessTimeOut: Option[FiniteDuration] = None,
                    maxFailureTimeOut: Option[FiniteDuration] = None
                   )(implicit programTimeout: FiniteDuration, actorSystem: ActorSystem): Gen[List[Task]] = {
    for {
      successTimeOut <- Gen.choose(2.seconds, maxSuccessTimeOut.getOrElse(2.seconds))
      failureTimeOut <- Gen.choose(2.seconds, maxFailureTimeOut.getOrElse(2.seconds))
      numTasks <- Gen.choose(numTasksMin, numTaskMax)
      taskGen <- Gen.listOfN(numTasks, Gen.oneOf(Failed(runDuration = failureTimeOut), TimedOut(), Successful(runDuration = successTimeOut))) suchThat {
        tasks =>
          tasks.collect {
            case el@Successful(_, _) => el
          }.nonEmpty && tasks.collect {
            case el@Failed(_, _) => el
          }.nonEmpty && tasks.collect {
            case el@TimedOut(_) => el
          }.nonEmpty
      }
    } yield taskGen
  }
}
