package models

import utils.DatabaseService
import org.bson.types.ObjectId
import com.novus.salat.dao.SalatDAO
import utils.LogicContext._

case class Neuron (  _id: ObjectId = new ObjectId,
                   index: Int,
                   xPos: String,
                   yPos: String,
                   timeSample: Seq[String]
                  )

object NeuronDAO extends SalatDAO[Neuron, ObjectId] (
  collection = DatabaseService.getCollection("neurons"))(manifest[Neuron],manifest[ObjectId],ctx)