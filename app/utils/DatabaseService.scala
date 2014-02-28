package utils

import com.mongodb.casbah.Imports._
trait DatabaseService {

  //todo: move connection information into application.conf
//  val mongoClient = MongoClient("localhost", 27017)

 val mongoClient = MongoClient("ds061238.mongolab.com", 61238)

  def getCollection(collectionName: String): MongoCollection = {
        //val db = mongoClient("logicdb")
    val db = mongoClient("heroku_app20997644")
    db.authenticate("logicuser", "philo123")


    // Gets a reference to the collection
    // By default, you get a BSONCollection.

    db(collectionName)
  }
}

object DatabaseService extends DatabaseService

