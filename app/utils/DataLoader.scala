package utils

import scala.io.Source
import models.{RawTimeSampleDAO, RawTimeSample, NeuronDAO, Neuron}

object DataLoader {

  def loadData() = {

    if( NeuronDAO.collection.size == 0) {
      val positionsIterator = Source.fromFile("C:\\testdata\\test\\networkPositions_test.txt").getLines()
      val zippedPositions : scala.collection.Iterator[scala.Tuple2[String, scala.Int]] = positionsIterator.zipWithIndex

      zippedPositions.foreach { position =>
        val positions = position._1.split(",")
        val neuron = Neuron(index = position._2 ,xPos = positions(0), yPos = positions(1), timeSample = Seq())
        NeuronDAO.insert(neuron)
      }

      println("All neurons inserted successfully")
    }
    else
      println("Data is already loaded. To load fresh drop the collection and retry")
  }

  //Loads each parsed line into the database as a Seq of Strings
  def loadRawTimeSampleData() = {

    val timeSeriesIterator = Source.fromFile("C:\\testdata\\test\\fluorescence_test.txt").getLines()
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
}
