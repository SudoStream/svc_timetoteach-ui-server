package potentialmicroservice.termservice.reader.dao

import java.time.LocalDate

import controllers.time.SystemTime
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
class TermReaderDaoImpl @Inject()(mongoDbConnection: MongoDbConnection,
                                  systemTime: SystemTime) extends TermReaderDao {

  private val logger: Logger = Logger
  private val schoolTermsCollection: MongoCollection[Document] = mongoDbConnection.getSchoolTermsCollection

  override def currentSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = {
    logger.debug(s"Looking for the current school term for ${localAuthority.toString}")

    val findMatcher = BsonDocument(
      SchoolTermSchema.LOCAL_AUTHORITY -> localAuthority.toString
    )

    val futureFoundSchoolTermDocumentsDocuments = schoolTermsCollection.find(findMatcher).toFuture()
    extractSchoolTermFromDocs(futureFoundSchoolTermDocumentsDocuments)
  }

  override def nextSchoolTerm(localAuthority: LocalAuthority): Future[Option[SchoolTerm]] = ???

  private def extractSchoolTermFromDocs(futureFoundSchoolTermDocumentsDocuments: Future[Seq[Document]]): Future[Option[SchoolTerm]] = {

    val eventualToday = systemTime.getToday

    {
      for {
        today <- eventualToday

        nothing = logger.debug(s"________________________ today is ${today}")

        currentAndFutureSchoolTermsAfterTermStart = futureFoundSchoolTermDocumentsDocuments.map {
          schoolTerms =>
            schoolTerms.filter { schoolTermDoc =>
              logger.debug(s"________________________ schoolTermDoc is ${schoolTermDoc.toString}")
              val termFirstDay = schoolTermDoc.getString(SchoolTermSchema.TERM_FIRST_DAY)
              val termLastDay = schoolTermDoc.getString(SchoolTermSchema.TERM_LAST_DAY)
              (
                (today.isBefore(LocalDate.parse(termLastDay).plusDays(1)) &&
                  today.isAfter(LocalDate.parse(termFirstDay).minusDays(1)))
                  ||
                  (today.isBefore(LocalDate.parse(termLastDay)) && today.isBefore(LocalDate.parse(termFirstDay)))
                )
            }
        }

        filteredSchoolTerms = currentAndFutureSchoolTermsAfterTermStart.map { schoolTerms =>
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

          val sortedTerms = convertedTerms.sortBy {
            case Some(term) => term.termFirstDay.toEpochDay
            case None => LocalDate.of(2000, 1, 1).toEpochDay
          }

          logger.debug(s"((((((((((((((((())))))))))))))))))))) ${sortedTerms.toString}")

          if (sortedTerms.nonEmpty)
            sortedTerms.head
          else
            None
        }

      } yield filteredSchoolTerms
    }.flatMap(res => res)
  }
}
