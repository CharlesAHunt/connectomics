package controllers

import play.api.mvc._
import akka.actor.{Props, ActorSystem}
import utils._
import utils.{NeuronMaster, NeuronListener, NeuronCalculate}
import models.{RegressionHistory, RegressionHistoryDAO, NeuronDAO}
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
    jsonBuilder.append("[")
    val latestRegressionHistory: RegressionHistory = RegressionHistoryDAO.find(ref = MongoDBObject()).next()

    (0 until 100).foreach { x=>
      var fluor : Double = 0
      latestRegressionHistory.coefficients.foreach { y =>
        fluor = fluor + x*y
      }
      jsonBuilder.append("""{"pos":" """+x+""" ","fluor": " """+fluor+""" "},""")
    }

    jsonBuilder.deleteCharAt(jsonBuilder.length-1)
    jsonBuilder.append("]")
    Ok(jsonBuilder.toString())
  }

  def view() = Action { implicit request =>
  //todo maybe just redirect to a view screen
    Ok(views.html.index(loginForm, registerForm))
  }


}