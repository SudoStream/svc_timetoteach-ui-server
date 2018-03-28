package controllers.planning.weekly

import java.time.{LocalDate, LocalTime}

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.serviceproxies.{ClassTimetableReaderServiceProxyImpl, PlanningReaderServiceProxy, TermServiceProxy, UserReaderServiceProxyImpl}
import controllers.time.SystemTime
import curriculum.scotland.EsOsAndBenchmarksBuilderImpl
import duplicate.model.ClassDetails
import duplicate.model.planning.LessonsThisWeek
import io.sudostream.timetoteach.messages.systemwide.model.UserPreferences
import javax.inject.{Inject, Singleton}
import models.timetoteach.classtimetable.SchoolDayTimes
import models.timetoteach.planning.pdf.CurriculumAreaTermlyPlanForPdfBuilder
import models.timetoteach.{ClassId, CookieNames, TimeToTeachUserId}
import play.api.Logger
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import security.MyDeadboltHandler
import shared.model.classtimetable.WwwClassId
import shared.util.{LocalTimeUtil, PlanningHelper}
import utils.SchoolConverter
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class WeeklyPlanningController @Inject()(
                                          cc: ControllerComponents,
                                          deadbolt: DeadboltActions,
                                          esAndOsReader: EsOsAndBenchmarksBuilderImpl,
                                          classTimetableReaderProxy: ClassTimetableReaderServiceProxyImpl,
                                          userReader: UserReaderServiceProxyImpl,
                                          planningReaderService: PlanningReaderServiceProxy,
                                          termService: TermServiceProxy,
                                          systemTime: SystemTime
                                        ) extends AbstractController(cc) {

  private val logger: Logger.type = Logger

  private val defaultSchoolDayTimes = SchoolDayTimes(
    schoolDayStarts = LocalTime.of(9, 0),
    morningBreakStarts = LocalTime.of(10, 30),
    morningBreakEnds = LocalTime.of(10, 45),
    lunchStarts = LocalTime.of(12, 0),
    lunchEnds = LocalTime.of(13, 0),
    schoolDayEnds = LocalTime.of(15, 0)
  )

  def weeklyViewOfWeeklyPlanning(classId: String, weekNumberRequested: Int): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    val futureSchoolDayTimes: Future[SchoolDayTimes] = getSchoolDayTimes(tttUserId)
    val eventualTodaysDate = systemTime.getToday

    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
      avroClassTimetableFuture = classTimetableReaderProxy.
        readAvroClassTimetable(TimeToTeachUserId(tttUserId), WwwClassId(classDetails.id.id))
      schoolDayTimes <- futureSchoolDayTimes
      maybeAvroClassTimetable <- avroClassTimetableFuture
      if maybeAvroClassTimetable.isDefined

      futureMaybeSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classDetails.schoolDetails.localAuthority))
      maybeSchoolTerm <- futureMaybeSchoolTerm
      if maybeSchoolTerm.isDefined
      schoolTerm = maybeSchoolTerm.get
      todaysDate <- eventualTodaysDate
      weekNumber = if (weekNumberRequested == 0) schoolTerm.weekNumberForGivenDate(todaysDate) else weekNumberRequested
    } yield Ok(views.html.planning.weekly.weeklyView(
      new MyDeadboltHandler(userReader),
      userPictureUri,
      userFirstName,
      userFamilyName,
      classDetails,
      schoolDayTimes,
      maybeAvroClassTimetable.get,
      maybeSchoolTerm.get,
      weekNumber,
      todaysDate
    ))
  }


  private def getSchoolDayTimes(tttUserId: String): Future[SchoolDayTimes] = {
    val futureUserPrefs: Future[Option[UserPreferences]] = userReader.getUserPreferences(TimeToTeachUserId(tttUserId))
    val futureSchoolDayTimes: Future[SchoolDayTimes] = futureUserPrefs map {
      case Some(userPrefs) =>
        val schoolTimes = userPrefs.allSchoolTimes.head
        logger.debug(s"classTimetable() : schoolTimes : ${schoolTimes.toString}")
        SchoolDayTimes(
          schoolDayStarts = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.schoolStartTime).getOrElse(
            defaultSchoolDayTimes.schoolDayStarts),
          morningBreakStarts = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.morningBreakStartTime).getOrElse(
            defaultSchoolDayTimes.morningBreakStarts),
          morningBreakEnds = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.morningBreakEndTime).getOrElse(
            defaultSchoolDayTimes.morningBreakEnds),
          lunchStarts = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.lunchStartTime).getOrElse(
            defaultSchoolDayTimes.lunchStarts),
          lunchEnds = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.lunchEndTime).getOrElse(
            defaultSchoolDayTimes.lunchEnds),
          schoolDayEnds = LocalTimeUtil.convertStringTimeToLocalTime(schoolTimes.schoolEndTime).getOrElse(
            defaultSchoolDayTimes.schoolDayEnds)
        )
      case None => defaultSchoolDayTimes
    }
    futureSchoolDayTimes
  }


  def createPlanForTheWeek(classId: String, mondayDateOfWeekIso: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId: TimeToTeachUserId = TimeToTeachUserId(getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID"))
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(tttUserId)
    val eventualEsAndOsToDetailMap = esAndOsReader.esAndOsCodeToEsAndOsDetailMap()
    val eventualTodaysDate = systemTime.getToday

    import upickle.default.{ReadWriter => RW, _}
    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get

      futureMaybeSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classDetails.schoolDetails.localAuthority))
      maybeSchoolTerm <- futureMaybeSchoolTerm
      if maybeSchoolTerm.isDefined

      eventualMaybeCurriculumSelection = planningReaderService.
        currentTermlyCurriculumSelection(tttUserId, ClassId(classId), maybeSchoolTerm.get)

      maybeTermlyCurriculumSelection <- eventualMaybeCurriculumSelection
      if maybeTermlyCurriculumSelection.isDefined
      curriculumSelection = maybeTermlyCurriculumSelection.get
      eventualClassTermlyPlan = planningReaderService.allClassTermlyPlans(tttUserId, classDetails, curriculumSelection.planningAreas)
      classTermlyPlan <- eventualClassTermlyPlan
      classTermlyPlanPdf = CurriculumAreaTermlyPlanForPdfBuilder.buildCurriculumAreaTermlyPlanForPdf(classTermlyPlan, classDetails)

      esAndOsToDetailMap <- eventualEsAndOsToDetailMap
      avroClassTimetableFuture = classTimetableReaderProxy.readAvroClassTimetable(tttUserId, WwwClassId(classDetails.id.id))
      maybeAvroClassTimetable <- avroClassTimetableFuture
      if maybeAvroClassTimetable.isDefined

      futureMaybeSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classDetails.schoolDetails.localAuthority))
      maybeSchoolTerm <- futureMaybeSchoolTerm
      if maybeSchoolTerm.isDefined

      futureMaybeLessonsThisWeek = classTimetableReaderProxy.getThisWeeksLessons(tttUserId, WwwClassId(classDetails.id.id))
      maybeLessonsThisWeek <- futureMaybeLessonsThisWeek
      if maybeLessonsThisWeek.isDefined
      lessonsThisWeekPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[LessonsThisWeek](maybeLessonsThisWeek.get))
      todaysDate <- eventualTodaysDate
    } yield Ok(views.html.planning.weekly.createPlanForTheWeek(
      new MyDeadboltHandler(userReader),
      userPictureUri,
      userFirstName,
      userFamilyName,
      tttUserId,
      classDetails,
      classTermlyPlanPdf,
      esAndOsToDetailMap,
      maybeAvroClassTimetable.get,
      maybeSchoolTerm.get,
      lessonsThisWeekPickled,
      maybeSchoolTerm.get.weekNumberForGivenDate(LocalDate.parse(mondayDateOfWeekIso)),
      todaysDate
    ))
  }

}
