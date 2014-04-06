package utils

import scala.io.Source
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import models.{RawTimeSampleDAO, RawTimeSample, NeuronDAO, Neuron}

object DataLoader {

  def loadData() = {

    if( NeuronDAO.collection.size == 0) {
      val positionsIterator = Source.fromFile("public\\connectdata\\test\\networkPositions_test.txt").getLines()
      val zippedPositions : scala.collection.Iterator[scala.Tuple2[String, scala.Int]] = positionsIterator.zipWithIndex

      zippedPositions.foreach { position =>
        val positions = position._1.split(",")
        val neuron = Neuron(index = position._2 ,xPos = positions(0), yPos = positions(1), isOld = true, timeSample = Seq())
        NeuronDAO.insert(neuron)
      }

      println("All neurons inserted successfully")
    }
    else
      println("Data is already loaded. To load fresh drop the collection and retry")
  }

  //Loads each parsed line into the database as a Seq of Strings
  def loadRawTimeSampleData() = {

    println("Preparing to load raw time sample data...")
    val timeSeriesIterator = Source.fromFile("public\\connectdata\\test\\fluorescence_test.txt").getLines()
    val zippedTimeSeries : scala.collection.Iterator[Tuple2[String, Int]] = timeSeriesIterator.zipWithIndex

    zippedTimeSeries.foreach { timeSeriesLine =>
      var timeSampleSeq: Seq[String] = Seq()
      timeSeriesLine._1.split(",").foreach { elem =>
        timeSampleSeq = timeSampleSeq :+ elem
      }
      val rawTimeSample = RawTimeSample(index = timeSeriesLine._2, timeSamples = timeSampleSeq)
      RawTimeSampleDAO.insert(rawTimeSample)
    }

    println("Raw time sample data inserted successfully")
  }

  //integrates the time sample data into the neuron objects
  def integrateRawTimeSampleData() = {

    println("Preparing to integrate raw time sample data...")

    var index:Int = 0
    val allNeurons = NeuronDAO.find(ref = MongoDBObject())

    allNeurons.foreach { neuron =>
      var timeSampleSeq: Seq[String] = Seq()

      RawTimeSampleDAO.find(ref = MongoDBObject()).foreach { sample =>
        timeSampleSeq = timeSampleSeq :+ sample.timeSamples(index)
      }

      val newNeuron = Neuron(index = neuron.index, xPos = neuron.xPos, yPos = neuron.yPos, isOld = false, timeSample = timeSampleSeq)
      NeuronDAO.insert(newNeuron)
      index = index + 1
      println("Neuron integrated:" + index)
    }

    println("Raw time sample data integrated successfully")
  }
}