package controllers

import play.api.mvc._
import akka.actor.{Props, ActorSystem}
import utils._
import scala.concurrent.{Await, Future}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import utils.{NeuronMaster, NeuronListener, NeuronCalculate}

object Application extends Controller with Access {

  def index = Action { implicit request =>
    Ok(views.html.index(loginForm, registerForm))
  }

  def launchNeurons() = Action { implicit request =>
    calculate(nrOfWorkers = 5, nrOfElements = 500, nrOfMessages = 1000)

    def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
      val system = ActorSystem("NeuronSystem")
      val listener = system.actorOf(Props[NeuronListener], name = "Neuronlistener")
      val master = system.actorOf(Props(new NeuronMaster(
        nrOfWorkers, nrOfMessages, nrOfElements, listener)),
        name = "master")

      implicit val timeout = Timeout(5 seconds)
      val future = master ! NeuronCalculate
    }

    Ok(views.html.index(loginForm, registerForm))

  }

}