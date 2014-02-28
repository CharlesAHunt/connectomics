package utils

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

object NeuronProcessor {

  def calculateFor(start: Int, nrOfElements: Int): Double = {
    var acc = 0.0
    for (i ← start until (start + nrOfElements))
      acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
    acc
  }
}

class NeuronWorker extends Actor {

  def receive = {
    case Work(start, nrOfElements) ⇒
      sender ! NeuronResult(NeuronProcessor.calculateFor(start, nrOfElements)) // perform the work
  }
}

class NeuronMaster(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, listener: ActorRef)
  extends Actor {

  var pi: Double = _
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis

  val workerRouter = context.actorOf(
    Props[NeuronWorker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

  def receive = {
    case NeuronCalculate ⇒
      for (i ← 0 until nrOfMessages) workerRouter ! Work(i * nrOfElements, nrOfElements)
    case NeuronResult(value) ⇒
      pi += value
      nrOfResults += 1
      if (nrOfResults == nrOfMessages) {

        // Send the result to the listener
        listener ! NeuronApproximation(pi, duration = (System.currentTimeMillis - start).millis)

        // Stops this actor and all its supervised children
        context.stop(self)
      }
  }
}

class NeuronListener extends Actor {
  def receive = {
    case NeuronApproximation(pi, duration) ⇒
      println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s"
        .format(pi, duration))
      context.system.shutdown()
  }
}

sealed trait NeuronMessage
case object NeuronCalculate extends NeuronMessage
case class Work(start: Int, nrOfElements: Int) extends NeuronMessage
case class NeuronResult(value: Double) extends NeuronMessage
case class NeuronApproximation(pi: Double, duration: Duration)
