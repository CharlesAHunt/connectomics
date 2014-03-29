package models

import utils.DatabaseService
import org.bson.types.ObjectId
import com.novus.salat.dao.SalatDAO
import utils.LogicContext._

case class RawTimeSample (  _id: ObjectId = new ObjectId,
                     index: Int,
                     timeSamples: Seq[String]
                    )

object RawTimeSampleDAO extends SalatDAO[RawTimeSample, ObjectId] (
  collection = DatabaseService.getCollection("rawTimeSamples"))(manifest[RawTimeSample],manifest[ObjectId],ctx)