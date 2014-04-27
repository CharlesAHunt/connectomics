package utils

import models.{NeuronDAO, Neuron}
import com.mongodb.casbah.Imports._
import breeze.linalg._

object Statistics {

  def calcStatsForNeuron(neuronIndex: Int, neuron: Neuron)= {
    var accumulator: Float = 0

    Aggregator.samplesForNeuron(neuronIndex).foreach { sample =>
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

    Aggregator.samplesForNeuron(neuronIndex).foreach { sample =>
      accumulator += scala.math.pow(sample.timeSamples.head - mean, 2).toFloat
    }

    accumulator = accumulator / 179500
    accumulator
  }

  def calcRegression(neuronIndex: Int, neuron: Neuron) = {

    val neuronMatrix = DenseMatrix.zeros[Double](2,1000)
    val fluorescenceMatrix = DenseMatrix.zeros[Double](1,179500)
    //todo need to load up these matrices  with data

    val inverse : breeze.linalg.DenseMatrix[Double] = inv(neuronMatrix)

    val multiplied : breeze.linalg.DenseMatrix[Double] = neuronMatrix :* inverse

    val inverseOfMultiplied: breeze.linalg.DenseMatrix[Double] = inv(multiplied)

    val leastSquaresEstimates = inverseOfMultiplied :* inverse :* fluorescenceMatrix

    println("Least Squares Estimates" + leastSquaresEstimates.toString)
  }

}
