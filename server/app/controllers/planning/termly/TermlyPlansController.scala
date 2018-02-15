package controllers.planning.termly

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltActions}
import controllers.serviceproxies._
import curriculum.scotland.EsOsAndBenchmarksBuilderImpl
import duplicate.model.{ClassDetails, TermlyPlansToSave}
import models.timetoteach.planning.{GroupId, SubjectNameConverter}
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
                                       deadbolt: DeadboltActions) extends AbstractController(cc)
{

  import TermlyPlansController.buildSchoolNameToClassesMap

  val logger: Logger = Logger


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
        classDetails
      ))
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
            classDetails
          ))
        case None =>
          Redirect(routes.TermlyPlansController.termlyPlansSelectCurriculumAreas(classId))
      }
    } yield route
  }


  def termlyPlansForGroup(classId: String, subject: String, groupId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
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
        convertSubjectToCurriculumArea(subject)
      )
    } yield {
      Ok(views.html.planning.termly.termlyPlansForGroup(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        TimeToTeachUserId(tttUserId),
        classDetails,
        group,
        subject,
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

  def savePlansForGroup(classId: String, subject: String, groupId: String): Action[AnyContent] = Action.async { implicit request =>
    val termlyPlansForGroup = termlyPlansToSaveForm.bindFromRequest.get
    logger.debug(s"Termly Plans Pickled = #${termlyPlansForGroup.groupTermlyPlansPickled}#")

    import upickle.default._
    val termlyPlansToSave: TermlyPlansToSave = read[TermlyPlansToSave](termlyPlansForGroup.groupTermlyPlansPickled)
    logger.debug(s"Termly plans Unpickled = ${termlyPlansToSave.toString}")

    val termlyPlansAsModel = termsPlanHelper.convertTermlyPlanToModel(classId, termlyPlansToSave, Some(GroupId(groupId)), subject)
    val savedPlan = planningWriterService.saveSubjectTermlyPlan(termlyPlansAsModel)

    for {
      done <- savedPlan
    } yield Ok("Saved termly plans!")
  }

  def termlyOverviewForGroup(classId: String, subject: String, groupId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
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
            SubjectNameConverter.convertSubjectNameStringToSubjectName(subject)
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
            subject,
            maybeSubjectTermlyPlan.get,
            esAndOsCodeToDetailMap
          ))
        case None =>
          Redirect(routes.TermlyPlansController.termlyPlansForGroup(classId, subject, groupId))
      }
    } yield route
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


