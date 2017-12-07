package controllers

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import com.typesafe.config.ConfigFactory
import controllers.serviceproxies.{ClassTimetableWriterServiceProxyImpl, TimeToTeachUserId}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, _}
import play.api.mvc.{Action, AnyContent, Controller}
import shared.model.classtimetable.WwwClassName
import utils.ClassTimetableConverterToAvro.convertJsonClassTimetableToWwwClassTimetable

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class ClassTimetableController @Inject()(classTimetableWriter: ClassTimetableWriterServiceProxyImpl,
                                         deadbolt: DeadboltActions,
                                         handlers: HandlerCache,
                                         actionBuilder: ActionBuilders) extends Controller {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)
  private val config = ConfigFactory.load()
  private val userServiceHostname = config.getString("services.user-service-host")
  private val userServicePort = config.getString("services.user-service-port")
  private val timeToTeachFacebookId = config.getString("login.details.facebook.timetoteach-facebook-id")
  private val timeToTeachFacebookSecret = config.getString("login.details.facebook.timetoteach-facebook-secret")
  val logger: Logger.type = Logger

  val classTimetableForm = Form(
    mapping(
      "classTimetable" -> text,
      "className" -> text,
      "tttUserId" -> text
    )(ClassTimeTableJson.apply)(ClassTimeTableJson.unapply)
  )

  case class ClassTimeTableJson(classTimetable: String, className: String, tttUserId: String)

  def classTimetableSave: Action[AnyContent] = Action.async { implicit request =>
    val classTimetableFormBound = classTimetableForm.bindFromRequest.get
    logger.debug(s"Class Timetable As Json = ${classTimetableFormBound.classTimetable}")
    logger.debug(s"Class Name As Json = ${classTimetableFormBound.className}")
    logger.debug(s"TTT User Id = ${classTimetableFormBound.tttUserId}")

    val wwwClassTimetable = convertJsonClassTimetableToWwwClassTimetable(classTimetableFormBound.classTimetable)
    classTimetableWriter.upsertClassTimetables(
      TimeToTeachUserId(classTimetableFormBound.tttUserId),
      WwwClassName(classTimetableFormBound.className),
      wwwClassTimetable
    )

    Future {
      Ok("Hmmmmmmmm doodly doos!")
    }
  }

}