package controllers.email

import javax.inject.Inject

import controllers.serviceproxies.SchoolReaderServiceProxyImpl
import models.timetoteach.{School, TimeToTeachUser}
import play.api.Logger
import play.api.libs.mailer.{Email, MailerClient}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class SignupEmailService @Inject()(mailerClient: MailerClient, schoolReader: SchoolReaderServiceProxyImpl) {

  val logger: Logger = Logger
  val eventualSchools: Future[Seq[School]] = schoolReader.getAllSchoolsFuture

  def sendEmail(user: TimeToTeachUser): Unit = {
    val schoolId = user.schoolId

    val eventualUserSchools = eventualSchools.map { schools =>
      for {
        school <- schools
        if school.id == schoolId
      } yield school
    }

    eventualUserSchools onComplete {
      case Success(schools) =>
        val schoolsInfo = for {
          school <- schools
          localAuthority = school.localAuthority.value.toLowerCase.replace("_"," ").split(" ").map(word => word.capitalize).mkString(" ")
        } yield
          s"""
            |<p>School Name: ${school.name}</p>
            |<p>School Local Authority: $localAuthority
          """.stripMargin

        val emailToTimeToTeachTeam = Email(
          "New person sign up to DEV Time to TEACH",
          from = "Time to TEACH Team <team@timetoteach.zone>",
          Seq(s"<team@timetoteach.zone>"),
          bodyHtml = Some(
            s"""<html><body>
               |<p>Hi</p>
               |<p>New user has registered</p>
               |<br>
               |<p>Firstname: ${user.firstName}</p>
               |<p>Surname: ${user.familyName}</p>
               |<p>Email: ${user.emailAddress}</p>
               |${schoolsInfo.mkString}
               |""".stripMargin),
          replyTo = Seq("Time to TEACH Team <team@timetoteach.zone>")
        )
        mailerClient.send(emailToTimeToTeachTeam)

      case Failure(ex) =>
        logger.error(s"Issue with getting schools : ${ex.getMessage}")
        val emailToTimeToTeachTeam = Email(
          "New person sign up to DEV Time to TEACH",
          from = "Time to TEACH Team <team@timetoteach.zone>",
          Seq(s"<team@timetoteach.zone>"),
          bodyHtml = Some(
            s"""<html><body>
               |<p>Hi</p>
               |<p>New user has registered</p>
               |<br>
               |<p>Firstname: ${user.firstName}</p>
               |<p>Surname: ${user.familyName}</p>
               |<p>Email: ${user.emailAddress}</p>
               |""".stripMargin),
          replyTo = Seq("Time to TEACH Team <team@timetoteach.zone>")
        )
        mailerClient.send(emailToTimeToTeachTeam)

        throw ex
    }
  }

}
