package utils

import models._
import com.mongodb.casbah.Imports._
import breeze.linalg._
import models.Neuron
import java.util.Date

object Statistics {

  val timeSlices = 179500

  def calcStatsForNeuron(neuronIndex: Int, neuron: Neuron) = {
    val mean = calcMean(neuronIndex)
    val variance = calculateVariance(neuronIndex, mean)

    NeuronDAO.update(MongoDBObject("_id" -> neuron._id),
      MongoDBObject("index" -> neuron.index ,"xPos" -> neuron.xPos, "yPos" -> neuron.yPos, "mean" -> mean, "variance" -> variance), multi = false, upsert = true)
  }

  def calcMean(neuronIndex: Int) = {
    var accumulator: Float = 0

    Finder.samplesForNeuron(neuronIndex).foreach { sample =>
      accumulator += sample.timeSamples.head
    }

    accumulator / timeSlices
  }

  def calculateVariance(neuronIndex: Int, mean : Float): Float = {
    var accumulator: Float = 0

    Finder.samplesForNeuron(neuronIndex).foreach { sample =>
      accumulator += scala.math.pow(sample.timeSamples.head - mean, 2).toFloat
    }

    accumulator / timeSlices
  }

  def calcRegression() : Seq[Double] = {
    val numberOfNeurons : Int = 4
    var correlationCoefficients : Seq[Double] = Seq()
    var xPosArr : Array[Double] = Array[Double]()
    var yPosArr : Array[Double] = Array()
    var fluorArr : Array[Double] = Array()

    NeuronDAO.find(ref = MongoDBObject()).sort(orderBy = MongoDBObject("index" -> 1)).limit(numberOfNeurons).foreach { n =>
      println("loading neuron into array")
      xPosArr = xPosArr :+ n.xPos.toDouble
      yPosArr = yPosArr :+ n.yPos.toDouble
      Finder.samplesForNeuron(n.index).foreach { ts =>
        fluorArr = fluorArr :+ ts.timeSamples.head.toDouble
      }
    }

    println("done loading....now matrix ops")

    val neuronMatrix = DenseMatrix(xPosArr,yPosArr)
    val result1 : breeze.linalg.DenseMatrix[Double] = inv(neuronMatrix.t :* neuronMatrix )
    val leastSquaresEstimates = (result1 :* neuronMatrix.t) :* new DenseMatrix(179500,numberOfNeurons,fluorArr)

    leastSquaresEstimates.forall { (a:(Int,Int), b:Double) =>
      println("a1: "+a._1+"       a2: "+a._2+"       b: "+b)
      correlationCoefficients = correlationCoefficients :+ b
      true
    }

    val regressionHistory = RegressionHistory(coefficients = correlationCoefficients, timeGenerated = new Date().toString, regressionType = "MultipleLinear")
    RegressionHistoryDAO.insert(regressionHistory)
    println("regression history inserted")
    correlationCoefficients
  }

  def autoCorrelation(index : Int, lag: Int) : Double = {
    var fluorArr : Seq[Float] = Seq()

    val neuron : Neuron = NeuronDAO.findOne(DBObject("index"->index)).get
      Finder.samplesForNeuron(index).foreach { ts =>
        fluorArr = fluorArr :+ ts.timeSamples.head
      }

    val mean = neuron.mean
    val zippedFluor = fluorArr.zipWithIndex
    val numer = zippedFluor.foldLeft(0f){ case (acc, (curr:Float, zip:Int)) => acc + ( if(zip+lag>=fluorArr.size) 0 else (curr - mean)*(fluorArr(zip+lag) - mean))}
    val denom = zippedFluor.foldLeft(0f){case (acc, (curr:Float, zip:Int)) =>  math.pow(curr - mean,2).toFloat}

    numer/denom
  }

}
