package controllers.planning.termly

import java.time.{LocalDateTime, LocalTime}
import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltActions}
import controllers.planning.termly.TermlyPlansControllerFormHelper._
import controllers.serviceproxies._
import curriculum.scotland.EsOsAndBenchmarksBuilderImpl
import duplicate.model.{ClassDetails, TermlyPlansToSave}
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import models.timetoteach.planning.{TermlyCurriculumSelection, _}
import models.timetoteach.{ClassId, CookieNames, TimeToTeachUserId}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import security.MyDeadboltHandler
import utils.TemplateUtils.getCookieStringFromRequest

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
                                       deadbolt: DeadboltActions) extends AbstractController(cc)
{

  import TermlyPlansController.buildSchoolNameToClassesMap

  val logger: Logger = Logger
  private val postSelectedCurriculumAreasUrl = routes.TermlyPlansController.curriulumAreasSelected


  def termlyPlans: Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))

    for {
      classes <- eventualClasses
    } yield {
      Ok(views.html.planning.termly.termlyPlansHome(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        TimeToTeachUserId(tttUserId),
        buildSchoolNameToClassesMap(classes)
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

  def termlyPlansSelectCurriculumAreas(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
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
      Ok(views.html.planning.termly.termlyPlansSelectCurriculumAreas(
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
      ClassId(classId)
    )

    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    for {
      classes <- eventualClasses
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get

      maybeCurrentTermlyCurriculumSelection <- eventualMaybeCurrentTermlyCurriculumSelection
      route = maybeCurrentTermlyCurriculumSelection match {
        case Some(currentTermlyCurriculumSelection) =>
          Ok(views.html.planning.termly.termlyPlansForClass(new MyDeadboltHandler(userReader),
            userPictureUri,
            userFirstName,
            userFamilyName,
            TimeToTeachUserId(tttUserId),
            classDetails,
            currentTermlyCurriculumSelection
          ))
        case None =>
          Redirect(routes.TermlyPlansController.termlyPlansSelectCurriculumAreas(classId))
      }
    } yield route
  }


  def termlyPlansForClassAtGroupLevel(classId: String, curriculumArea: String, groupId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
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
        convertSubjectToCurriculumArea(curriculumArea)
      )
    } yield {
      Ok(views.html.planning.termly.termlyPlansForGroup(new MyDeadboltHandler(userReader),
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
      "groupTermlyPlansPickled" -> text
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

  def termlyOverviewForCurriculumAreaAtClassLevel(classId: String, curriculumArea: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    Future{
      Ok("Yep")
    }
  }

  def termlyOverviewForCurriculumAreaAtGroupLevel(classId: String, curriculumArea: String, groupId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)
    val tttUserId = getCookieStringFromRequest(CookieNames.timetoteachId, authRequest).getOrElse("NO ID")
    val eventualClasses = classTimetableReaderProxy.extractClassesAssociatedWithTeacher(TimeToTeachUserId(tttUserId))
    val eventualEsAndOsTodetailMap = esAndOsReader.esAndOsCodeToEsAndOsDetailMap()

    val futureMaybeSubjectName = eventualClasses.map {
      classes =>
        val classDetailsList = classes.filter(theClass => theClass.id.id == classId)
        val maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
        maybeClassDetails match {
          case Some(classDetails) =>
            val group = classDetails.groups.filter(group => group.groupId.id == groupId).head
            CurriculumAreaConverter.convertCurriculumAreaStringToSubjectName(curriculumArea)
          case None => None
        }
    }

    val futureMaybeSubjectTermlyPlan = {
      futureMaybeSubjectName.map {
        case Some(subjectName) =>
          planningReaderService.readSubjectTermlyPlan(
            TimeToTeachUserId(tttUserId),
            ClassId(classId),
            GroupId(groupId),
            subjectName
          )
        case None => Future {
          None
        }
      }
    }.flatMap(res => res)

    for {
      classes <- eventualClasses
      esAndOsCodeToDetailMap <- eventualEsAndOsTodetailMap
      classDetailsList = classes.filter(theClass => theClass.id.id == classId)
      maybeClassDetails: Option[ClassDetails] = classDetailsList.headOption
      if maybeClassDetails.isDefined
      classDetails = maybeClassDetails.get
      group = classDetails.groups.filter(group => group.groupId.id == groupId).head

      maybeSubjectTermlyPlan <- futureMaybeSubjectTermlyPlan
      route = maybeSubjectTermlyPlan match {
        case Some(subjectTermlyPlan) =>
          Ok(views.html.planning.termly.termlyPlansOverviewForGroup(new MyDeadboltHandler(userReader),
            userPictureUri,
            userFirstName,
            userFamilyName,
            TimeToTeachUserId(tttUserId),
            classDetails,
            group,
            curriculumArea,
            maybeSubjectTermlyPlan.get,
            esAndOsCodeToDetailMap
          ))
        case None =>
          Redirect(routes.TermlyPlansController.termlyPlansForClassAtGroupLevel(classId, curriculumArea, groupId))
      }
    } yield route
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
          buildSchoolNameToClassesMap(classes)
        ))
      }
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


