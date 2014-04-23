package utils

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import models.{RawTimeSample, RawTimeSampleDAO, Neuron, NeuronDAO}
import com.mongodb.casbah.Imports._

object NeuronProcessor {

  def calculateFor(neuronIndex: Int, neuron : Neuron, timeSamples : Seq[Seq[Float]]): Float = {
    var accumulator : Float = 0

    timeSamples.foreach { sample =>
      accumulator += sample(neuronIndex)
    }

    accumulator = accumulator/timeSamples.size

    println("calced neuron " + accumulator)

    accumulator

  }
}

class NeuronWorker extends Actor {

  def receive = {
    case Work(start, neuron, timeSamples) ⇒
      sender ! NeuronResult(NeuronProcessor.calculateFor(start, neuron, timeSamples))
  }

}

class NeuronMaster(nrOfWorkers: Int, nrOfMessages: Int, listener: ActorRef) extends Actor {

  var averageFluorList: List[Float] = List()
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis
  val allNeurons = NeuronDAO.find(ref = MongoDBObject())
  val allTimeSamples = RawTimeSampleDAO.find(ref = MongoDBObject())

  val workerRouter = context.actorOf(
    Props[NeuronWorker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

  def receive = {
    case NeuronCalculate ⇒
      var sampleBuffer : Seq[Seq[Float]] = Seq()
      allTimeSamples.foreach {
        sample: RawTimeSample =>
          sampleBuffer = sampleBuffer :+ sample.timeSamples
      }
      for (i ← 0 until nrOfMessages) workerRouter ! Work(i, allNeurons.next(), sampleBuffer)

    case NeuronResult(value) ⇒
      averageFluorList :+ value
      nrOfResults += 1
      if (nrOfResults == nrOfMessages) {

        averageFluorList.foreach(x=>print(x+", "))

        // Send the result to the listener
        listener ! NeuronApproximation(0, duration = (System.currentTimeMillis - start).millis)

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
case class Work(start: Int, neuron : Neuron, timeSamples : Seq[Seq[Float]]) extends NeuronMessage
case class NeuronResult(value: Double) extends NeuronMessage
case class NeuronApproximation(numericalResult: Double, duration: Duration)
