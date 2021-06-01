package com.pirum.exercises.worker.model

import scala.concurrent.duration.FiniteDuration

final case class TaskCompletionResult(task: Task, completionTime: Long) {
  override def toString: String = {
    def buildString(id: String, runDurationOpt: Option[FiniteDuration]): String = {
      val endOfOutput = s"result: ${task.getClass.getSimpleName}, completedOn: $completionTime)"
      runDurationOpt
        .map(runDuration => s"Task(taskId: $id, runDuration: $runDuration, $endOfOutput)")
        .getOrElse(s"Task(taskId: $id, $endOfOutput)")
    }

    task match {
      case Successful(id, runDuration) => buildString(id, Some(runDuration))
      case Failed(id, runDuration) => buildString(id, Some(runDuration))
      case TimedOut(id) => buildString(id, None)
    }
  }
}