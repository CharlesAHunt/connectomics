package models

import utils.DatabaseService
import org.bson.types.ObjectId
import com.novus.salat.dao.SalatDAO
import utils.LogicContext._

case class RegressionHistory (  _id: ObjectId = new ObjectId,
                     coefficients: Seq[Double],
                     timeGenerated: String,
                     regressionType: String
                    )

object RegressionHistoryDAO extends SalatDAO[RegressionHistory, ObjectId] (
  collection = DatabaseService.getCollection("regressionhistory"))(manifest[RegressionHistory],manifest[ObjectId],ctx)