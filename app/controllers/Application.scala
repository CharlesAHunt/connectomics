package controllers

import play.api.mvc._
import akka.actor.{Props, ActorSystem}
import utils._
import utils.{NeuronMaster, NeuronListener, NeuronCalculate}
import models.NeuronDAO
import com.mongodb.casbah.commons.MongoDBObject

object Application extends Controller with Access {

  def index = Action { implicit request =>
    Ok(views.html.index(loginForm, registerForm))
  }

  def launchNeurons() = Action { implicit request =>
    calculate(nrOfWorkers = 10, nrOfMessages = 1000)

    def calculate(nrOfWorkers: Int, nrOfMessages: Int) {
      val system = ActorSystem("NeuronSystem")

      val listener = system.actorOf(Props[NeuronListener], name = "Neuronlistener")

      val master = system.actorOf(Props(new NeuronMaster(
        nrOfWorkers, nrOfMessages, listener)), name = "master")

      master ! NeuronCalculate
    }

    Ok(views.html.index(loginForm, registerForm))

  }

  def loadNeurons() = Action { implicit request =>
    DataLoader.loadData()
    Ok(views.html.index(loginForm, registerForm))
  }

  def loadRaw() = Action { implicit request =>
    DataLoader.loadRawTimeSampleData()
    Ok(views.html.index(loginForm, registerForm))
  }

  def integrate() = Action { implicit request =>
    //DataLoader.integrateRawTimeSampleData()
    Ok(views.html.index(loginForm, registerForm))
  }

  def crunch() = Action { implicit request =>
    Statistics.calcRegression()
    Ok(views.html.index(loginForm, registerForm))
  }

  def positions(end: Int) = Action {
    val jsonBuilder = StringBuilder.newBuilder
    jsonBuilder.append("[")
    getCollection("neurons").find().foreach( jsonBuilder.append(_).append(",") )
    jsonBuilder.deleteCharAt(jsonBuilder.length-1)
    jsonBuilder.append("]")
    Ok(jsonBuilder.toString())
  }

  def regression() = Action {
    val jsonBuilder = StringBuilder.newBuilder
    jsonBuilder.append("""[{"date":"1","close": 2.13},{"date":"2","close": 3.98},{"date":"3","close": 3.00},{"date":"4","close": 7.70}]""")
    Ok(jsonBuilder.toString())
  }

  def view() = Action { implicit request =>
  //todo maybe just redirect to a view screen
    Ok(views.html.index(loginForm, registerForm))
  }


}