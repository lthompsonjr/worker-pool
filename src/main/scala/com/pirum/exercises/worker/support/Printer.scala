package com.pirum.exercises.worker.support

import com.pirum.exercises.worker.model.Result

import scala.collection.mutable.ListBuffer

sealed trait Printer {
  def printOut(messageOpt: Option[String] = None): Unit

  /**
   *
   * @param result
   * prints results to printer output
   */
  def printResults(result: Result): Unit = {
    printOut(Some("*************************************************************"))
    printOut(Some("*************************************************************"))
    printOut()
    printOut(Some(s"result.successful = [${result.successTasks.mkString(", ")}]"))
    printOut(Some(s"result.failed = [${result.failedTasks.mkString(", ")}]"))
    printOut(Some(s"result.timedOut = [${result.timedOutTasks.mkString(", ")}]"))
    printOut()
    printOut(Some("*************************************************************"))
    printOut(Some("*************************************************************"))
  }
}

class MainPrinter extends Printer {
  /**
   *
   * @param messageOpt
   * prints to console
   */
  def printOut(messageOpt: Option[String] = None): Unit = {
    messageOpt match {
      case Some(message) => println(message)
      case None => println()
    }
  }
}

class TestPrinter extends Printer {
  var buffer: ListBuffer[String] = new ListBuffer()

  /**
   *
   * @param messageOpt
   * prints to in memory buffer
   */
  override def printOut(messageOpt: Option[String] = None): Unit = {
    messageOpt match {
      case Some(message) =>
        buffer = buffer.addOne(message)
        ()
      case None => ()
    }
  }
}


