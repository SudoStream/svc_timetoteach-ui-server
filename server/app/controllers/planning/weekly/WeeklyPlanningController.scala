package controllers.planning.weekly

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.serviceproxies.{ClassTimetableReaderServiceProxyImpl, PlanningReaderServiceProxy, UserReaderServiceProxyImpl}
import curriculum.scotland.EsOsAndBenchmarksBuilderImpl
import javax.inject.{Inject, Singleton}
import models.timetoteach.CookieNames
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import security.MyDeadboltHandler
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
                                          planningReaderService: PlanningReaderServiceProxy
                                        ) extends AbstractController(cc)
{

  def weeklyPlanning = deadbolt.SubjectPresent()() { authRequest =>
    val userPictureUri = getCookieStringFromRequest(CookieNames.socialNetworkPicture, authRequest)
    val userFirstName = getCookieStringFromRequest(CookieNames.socialNetworkGivenName, authRequest)
    val userFamilyName = getCookieStringFromRequest(CookieNames.socialNetworkFamilyName, authRequest)

    Future {
      Ok(views.html.planning.weekly.homeWeeklyPlanning(new MyDeadboltHandler(userReader),
        "Weekly Planning",
        userPictureUri,
        userFirstName,
        userFamilyName)
      )
    }
  }


}
