package controllers.planning.weekly

import java.time.{LocalDate, LocalTime}

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.serviceproxies._
import controllers.time.SystemTime
import curriculum.scotland.EsOsAndBenchmarksBuilderImpl
import duplicate.model.ClassDetails
import duplicate.model.esandos.{CompletedEsAndOsByGroup, CompletedEsAndOsByGroupBySubject, NotStartedEsAndOsByGroup}
import duplicate.model.planning.{FullWeeklyPlanOfLessons, LessonsThisWeek, WeeklyPlanOfOneSubject}
import io.sudostream.timetoteach.messages.systemwide.model.UserPreferences
import javax.inject.{Inject, Singleton}
import models.timetoteach.classtimetable.SchoolDayTimes
import models.timetoteach.planning.pdf.CurriculumAreaTermlyPlanForPdfBuilder
import models.timetoteach.{ClassId, CookieNames, TimeToTeachUserId}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import security.MyDeadboltHandler
import shared.model.classtimetable.WwwClassId
import shared.util.{LocalTimeUtil, PlanningHelper}
import upickle.default.write
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
                                          planningWriterService: PlanningWriterServiceProxy,
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


  val subjectWeeklyPlansToSaveForm = Form(
    mapping(
      "subjectWeeklyPlansPickled" -> text,
      "notStarted" -> text,
      "completed" -> text
    )(OneSubjectWeeklyPlansJson.apply)(OneSubjectWeeklyPlansJson.unapply)
  )

  case class OneSubjectWeeklyPlansJson(
                                        subjectWeeklyPlansPickled: String,
                                        notStarted: String,
                                        completed: String
                                      )


  def weeklyViewOfWeeklyPlanningWithNoMondayDate(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))

    val route = for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      log1 = logger.debug(s"maybeClassDetails : ${maybeClassDetails.toString}")
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
      futureMaybeSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classDetails.schoolDetails.localAuthority))
      maybeSchoolTerm <- futureMaybeSchoolTerm
      if maybeSchoolTerm.isDefined
      schoolTerm = maybeSchoolTerm.get
    } yield Redirect(routes.WeeklyPlanningController.weeklyViewOfWeeklyPlanning(classId, schoolTerm.termFirstDay.toString))

    route
  }

  def weeklyViewOfWeeklyPlanning(classId: String, mondayDateOfWeekIso: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
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
      log1 = logger.debug(s"maybeClassDetails : ${maybeClassDetails.toString}")
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
      avroClassTimetableFuture = classTimetableReaderProxy.
        readAvroClassTimetable(TimeToTeachUserId(tttUserId), WwwClassId(classDetails.id.id))
      schoolDayTimes <- futureSchoolDayTimes
      maybeAvroClassTimetable <- avroClassTimetableFuture
      log2 = logger.debug(s"maybeAvroClassTimetable : ${maybeAvroClassTimetable.toString}")
      if maybeAvroClassTimetable.isDefined

      futureMaybeSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classDetails.schoolDetails.localAuthority))
      maybeSchoolTerm <- futureMaybeSchoolTerm
      log3 = logger.debug(s"maybeSchoolTerm : ${maybeSchoolTerm.toString}")
      if maybeSchoolTerm.isDefined
      schoolTerm = maybeSchoolTerm.get
      todaysDate <- eventualTodaysDate

      futureMaybefullWeeklyPlanOfLessons = planningReaderService.retrieveFullWeekOfLessons(
        TimeToTeachUserId(tttUserId),
        ClassId(classId),
        mondayDateOfWeekIso
      )
      fullWeeklyPlanOfLessons <- futureMaybefullWeeklyPlanOfLessons
      fullWeeklyPlanOfLessonsPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[FullWeeklyPlanOfLessons](fullWeeklyPlanOfLessons))
    } yield Ok(views.html.planning.weekly.weeklyView(
      new MyDeadboltHandler(userReader),
      userPictureUri,
      userFirstName,
      userFamilyName,
      classDetails,
      schoolDayTimes,
      maybeAvroClassTimetable.get,
      maybeSchoolTerm.get,
      maybeSchoolTerm.get.weekNumberForGivenDate(LocalDate.parse(mondayDateOfWeekIso)),
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

    import upickle.default._
    for {
      classes <- eventualClasses
      nothing0 = logger.debug(s"+=+= we are underway 0 - ${classes.toString}")
      nothing01 = logger.debug(s"+=+= we are underway 01 - filtering classId = ${classId}")
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      nothing1 = logger.debug(s"+=+= we are underway 1 - ${classDetailsList.toString}")
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
      nothing2 = logger.debug(s"+=+= we are underway 2")
      futureMaybeSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classDetails.schoolDetails.localAuthority))
      maybeSchoolTerm <- futureMaybeSchoolTerm
      if maybeSchoolTerm.isDefined

      nothing3 = logger.debug(s"+=+= we are underway 3")

      eventualMaybeCurriculumSelection = planningReaderService.
        currentTermlyCurriculumSelection(tttUserId, ClassId(classId), maybeSchoolTerm.get)

      maybeTermlyCurriculumSelection <- eventualMaybeCurriculumSelection
      if maybeTermlyCurriculumSelection.isDefined
      nothing4 = logger.debug(s"+=+= we are underway 4")
      curriculumSelection = maybeTermlyCurriculumSelection.get
      eventualClassTermlyPlan = planningReaderService.allClassTermlyPlans(tttUserId, classDetails, curriculumSelection.planningAreas)
      classTermlyPlan <- eventualClassTermlyPlan
      classTermlyPlanPdf = CurriculumAreaTermlyPlanForPdfBuilder.buildCurriculumAreaTermlyPlanForPdf(classTermlyPlan, classDetails)

      esAndOsToDetailMap <- eventualEsAndOsToDetailMap
      avroClassTimetableFuture = classTimetableReaderProxy.readAvroClassTimetable(tttUserId, WwwClassId(classDetails.id.id))
      maybeAvroClassTimetable <- avroClassTimetableFuture
      if maybeAvroClassTimetable.isDefined
      nothing5 = logger.debug(s"+=+= we are underway 5")

      futureMaybeSchoolTerm = termService.currentSchoolTerm(SchoolConverter.convertLocalAuthorityStringToAvroVersion(classDetails.schoolDetails.localAuthority))
      maybeSchoolTerm <- futureMaybeSchoolTerm
      if maybeSchoolTerm.isDefined

      nothing = logger.debug(s"+=+= maybeSchoolTerm = ${maybeSchoolTerm.get}")

      futureMaybeLessonsThisWeek = classTimetableReaderProxy.getThisWeeksLessons(tttUserId, WwwClassId(classDetails.id.id))
      maybeLessonsThisWeek <- futureMaybeLessonsThisWeek
      if maybeLessonsThisWeek.isDefined
      lessonsThisWeekPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[LessonsThisWeek](maybeLessonsThisWeek.get))
      todaysDate <- eventualTodaysDate

      futureMaybefullWeeklyPlanOfLessons = planningReaderService.retrieveFullWeekOfLessons(tttUserId, ClassId(classId), mondayDateOfWeekIso)
      fullWeeklyPlanOfLessons <- futureMaybefullWeeklyPlanOfLessons
      fullWeeklyPlanOfLessonsPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[FullWeeklyPlanOfLessons](fullWeeklyPlanOfLessons))

      futureCompletedEsAndOsBenchmarks = planningReaderService.completedEsOsBenchmarks(tttUserId, ClassId(classId), mondayDateOfWeekIso)
      completedEsAndOsBenchmarks <- futureCompletedEsAndOsBenchmarks
      log = logger.debug(s"completedEsAndOsBenchmarks : ${completedEsAndOsBenchmarks.toString}")
      completedEsAndOsBenchmarksPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[CompletedEsAndOsByGroupBySubject](completedEsAndOsBenchmarks))
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
      fullWeeklyPlanOfLessonsPickled,
      maybeSchoolTerm.get.weekNumberForGivenDate(LocalDate.parse(mondayDateOfWeekIso)),
      todaysDate,
      completedEsAndOsBenchmarksPickled
    ))
  }


  def savePlanForTheWeek(classId: String): Action[AnyContent] = Action.async { implicit request =>
    val subjectWeeklyPlans = subjectWeeklyPlansToSaveForm.bindFromRequest.get

    import upickle.default._
    val weeklyPlansToSave: WeeklyPlanOfOneSubject = read[WeeklyPlanOfOneSubject](
      PlanningHelper.decodeAnyNonFriendlyCharacters(subjectWeeklyPlans.subjectWeeklyPlansPickled))
    logger.debug(s"Subject Weekly plans Unpickled = ${weeklyPlansToSave.toString}")

    val notStartedEsOsBenchies: NotStartedEsAndOsByGroup = read[NotStartedEsAndOsByGroup](
      PlanningHelper.decodeAnyNonFriendlyCharacters(subjectWeeklyPlans.notStarted))
    logger.debug(s"Not Started Es&Os/Benchmarks Unpickled = ${notStartedEsOsBenchies.toString}")

    val completedEsOsBenchies: CompletedEsAndOsByGroup = read[CompletedEsAndOsByGroup](
      PlanningHelper.decodeAnyNonFriendlyCharacters(subjectWeeklyPlans.completed))
    logger.debug(s"Completed Es&Os/Benchmarks Unpickled = ${completedEsOsBenchies.toString}")

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(weeklyPlansToSave.tttUserId))

    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get

      savedPlanFutures = planningWriterService.saveWeeklyPlanForSingleSubject(
        weeklyPlansToSave,
        completedEsOsBenchies,
        notStartedEsOsBenchies
      )
      theFutures <- savedPlanFutures
    } yield Ok("Saved Weekly Plan Ok")
  }


  def saveEsOsBenchiesForTheWeek(classId: String): Action[AnyContent] = Action.async { implicit request =>
    logger.debug("saveEsOsBenchiesForTheWeek 1")
    val subjectWeeklyPlans = subjectWeeklyPlansToSaveForm.bindFromRequest.get
    logger.debug("saveEsOsBenchiesForTheWeek 2")

    import upickle.default._
    val weeklyPlansToSave: WeeklyPlanOfOneSubject = read[WeeklyPlanOfOneSubject](
      PlanningHelper.decodeAnyNonFriendlyCharacters(subjectWeeklyPlans.subjectWeeklyPlansPickled))
    logger.debug(s"Subject Weekly plans Unpickled = ${weeklyPlansToSave.toString}")

    val notStartedEsOsBenchies: NotStartedEsAndOsByGroup = read[NotStartedEsAndOsByGroup](
      PlanningHelper.decodeAnyNonFriendlyCharacters(subjectWeeklyPlans.notStarted))
    logger.debug(s"Not Started Es&Os/Benchmarks Unpickled = ${notStartedEsOsBenchies.toString}")

    val completedEsOsBenchies: CompletedEsAndOsByGroup = read[CompletedEsAndOsByGroup](
      PlanningHelper.decodeAnyNonFriendlyCharacters(subjectWeeklyPlans.completed))
    logger.debug(s"Completed Es&Os/Benchmarks Unpickled = ${completedEsOsBenchies.toString}")

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(weeklyPlansToSave.tttUserId))

    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get

      savedPlanFutures = planningWriterService.saveWeeklyPlanForSingleSubject(
        weeklyPlansToSave,
        completedEsOsBenchies,
        notStartedEsOsBenchies)
      theFutures <- savedPlanFutures
    } yield Ok("Saved Weekly Plan Ok")
  }


}
