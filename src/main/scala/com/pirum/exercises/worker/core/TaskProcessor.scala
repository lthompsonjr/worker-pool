package com.pirum.exercises.worker.core

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Balance, Flow, GraphDSL, Merge, Sink, Source}
import akka.stream.{FlowShape, OverflowStrategy}
import com.pirum.exercises.worker.exceptions.TaskProcessingException
import com.pirum.exercises.worker.model.{Task, TaskCompletionResult}
import com.pirum.exercises.worker.support.Time

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Responsible for processing tasks
 * @param actorSystem
 */
class TaskProcessor()(implicit actorSystem: ActorSystem) extends Time {

  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  /**
   *  representation of a worker, takes a task as input and outputs a TaskCompletionResult
   *  handles a single task at a time backpressure producer if too fast
   */
  private val worker: Flow[Task, TaskCompletionResult, NotUsed] = Flow[Task].mapAsync(1) { task =>
    task.execute.map(_ => TaskCompletionResult(task, currentTimeMs())).recover {
      case _ => TaskCompletionResult(task, currentTimeMs())
    }
  }.buffer(1, OverflowStrategy.backpressure)

  /**
   *
   * @param worker
   * @param numWorkers
   * @tparam Input
   * @tparam Output
   * @return Creates flow that distributes tasks to a specified number of workers
   */
  private def taskDispatcher[Input, Output](worker: Flow[Input, Output, NotUsed], numWorkers: Int): Flow[Input, Output, NotUsed] = {
    actorSystem.log.info("processing tasks, please wait ...")
    Flow.fromGraph(GraphDSL.create() { implicit flow =>
      val dispatcher = flow.add(Balance[Input](numWorkers, waitForAllDownstreams = true))
      val merge = flow.add(Merge[Output](numWorkers))
      for (_ <- 1 to numWorkers) {
        dispatcher ~> worker.async ~> merge
      }
      FlowShape(dispatcher.in, merge.out)
    })
  }

  /**
   *
   * @param tasks
   * @param numWorkers
   * @return Future of a collection of TaskCompletionResult
   *         processes a given list of tasks using a specified number of workers
   */
  def processTasks(tasks: List[Task], numWorkers:Int): Future[Seq[TaskCompletionResult]] = {
    Source(tasks).via(taskDispatcher(worker, numWorkers)).runWith(Sink.seq)
      .recoverWith( e =>  Future.failed(TaskProcessingException(e.getMessage)))
  }

}
