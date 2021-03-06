package utils

import scala.io.Source
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.global._
import models.{RawTimeSampleDAO, RawTimeSample, NeuronDAO, Neuron}

object DataLoader {

  def loadData() = {

    if (NeuronDAO.collection.size == 0) {
      val positionsIterator = Source.fromFile("public\\connectdata\\test\\networkPositions_test.txt").getLines()
      val zippedPositions: scala.collection.Iterator[(String, scala.Int)] = positionsIterator.zipWithIndex

      zippedPositions.foreach {
        position =>
          val positions = position._1.split(",")
          val neuron = Neuron(index = position._2, xPos = positions(0).toFloat, yPos = positions(1).toFloat, mean = 0, variance = 0)
          NeuronDAO.insert(neuron)
      }
      NeuronDAO.collection.ensureIndex("index")
      println("All neurons inserted successfully")
    }
    else
      println("Data is already loaded. To load fresh drop the collection and retry")
  }

  //Loads each parsed line into the database as a Seq of Strings
  def loadRawTimeSampleData() = {
    println("Preparing to load raw time sample data...")
    val timeSeriesIterator = Source.fromFile("public\\connectdata\\test\\fluorescence_test.txt").getLines()
    val zippedTimeSeries: scala.collection.Iterator[(String, Int)] = timeSeriesIterator.zipWithIndex

    zippedTimeSeries.foreach {
      timeSeriesLine =>
        var timeSampleSeq: Seq[Float] = Seq()
        timeSeriesLine._1.split(",").foreach {
          elem =>
            timeSampleSeq = timeSampleSeq :+ elem.toFloat
        }
        val rawTimeSample = RawTimeSample(index = timeSeriesLine._2, timeSamples = timeSampleSeq)
        RawTimeSampleDAO.insert(rawTimeSample)
    }

    println("Raw time sample data inserted successfully")
  }

//  //integrates the time sample data into the neuron objects
//  def integrateRawTimeSampleData() = {
//    println("Preparing to integrate raw time sample data...")
//    var start = 0
//    val chunkSize = 3000
//    var isMore = true
//    RawTimeSampleDAO.collection.ensureIndex("index")
//
//    while(isMore) {
//      println(((start/185500.0)*100.0).toInt + "% complete")
//      isMore = sliceAndIntegrate(start, start + chunkSize)
//      start += chunkSize
//    }
//
//    println("Raw time sample data integrated successfully")
//  }

//  def sliceAndIntegrate(start : Int, chunkSize : Int): Boolean = {
//    val allNeurons = NeuronDAO.find(ref = MongoDBObject()).sort(orderBy = MongoDBObject("index" -> 1))
//    val allTimeSamples = RawTimeSampleDAO.find(ref = MongoDBObject()).sort(orderBy = MongoDBObject("index" -> 1))
//    var sampleBuffer: Seq[Seq[Float]] = Seq()
//    allTimeSamples.slice(start, chunkSize).foreach {
//      sample: RawTimeSample =>
//        sampleBuffer = sampleBuffer :+ sample.timeSamples
//    }
//    if (sampleBuffer.isEmpty) false
//    allNeurons.foreach {
//      neuron =>
//        var nextBuffer: Seq[Seq[Float]] = Seq()
//        var currentList: Seq[Float] = Seq()
//        sampleBuffer.foreach {
//          sample: Seq[Float] =>
//            if (!sample.isEmpty) {
//              currentList = currentList :+ sample.head
//              nextBuffer = nextBuffer :+ sample.tail
//            }
//        }
//        if (currentList.isEmpty) false
//        else {
//          NeuronDAO.update(MongoDBObject("_id" -> neuron._id),
//            MongoDBObject("timeSample" -> neuron.timeSample.++(currentList), "index" -> neuron.index ,"xPos" -> neuron.xPos, "yPos" -> neuron.yPos), multi = false, upsert = true)
//          sampleBuffer = nextBuffer
//        }
//    }
//    true
//  }
}