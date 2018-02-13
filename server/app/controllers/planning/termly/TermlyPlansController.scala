package controllers.planning.termly

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltActions}
import controllers.serviceproxies.{ClassTimetableReaderServiceProxyImpl, PlanningWriterServiceProxy, TermServiceProxy, UserReaderServiceProxyImpl}
import curriculum.scotland.EsOsAndBenchmarksBuilderImpl
import duplicate.model.{ClassDetails, TermlyPlansToSave}
import models.timetoteach.planning.GroupId
import models.timetoteach.{CookieNames, TimeToTeachUserId}
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
                                       termsPlanHelper: TermPlansHelper,
                                       deadbolt: DeadboltActions) extends AbstractController(cc) {

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

  def termlyPlansForClass(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
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
    } yield {
      Ok(views.html.planning.termly.termlyPlansForClass(new MyDeadboltHandler(userReader),
        userPictureUri,
        userFirstName,
        userFamilyName,
        TimeToTeachUserId(tttUserId),
        classDetails
      ))
    }
  }


  def termlyPlansForGroup(classId: String, subject: String, groupId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest: AuthenticatedRequest[AnyContent] =>
    import utils.CurriulumConverterUtil._
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
    // TODO: This next andy
    Future {
      Ok("TODO - This")
    }
  }


}

object TermlyPlansController {
  private[termly] def buildSchoolNameToClassesMap(classes: List[ClassDetails]): Map[String, List[ClassDetails]] = {
    @tailrec
    def buildSchoolNameToClassesMapLoop(currentMap: Map[String, List[ClassDetails]],
                                        remainingClasses: List[ClassDetails]): Map[String, List[ClassDetails]] = {
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


