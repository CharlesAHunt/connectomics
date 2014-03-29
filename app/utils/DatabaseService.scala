package utils

import com.mongodb.casbah.Imports._
trait DatabaseService {

  val mongoClient = MongoClient("localhost", 27017)

  def getCollection(collectionName: String): MongoCollection = {
    val db = mongoClient("connectomics")
    db(collectionName)
  }
}

object DatabaseService extends DatabaseService

