package utils

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import models._
import com.mongodb.casbah.Imports._
import models.Neuron

object NeuronProcessor {

  def calcStatsForNeuron(neuronIndex: Int, neuron: Neuron):Float = {
    var accumulator: Float = 0

    Aggregator.samplesForNeuron(neuronIndex).foreach { sample =>
        accumulator += sample.timeSamples.head
    }

    val mean = accumulator / 179500
    val variance = calculateVariance(neuronIndex, neuron, mean)

    NeuronDAO.update(MongoDBObject("_id" -> neuron._id),
      MongoDBObject("index" -> neuron.index ,"xPos" -> neuron.xPos, "yPos" -> neuron.yPos, "mean" -> mean, "variance" -> variance), multi = false, upsert = true)

    println("Neuron sample avg " + accumulator + " and variance: " + variance)

    //todo: calculate standard deviation
    variance
  }

  def calculateVariance(neuronIndex: Int, neuron: Neuron, mean : Float): Float = {
    var accumulator: Float = 0

    Aggregator.samplesForNeuron(neuronIndex).foreach { sample =>
      accumulator += scala.math.pow(sample.timeSamples.head - mean, 2).toFloat
    }

    accumulator = accumulator / 179500
    accumulator
  }
}

class NeuronWorker extends Actor {

  def receive = {
    case Work(start, neuron) ⇒
      sender ! NeuronResult(NeuronProcessor.calcStatsForNeuron(start, neuron))
  }

}

class NeuronMaster(nrOfWorkers: Int, nrOfMessages: Int, listener: ActorRef) extends Actor {

  var averageFluorList: List[Float] = List()
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis
  val allNeurons = NeuronDAO.find(ref = MongoDBObject())

  val workerRouter = context.actorOf(
    Props[NeuronWorker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

  def receive = {

    case NeuronCalculate ⇒
      for (i ← 0 until nrOfMessages) workerRouter ! Work(i, allNeurons.next())

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
case class Work(start: Int, neuron : Neuron) extends NeuronMessage
case class NeuronResult(value: Double) extends NeuronMessage
case class NeuronVarianceResult(value: Double) extends NeuronMessage
case class NeuronApproximation(numericalResult: Double, duration: Duration)
