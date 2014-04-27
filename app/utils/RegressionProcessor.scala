package utils

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import models._
import com.mongodb.casbah.Imports._
import models.Neuron

class RegressionWorker extends Actor {

  def receive = {
    case RegressionWork(start, neuron) ⇒
      Statistics.calcRegression(start, neuron)
      sender ! RegressionResult()
  }

}

class RegressionMaster(nrOfWorkers: Int, nrOfMessages: Int, listener: ActorRef) extends Actor {
  var nrOfResults: Int = _
  val allNeurons = NeuronDAO.find(ref = MongoDBObject())

  val regressionWorkerRouter = context.actorOf(
    Props[RegressionWorker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "regressionWorkerRouter")

  def receive = {

    case RegressionCalculate ⇒
      for (i ← 0 until nrOfMessages) regressionWorkerRouter ! Work(i, allNeurons.next())

    case RegressionResult() ⇒
      nrOfResults += 1

      if (nrOfResults == nrOfMessages) {
        context.stop(self)
        context.system.shutdown()
      }
  }
}

sealed trait RegressionMessage
case object RegressionCalculate extends RegressionMessage
case class RegressionResult() extends RegressionMessage
case class RegressionWork(start: Int, neuron : Neuron) extends RegressionMessage
case class RegressionApproximation(numericalResult: Double, duration: Duration)
