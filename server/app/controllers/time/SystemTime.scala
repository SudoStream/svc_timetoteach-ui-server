package controllers.time

import java.time.LocalDate

import dao.MongoDbConnection
import javax.inject.{Inject, Singleton}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{Document, MongoCollection}
import play.api.Logger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SystemTime @Inject()(mongoDbConnection: MongoDbConnection) {
  private val readTestDate = if (System.getProperty("system-date.read-test-date-from-database") == "true") true else false

  private val logger: Logger = Logger
  private val systemDateCollection: MongoCollection[Document] = mongoDbConnection.getSystemDateCollection

  def getToday: Future[LocalDate] = {
    if (readTestDate) {
      val findMatcher = BsonDocument()
      val eventualSystemTimeDocs = systemDateCollection.find(findMatcher).toFuture()
      for {
        systemTimeDocs <- eventualSystemTimeDocs
        localDate: LocalDate = extractLocalDate(systemTimeDocs)
      } yield localDate
    } else {
      Future {
        LocalDate.now()
      }
    }
  }

  private def extractLocalDate(systemTimeDocs: Seq[Document]): LocalDate = {
    if (systemTimeDocs.isEmpty) {
      LocalDate.now
    } else {
      val dateIso = systemTimeDocs.head.getString("systemDate")
      LocalDate.parse(dateIso)
    }
  }


}
