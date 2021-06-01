package com.pirum.exercises.worker.model

case class Result(successTasks: List[TaskCompletionResult], failedTasks: List[TaskCompletionResult], timedOutTasks: List[TaskCompletionResult])
