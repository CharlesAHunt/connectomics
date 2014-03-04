package utils

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import scala.io.Source

object NeuronProcessor {

  val csvIterator = Source.fromFile("C:\\testdata\\test\\fluorescence_test.txt").getLines()

  def calculateFor(start: Int, nrOfElements: Int): Double = {
    var acc = 0.0
    val values = csvIterator.next().split(",")
    for (i ← 0 until values.size) {
      values(i)
      acc += i //some processing might occur here...not sure yet what should happen inside each neuron worker
    }
    acc
  }
}

class NeuronWorker extends Actor {

  def receive = {
    case Work(start, nrOfElements) ⇒
      sender ! NeuronResult(NeuronProcessor.calculateFor(start, nrOfElements)) // perform the work
  }

}

class NeuronMaster(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, listener: ActorRef) extends Actor {

  var numericalResult: Double = _
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis

  val workerRouter = context.actorOf(
    Props[NeuronWorker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

  def receive = {
    case NeuronCalculate ⇒
      for (i ← 0 until nrOfMessages) workerRouter ! Work(i * nrOfElements, nrOfElements)
    case NeuronResult(value) ⇒
      numericalResult += value
      nrOfResults += 1
      if (nrOfResults == nrOfMessages) {

        // Send the result to the listener
        listener ! NeuronApproximation(numericalResult, duration = (System.currentTimeMillis - start).millis)

        // Stops this actor and all its supervised children
        context.stop(self)
      }
  }
}

class NeuronListener extends Actor {
  def receive = {
    case NeuronApproximation(numericalResult, duration) ⇒
      println("\n\tNeuron Result: \t\t%s\n\tCalculation time: \t%s"
        .format(numericalResult, duration))
      context.system.shutdown()
  }
}

sealed trait NeuronMessage
case object NeuronCalculate extends NeuronMessage
case class Work(start: Int, nrOfElements: Int) extends NeuronMessage
case class NeuronResult(value: Double) extends NeuronMessage
case class NeuronApproximation(numericalResult: Double, duration: Duration)
