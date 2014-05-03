package utils

import models.{RawTimeSampleDAO, RawTimeSample, NeuronDAO, Neuron}
import com.mongodb.casbah.Imports._
import breeze.linalg._

object Statistics {

  def calcStatsForNeuron(neuronIndex: Int, neuron: Neuron)= {
    var accumulator: Float = 0

    Finder.samplesForNeuron(neuronIndex).foreach { sample =>
      accumulator += sample.timeSamples.head
    }

    val mean = accumulator / 179500
    val variance = calculateVariance(neuronIndex, neuron, mean)

    NeuronDAO.update(MongoDBObject("_id" -> neuron._id),
      MongoDBObject("index" -> neuron.index ,"xPos" -> neuron.xPos, "yPos" -> neuron.yPos, "mean" -> mean, "variance" -> variance), multi = false, upsert = true)

    println("Neuron sample avg " + accumulator + " and variance: " + variance)
  }

  def calculateVariance(neuronIndex: Int, neuron: Neuron, mean : Float): Float = {
    var accumulator: Float = 0

    Finder.samplesForNeuron(neuronIndex).foreach { sample =>
      accumulator += scala.math.pow(sample.timeSamples.head - mean, 2).toFloat
    }

    accumulator = accumulator / 179500
    accumulator
  }

  def calcRegression() = {

    var xPosArr : Array[Double] = Array[Double]()
    var yPosArr : Array[Double] = Array()
    var fluorArr : Array[Double] = Array()

    NeuronDAO.find(ref = MongoDBObject()).sort(orderBy = MongoDBObject("index" -> 1)).foreach { n =>
      xPosArr = xPosArr :+ n.xPos.toDouble
      yPosArr = yPosArr :+ n.yPos.toDouble
    }

    RawTimeSampleDAO.find(ref = MongoDBObject()).sort(orderBy = MongoDBObject("index" -> 1)).foreach { ts =>
      fluorArr = fluorArr :+ ts.timeSamples(0).toDouble //todo figure this shit out
    }

    val neuronMatrix = DenseMatrix(xPosArr,yPosArr)
    val fluorescenceMatrix = DenseMatrix(Array[Double](),Array[Double]())


    val result1 : breeze.linalg.DenseMatrix[Double] = inv(neuronMatrix :* neuronMatrix.t)

    val result2 = result1 :* neuronMatrix.t

    val leastSquaresEstimates = result2 :* fluorescenceMatrix

    println("Least Squares Estimates" + leastSquaresEstimates.toString)
  }

}
