package utils

import models.RawTimeSampleDAO
import com.mongodb.casbah.Imports._

object Finder {

  //Unfortunately, MongoDB does not yet support $slice in the aggregation pipeline.
  def samplesForNeuron(neuronIndex: Int) =
    RawTimeSampleDAO.find( MongoDBObject(),MongoDBObject("timeSamples" -> MongoDBObject("$slice" -> MongoDBList(neuronIndex, 1))))

}

//val result = RawTimeSampleDAO.collection.aggregate(List(
//MongoDBObject("$project" -> MongoDBObject ("timeSamples" -> 1)),
//MongoDBObject("$group" -> MongoDBObject("_id" -> null, "count" -> MongoDBObject("$sum" -> 1)))))
//result.results.mkString