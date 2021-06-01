package com.pirum.exercises.worker.support

trait Time {
  /**
   *
   * @return
   */
  def currentTimeMs(): Long = System.currentTimeMillis()
}
