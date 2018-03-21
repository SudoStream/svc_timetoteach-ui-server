package potentialmicroservice.termservice.reader.dao

import java.time.LocalDate

import dao.MongoDbConnection
import io.sudostream.timetoteach.messages.systemwide.model.LocalAuthority
import javax.inject.{Inject, Singleton}
import models.timetoteach.term.{SchoolTerm, SchoolTermName, SchoolYear}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.{Document, MongoCollection}
import play.api.Logger
import potentialmicroservice.termservice.sharedschema.SchoolTermSchema

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

@Singleton
class TermReaderDaoImpl @Inject()(mongoDbConnection: MongoDbConnection) extends TermReaderDao {

  private val logger: Logger = Logger
  private val schoolTermsCollection: MongoCollection[Document] = mongoDbConnection.getSchoolTermsCollection

  override def currentSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = {
    logger.debug(s"Looking for the current school term for ${localAuthority.toString}")

    val findMatcher = BsonDocument(
      SchoolTermSchema.LOCAL_AUTHORITY -> localAuthority.toString
    )

    val futureFoundSchoolTermDocumentsDocuments = schoolTermsCollection.find(findMatcher).toFuture()
    extractSchoolFromDocs(futureFoundSchoolTermDocumentsDocuments)
  }

  override def nextSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = ???

  private def extractSchoolFromDocs(futureFoundSchoolTermDocumentsDocuments: Future[Seq[Document]]): Future[Option[SchoolTerm]] = {
    val today = LocalDate.now()
    val filteredSchools = futureFoundSchoolTermDocumentsDocuments.map {
      schoolTerms =>
        schoolTerms.filter { schoolTermDoc =>
          val termFirstDay = schoolTermDoc.getString(SchoolTermSchema.TERM_FIRST_DAY)
          val termLastDay = schoolTermDoc.getString(SchoolTermSchema.TERM_LAST_DAY)
          today.isBefore(LocalDate.parse(termLastDay)) && today.isAfter(LocalDate.parse(termFirstDay))
        }
    }

    val filteredSchoolTerms = filteredSchools.map { schoolTerms =>
      val convertedTerms = schoolTerms.map { term =>
        val schoolYear = term.getString(SchoolTermSchema.SCHOOL_YEAR).split("-")
        val maybeTermName = SchoolTermName.convertToSchoolTermName(term.getString(SchoolTermSchema.SCHOOL_TERM_NAME))
        maybeTermName match {
          case Some(termName) =>
            Some(SchoolTerm(
              SchoolYear(schoolYear(0).toInt, Some(schoolYear(1).toInt)),
              termName,
              LocalDate.parse(term.getString(SchoolTermSchema.TERM_FIRST_DAY)),
              LocalDate.parse(term.getString(SchoolTermSchema.TERM_LAST_DAY))
            ))
          case None => None
        }
      }

      if (convertedTerms.nonEmpty)
        convertedTerms.head
      else
        None
    }


    filteredSchoolTerms onComplete {
      case Success(maybeSchoolTerm) => logger.debug(s" - - - - - MaybeSchool term = ${maybeSchoolTerm.toString}")
      case Failure(t) => logger.debug("filteredSchoolTerms an error has occured: " + t.getMessage)
    }

    filteredSchoolTerms
  }
}
