package models

import utils.DatabaseService
import org.bson.types.ObjectId
import com.novus.salat.dao.SalatDAO
import utils.LogicContext._

case class User (  _id: ObjectId = new ObjectId,
                   username: String,
                   token: String
                 )

object UserDAO extends SalatDAO[User, ObjectId] (
  collection = DatabaseService.getCollection("users"))(manifest[User],manifest[ObjectId],ctx)

