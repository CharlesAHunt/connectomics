package utils

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import models._
import com.mongodb.casbah.Imports._
import models.Neuron

class NeuronWorker extends Actor {

  def receive = {
    case Work(start, neuron) ⇒
      Statistics.calcStatsForNeuron(start, neuron)
      sender ! NeuronResult()
  }

}

class NeuronMaster(nrOfWorkers: Int, nrOfMessages: Int, listener: ActorRef) extends Actor {
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis
  val allNeurons = NeuronDAO.find(ref = MongoDBObject())

  val workerRouter = context.actorOf(
    Props[NeuronWorker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

  def receive = {
    case NeuronCalculate ⇒
      for (i ← 0 until nrOfMessages) workerRouter ! Work(i, allNeurons.next())
    case NeuronResult() ⇒
      nrOfResults += 1
      if (nrOfResults == nrOfMessages) {
        listener ! NeuronApproximation(0, duration = (System.currentTimeMillis - start).millis)
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
case class NeuronResult() extends NeuronMessage
case class NeuronVarianceResult(value: Double) extends NeuronMessage
case class NeuronApproximation(numericalResult: Double, duration: Duration)
