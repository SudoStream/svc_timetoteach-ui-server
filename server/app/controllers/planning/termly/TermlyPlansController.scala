package controllers.planning.termly

import java.time.{LocalDateTime, LocalTime}

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltActions}
import controllers.pdf.PdfGeneratorWrapper
import controllers.planning.termly.TermlyPlansControllerFormHelper._
import controllers.serviceproxies._
import curriculum.scotland.EsOsAndBenchmarksBuilderImpl
import duplicate.model.{ClassDetails, TermlyPlansToSave}
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import javax.inject.{Inject, Singleton}
import models.timetoteach.planning.{TermlyCurriculumSelection, _}
import models.timetoteach.{ClassId, CookieNames, TimeToTeachUserId}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc._
import security.MyDeadboltHandler
import utils.TemplateUtils.getCookieStringFromRequest

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TermlyPlansController @Inject()(
                                       cc: ControllerComponents,
                                       userReader: UserReaderServiceProxyImpl,
                                       classTimetableReaderProxy: ClassTimetableReaderServiceProxyImpl,
                                       esAndOsReader: EsOsAndBenchmarksBuilderImpl,
                                       handlers: HandlerCache,
                                       planningWriterService: PlanningWriterServiceProxy,
                                       planningReaderService: PlanningReaderServiceProxy,
                                       termsPlanHelper: TermPlansHelper,
                                       termService: TermServiceProxy,
                                       pdfGeneratorWrapper: PdfGeneratorWrapper,
                                       deadbolt: DeadboltActions) extends AbstractController(cc)
{

  import TermlyPlansController.buildSchoolNameToClassesMap

  private val logger: Logger = Logger
  private val postSelectedCurriculumAreasUrl = routes.TermlyPlansController.curriulumAreasSelected()

  def termlyPlans: Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))

    for {
      classes <- eventualClasses

      futureMaybeCurriculumPlanProgress = planningReaderService.curriculumPlanProgressForClasses(
        TimeToTeachUserId(tttUserId),
        classes,
        termService.currentSchoolTerm()
      )

      maybeCurriculumPlanProgress <- futureMaybeCurriculumPlanProgress

    } yield {
      Ok(views.html.planning.termly.termlyPlansHome(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        TimeToTeachUserId(tttUserId),
        buildSchoolNameToClassesMap(classes),
        maybeCurriculumPlanProgress
      ))
    }
  }

  def createScottishCurriculumPlanningAreaWrapperList(): List[ScottishCurriculumPlanningAreaWrapper] =
  {
    {
      for {
        planningArea <- ScottishCurriculumPlanningArea.values()
        if planningArea != ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS
        if planningArea != ScottishCurriculumPlanningArea.NONE
      } yield ScottishCurriculumPlanningAreaWrapper(planningArea)
    }.toList
  }

  def termlyPlansSelectOverallCurriculumAreasForTheTerm(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
    } yield
      Ok(views.html.planning.termly.termlyPlansSelectOverallCurriculumAreasForTheTerm(
        new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        TimeToTeachUserId(tttUserId),
        classDetails,
        createScottishCurriculumPlanningAreaWrapperList(),
        postSelectedCurriculumAreasUrl,
        curriculumAreaSelectionDataForm
      )(authRequest))
  }

  def termlyPlansForClass(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")

    val eventualMaybeCurrentTermlyCurriculumSelection = planningReaderService.currentTermlyCurriculumSelection(
      TimeToTeachUserId(tttUserId),
      ClassId(classId),
      termService.currentSchoolTerm()
    )

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get

      maybeCurrentTermlyCurriculumSelection: Option[TermlyCurriculumSelection] <- eventualMaybeCurrentTermlyCurriculumSelection

      futureMaybeCurriculumPlanProgress = planningReaderService.curriculumPlanProgress(
        TimeToTeachUserId(tttUserId),
        classDetails,
        maybeCurrentTermlyCurriculumSelection
      )

      maybeCurriculumPlanProgress <- futureMaybeCurriculumPlanProgress

      route = maybeCurrentTermlyCurriculumSelection match {
        case Some(currentTermlyCurriculumSelection) =>
          Ok(views.html.planning.termly.termlyPlansForClassOverallOverview(new MyDeadboltHandler(userReader),
            userPictureUri,
            userFirstName,
            userFamilyName,
            TimeToTeachUserId(tttUserId),
            classDetails,
            currentTermlyCurriculumSelection,
            maybeCurriculumPlanProgress
          ))
        case None =>
          Redirect(routes.TermlyPlansController.termlyPlansSelectOverallCurriculumAreasForTheTerm(classId))
      }
    } yield route
  }

  def termlyPlansClassLevel_SelectEsOsBenchmarksForCurriculumArea(classId: String, curriculumArea: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    import utils.CurriculumConverterUtil._
    logger.debug(s"Will be selecting es, os and benchmarks for class($classId) and curriculumArea ($curriculumArea)")
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))

    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
      maybeRelevantEsAndOs <- esAndOsReader.buildEsOsAndBenchmarks(
        convertCurriculumAreaToModel(curriculumArea)
      )
      if maybeRelevantEsAndOs.isDefined
    } yield {
      Ok(views.html.planning.termly.termlyPlansSelectEsOsBenchmarksForCurriculumAreaAtClassLevel(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        TimeToTeachUserId(tttUserId),
        classDetails,
        curriculumArea,
        maybeRelevantEsAndOs.get
      ))
    }
  }

  def termlyPlansGroupLevel_SelectEsOsBenchmarksForCurriculumArea(classId: String, curriculumArea: String, groupId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    import utils.CurriculumConverterUtil._
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))

    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
      group = classDetails.groups.filter(group => group.groupId.id == groupId).head
      relevantEsAndOs <- esAndOsReader.buildEsOsAndBenchmarks(
        group.groupLevel,
        convertCurriculumAreaToModel(curriculumArea)
      )
    } yield {
      Ok(views.html.planning.termly.termlyPlansSelectEsOsBenchmarksForCurriculumAreaAtGroupLevel(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        TimeToTeachUserId(tttUserId),
        classDetails,
        group,
        curriculumArea,
        relevantEsAndOs
      ))
    }
  }

  val termlyPlansToSaveForm = Form(
    mapping(
      "termlyPlansPickled" -> text
    )(TermlyPlansToSaveJson.apply)(TermlyPlansToSaveJson.unapply)
  )

  case class TermlyPlansToSaveJson(
                                    groupTermlyPlansPickled: String
                                  )

  def savePlansForGroup(classId: String, curriculumArea: String, groupId: String): Action[AnyContent] = Action.async { implicit request =>
    val termlyPlansForGroup = termlyPlansToSaveForm.bindFromRequest.get
    logger.debug(s"Termly Plans Pickled = #${termlyPlansForGroup.groupTermlyPlansPickled}#")

    import upickle.default._
    val termlyPlansToSave: TermlyPlansToSave = read[TermlyPlansToSave](termlyPlansForGroup.groupTermlyPlansPickled)
    logger.debug(s"Termly plans Unpickled = ${termlyPlansToSave.toString}")

    val termlyPlansAsModel = termsPlanHelper.convertTermlyPlanToModel(classId, termlyPlansToSave, Some(GroupId(groupId)), curriculumArea)
    val savedPlan = planningWriterService.saveSubjectTermlyPlan(termlyPlansAsModel)

    for {
      done <- savedPlan
    } yield Ok("Saved termly plans!")
  }

  def savePlansForClass(classId: String, curriculumArea: String): Action[AnyContent] = Action.async { implicit request =>
    val termlyPlansForGroup = termlyPlansToSaveForm.bindFromRequest.get
    logger.debug(s"Termly Plans Pickled = #${termlyPlansForGroup.groupTermlyPlansPickled}#")

    import upickle.default._
    val termlyPlansToSave: TermlyPlansToSave = read[TermlyPlansToSave](termlyPlansForGroup.groupTermlyPlansPickled)
    logger.debug(s"Termly plans Unpickled = ${termlyPlansToSave.toString}")

    val termlyPlansAsModel = termsPlanHelper.convertTermlyPlanToModel(classId, termlyPlansToSave, None, curriculumArea)
    val savedPlan = planningWriterService.saveSubjectTermlyPlan(termlyPlansAsModel)

    for {
      done <- savedPlan
    } yield Ok("Saved termly plans!")
  }


  def termlyOverviewForCurriculumAreaAtClassLevel(classId: String, curriculumArea: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    val eventualEsAndOsToDetailMap = esAndOsReader.esAndOsCodeToEsAndOsDetailMap()

    val futureMaybeCurriculumAreaTermlyPlanForClassLevel = findAnyCurrentTermlyPlanForCurriculumAreaAtClassLevel(classId, curriculumArea, tttUserId)

    import utils.CurriculumConverterUtil.convertSubjectToScottishCurriculumPlanningAreaWrapper
    for {
      classes <- eventualClasses
      esAndOsCodeToDetailMap <- eventualEsAndOsToDetailMap
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get

      maybeCurriculumAreaTermlyPlanForGroup <- futureMaybeCurriculumAreaTermlyPlanForClassLevel
      route = maybeCurriculumAreaTermlyPlanForGroup match {
        case Some(curriculumAreaTermlyPlan: CurriculumAreaTermlyPlan) =>
          Ok(views.html.planning.termly.termlyPlansOverviewForCurriculumAtClassLevel(new MyDeadboltHandler(userReader),
            userPictureUri,
            userFirstName,
            userFamilyName,
            TimeToTeachUserId(tttUserId),
            classDetails,
            curriculumArea,
            maybeCurriculumAreaTermlyPlanForGroup.get,
            esAndOsCodeToDetailMap
          ))
        case None =>
          Redirect(routes.TermlyPlansController.termlyPlansClassLevel_SelectEsOsBenchmarksForCurriculumArea(classId, curriculumArea))
      }
    } yield route
  }

  def termlyOverviewForCurriculumAreaAtGroupLevel(classId: String, curriculumArea: String, groupId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    val eventualEsAndOsToDetailMap = esAndOsReader.esAndOsCodeToEsAndOsDetailMap()

    val futureMaybeCurriculumAreaTermlyPlanForGroup = findAnyCurrentTermlyPlanForCurriculumAreaAndGroup(classId, curriculumArea, groupId, tttUserId)

    for {
      classes <- eventualClasses
      esAndOsCodeToDetailMap <- eventualEsAndOsToDetailMap
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
      group = classDetails.groups.filter(group => group.groupId.id == groupId).head

      maybeCurriculumAreaTermlyPlanForGroup <- futureMaybeCurriculumAreaTermlyPlanForGroup
      route = maybeCurriculumAreaTermlyPlanForGroup match {
        case Some(curriculumAreaTermlyPlan: CurriculumAreaTermlyPlan) =>
          Ok(views.html.planning.termly.termlyPlansOverviewForCurriculumAtGroupLevel(new MyDeadboltHandler(userReader),
            userPictureUri,
            userFirstName,
            userFamilyName,
            TimeToTeachUserId(tttUserId),
            classDetails,
            group,
            curriculumArea,
            maybeCurriculumAreaTermlyPlanForGroup.get,
            esAndOsCodeToDetailMap
          ))
        case None =>
          Redirect(routes.TermlyPlansController.termlyPlansGroupLevel_SelectEsOsBenchmarksForCurriculumArea(classId, curriculumArea, groupId))
      }
    } yield route
  }

  private def findAnyCurrentTermlyPlanForCurriculumAreaAtClassLevel(classId: String, curriculumArea: String, tttUserId: String) =
  {
    CurriculumAreaConverter.convertCurriculumAreaStringToModel(curriculumArea) match {
      case Some(curriculumPlanningArea) =>
        planningReaderService.readCurriculumAreaTermlyPlanForClassLevel(
          TimeToTeachUserId(tttUserId),
          ClassId(classId),
          curriculumPlanningArea
        )
      case None => Future {
        None
      }
    }
  }

  private def findAnyCurrentTermlyPlanForCurriculumAreaAndGroup(classId: String, curriculumArea: String, groupId: String, tttUserId: String) =
  {
    CurriculumAreaConverter.convertCurriculumAreaStringToModel(curriculumArea) match {
      case Some(curriculumPlanningArea) =>
        planningReaderService.readCurriculumAreaTermlyPlanForGroup(
          TimeToTeachUserId(tttUserId),
          ClassId(classId),
          GroupId(groupId),
          curriculumPlanningArea
        )
      case None => Future {
        None
      }
    }
  }

  def curriulumAreasSelected: Action[AnyContent] = deadbolt.SubjectPresent()() { implicit request =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, request)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, request)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, request)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, request).getOrElse("NO ID")

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))

    val errorFunction = { formWithErrors: Form[CurriculumAreaSelectionData] =>
      logger.error(s"${LocalTime.now.toString} : Form ERROR : Oh well ... " + formWithErrors.errors.toString())
      for {
        classes <- eventualClasses
      } yield {
        BadRequest(views.html.planning.termly.termlyPlansHome(new MyDeadboltHandler(userReader),
          userPictureUri,
          userFirstName,
          userFamilyName,
          TimeToTeachUserId(tttUserId),
          buildSchoolNameToClassesMap(classes),
          Map()
        ))
      }
      // TODO: ANDY : Empty Map above should have progress in it
    }

    val successFunction = { curriculumAreaSelectionData: CurriculumAreaSelectionData =>
      logger.info(s"${LocalTime.now.toString} : Form SUCCESS")
      logger.debug(s"${LocalTime.now.toString} : CurriculumAreaSelectionData - ${curriculumAreaSelectionData.toString}")

      val cookies = request.cookies

      val theTimeToTeachUserId = cookies.get(CookieNames.timetoteachId) match {
        case Some(userId) => userId.value
        case None => ""
      }
      val termlyCurriculumSelection: TermlyCurriculumSelection = createTermlyCurriculumSelection(
        TimeToTeachUserId(theTimeToTeachUserId),
        LocalDateTime.now(),
        termService.currentSchoolTerm(),
        curriculumAreaSelectionData
      )
      val insertCompleted = planningWriterService.saveTermlyCurriculumSelection(termlyCurriculumSelection)

      for {
        classes <- eventualClasses
        classDetailsList = classes.filter(theClass => theClass.id.id == curriculumAreaSelectionData.classId)
        maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
        if maybeClassDetails.isDefined
        classDetails = maybeClassDetails.get
        done <- insertCompleted
      } yield {
        Redirect(routes.TermlyPlansController.termlyPlansForClass(classDetails.id.id))
      }
    }

    val formValidationResult = curriculumAreaSelectionDataForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def pdfTest = Action {
    pdfGeneratorWrapper.pdfGenerator.ok(views.html.planning.termly.termlyPlansForClassOverallOverviewPdf.apply(), "http://localhost:9000")
  }

}

object TermlyPlansController
{
  private[termly] def buildSchoolNameToClassesMap(classes: List[ClassDetails]): Map[String, List[ClassDetails]] =
  {
    @tailrec
    def buildSchoolNameToClassesMapLoop(currentMap: Map[String, List[ClassDetails]],
                                        remainingClasses: List[ClassDetails]): Map[String, List[ClassDetails]] =
    {
      if (remainingClasses.isEmpty) {
        currentMap
      } else {
        val nextClassToAdd = remainingClasses.head
        val schoolNameOfClassToAdd = nextClassToAdd.schoolDetails.name
        val maybeClasses = currentMap.get(schoolNameOfClassToAdd)
        val newClassesList = maybeClasses match {
          case Some(currentClasses) => nextClassToAdd :: currentClasses
          case None => nextClassToAdd :: Nil
        }
        buildSchoolNameToClassesMapLoop(currentMap + (schoolNameOfClassToAdd -> newClassesList), remainingClasses.tail)
      }
    }

    buildSchoolNameToClassesMapLoop(Map(), classes)
  }
}


