package controllers.planning.termly

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltActions}
import controllers.pdf.PdfGeneratorWrapper
import controllers.serviceproxies.{ClassTimetableReaderServiceProxyImpl, TermServiceProxy, UserReaderServiceProxyImpl}
import duplicate.model.ClassDetails
import javax.inject.{Inject, Singleton}
import models.timetoteach.{CookieNames, TimeToTeachUserId}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DownloadPdfController @Inject()(
                                       cc: ControllerComponents,
                                       pdfGeneratorWrapper: PdfGeneratorWrapper,
                                       classTimetableReaderProxy: ClassTimetableReaderServiceProxyImpl,
                                       termService: TermServiceProxy,
                                       userReader: UserReaderServiceProxyImpl,
                                       deadbolt: DeadboltActions) extends AbstractController(cc)
{

  private def createTodaysDatePretty(): String =
  {
    val localDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("eeee, d MMMM yyyy")
    localDate.format(formatter)
  }

  def viewClassTermlyPlan(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    val eventualMaybeUser = userReader.getUserDetails(TimeToTeachUserId(tttUserId))
    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails: ClassDetails = maybeClassDetails.get
      maybeUser <- eventualMaybeUser
      if maybeUser.isDefined
      user = maybeUser.get
    } yield Ok(views.html.planning.termly.viewClassTermlyPlan(
      classDetails,
      termService.currentSchoolTerm(),
      createTodaysDatePretty(),
      List(user)
    ))
  }

  def downloadClassPdf(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    val eventualMaybeUser = userReader.getUserDetails(TimeToTeachUserId(tttUserId))
    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails: ClassDetails = maybeClassDetails.get
      maybeUser <- eventualMaybeUser
      if maybeUser.isDefined
      user = maybeUser.get
    } yield pdfGeneratorWrapper.pdfGenerator.ok(views.html.planning.termly.viewClassTermlyPlan(
      classDetails,
      termService.currentSchoolTerm(),
      createTodaysDatePretty(),
      List(user)
    ), "http://localhost:9000")
  }

}
