package com.pirum.exercises.worker.exceptions

sealed trait PirumException

case class TaskProcessingException(message: String) extends Exception(message) with PirumException
case class ProgramTimeoutException(message: String = "failed to process all tasks before the configured program timeout") extends Exception(message) with PirumException
