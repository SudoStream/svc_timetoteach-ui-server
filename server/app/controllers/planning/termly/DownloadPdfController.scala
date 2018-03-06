package controllers.planning.termly

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltActions}
import controllers.pdf.PdfGeneratorWrapper
import controllers.serviceproxies.{ClassTimetableReaderServiceProxyImpl, PlanningReaderServiceProxy, TermServiceProxy, UserReaderServiceProxyImpl}
import duplicate.model.ClassDetails
import javax.inject.{Inject, Singleton}
import models.timetoteach.planning.pdf.CurriculumAreaTermlyPlanForPdfBuilder
import models.timetoteach.{ClassId, CookieNames, TimeToTeachUserId}
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
                                       planningReaderService: PlanningReaderServiceProxy,
                                       deadbolt: DeadboltActions) extends AbstractController(cc)
{

  private def createTodaysDatePretty(): String =
  {
    val localDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("eeee, d MMMM yyyy")
    localDate.format(formatter)
  }

  def downloadClassPdf(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val tttUserId = TimeToTeachUserId(getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID"))
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(tttUserId)
    val eventualMaybeUser = userReader.getUserDetails(tttUserId)
    val eventualMaybeCurriculumSelection = planningReaderService.
      currentTermlyCurriculumSelection(tttUserId, ClassId(classId), termService.currentSchoolTerm())

    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails: ClassDetails = maybeClassDetails.get
      maybeUser <- eventualMaybeUser
      if maybeUser.isDefined
      user = maybeUser.get

      maybeTermlyCurriculumSelection <- eventualMaybeCurriculumSelection
      if maybeTermlyCurriculumSelection.isDefined
      curriculumSelection = maybeTermlyCurriculumSelection.get
      eventualClassTermlyPlan = planningReaderService.
        allClassTermlyPlans(tttUserId, classDetails, curriculumSelection.planningAreas)
      classTermlyPlan <- eventualClassTermlyPlan
      classTermlyPlanPdf = CurriculumAreaTermlyPlanForPdfBuilder.buildCurriculumAreaTermlyPlanForPdf(classTermlyPlan, classDetails)
    } yield pdfGeneratorWrapper.pdfGenerator.ok(views.html.planning.termly.pdfViewClassTermlyPlan(
      classDetails,
      termService.currentSchoolTerm(),
      createTodaysDatePretty(),
      List(user),
      classTermlyPlanPdf
    ), "http://localhost:9000")
  }

}
