package controllers.planning.weekly

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.serviceproxies.{ClassTimetableReaderServiceProxyImpl, PlanningReaderServiceProxy, UserReaderServiceProxyImpl}
import curriculum.scotland.EsOsAndBenchmarksBuilderImpl
import duplicate.model.ClassDetails
import javax.inject.{Inject, Singleton}
import models.timetoteach.{CookieNames, TimeToTeachUserId}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import security.MyDeadboltHandler
import utils.TemplateUtils.getCookieStringFromRequest

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class WeeklyPlanningController @Inject()(
                                          cc: ControllerComponents,
                                          deadbolt: DeadboltActions,
                                          esAndOsReader: EsOsAndBenchmarksBuilderImpl,
                                          classTimetableReaderProxy: ClassTimetableReaderServiceProxyImpl,
                                          userReader: UserReaderServiceProxyImpl,
                                          planningReaderService: PlanningReaderServiceProxy
                                        ) extends AbstractController(cc)
{

  def weeklyPlanning(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
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
    } yield Ok(views.html.planning.weekly.weeklyView(
      new MyDeadboltHandler(userReader),
      userPictureUri,
      userFirstName,
      userFamilyName,
      classDetails
    ))
  }

  def weeklyViewOfWeeklyPlanning(classId: String): Action[AnyContent] = deadbolt.SubjectPresent()() { authRequest =>
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
    } yield Ok(views.html.planning.weekly.weeklyView(
      new MyDeadboltHandler(userReader),
      userPictureUri,
      userFirstName,
      userFamilyName,
      classDetails
    ))

  }


}
