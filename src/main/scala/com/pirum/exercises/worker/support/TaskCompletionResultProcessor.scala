package com.pirum.exercises.worker.support

import akka.actor.ActorSystem
import com.pirum.exercises.worker.model._

import scala.util.Try

class TaskCompletionResultProcessor(implicit actorSystem: ActorSystem) {

  /**
   *
   * @param results
   * Converts a list of TaskCompletionResult to a Result groups tasks by type
   */
  def createResultsForPrint(results: Try[List[TaskCompletionResult]]): Try[Result] = {
    results map { res =>
      res.foldRight(Result(List(), List(), List())) { (taskCompletionResult, acc) =>
        taskCompletionResult match {
          case success@TaskCompletionResult(Successful(_, _), _) =>
            val updated = success :: acc.successTasks
            acc.copy(successTasks = updated)
          case failed@TaskCompletionResult(Failed(_, _), _) =>
            val updated = failed :: acc.failedTasks
            acc.copy(failedTasks = updated)
          case timedOut@TaskCompletionResult(TimedOut(_), _) =>
            val updated = timedOut :: acc.timedOutTasks
            acc.copy(timedOutTasks = updated)
        }
      }
    }
  }
}
