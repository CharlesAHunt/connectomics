package utils

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import models.{RawTimeSampleDAO, Neuron, NeuronDAO}
import com.mongodb.casbah.Imports._

object NeuronProcessor {

  val allTimeSamples = RawTimeSampleDAO.find(ref = MongoDBObject())

  def calculateFor(start: Int, neuron : Neuron): Double = {
    var accumulator = 0.0

    accumulator += neuron.xPos
    
    allTimeSamples.find(x => x.index==1)

    accumulator
  }
}

class NeuronWorker extends Actor {

  def receive = {
    case Work(start, neuron) ⇒
      sender ! NeuronResult(NeuronProcessor.calculateFor(start, neuron))
  }

}

class NeuronMaster(nrOfWorkers: Int, nrOfMessages: Int, listener: ActorRef) extends Actor {

  var numericalResult: Double = _
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis
  val allNeurons = NeuronDAO.find(ref = MongoDBObject())

  val workerRouter = context.actorOf(
    Props[NeuronWorker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

  def receive = {
    case NeuronCalculate ⇒
      for (i ← 0 until nrOfMessages) workerRouter ! Work(i, allNeurons.next())

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
case class Work(start: Int, neuron : Neuron) extends NeuronMessage
case class NeuronResult(value: Double) extends NeuronMessage
case class NeuronApproximation(numericalResult: Double, duration: Duration)
