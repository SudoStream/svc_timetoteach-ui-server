package controllers.serviceproxies

import java.time.{LocalDate, LocalTime}
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.inject.Singleton
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import io.sudostream.timetoteach.kafka.serializing.systemwide.model.{UserPreferencesSerializer, UserSerializer}
import io.sudostream.timetoteach.messages.systemwide.model._
import models.timetoteach
import models.timetoteach.{InitialUserPreferences, TimeToTeachUser, TimeToTeachUserId}
import play.api.Logger
import utils.SchoolConverter.convertLocalSchoolToMessageSchool

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

@Singleton
class UserWriterServiceProxyImpl @Inject()(schoolReader: SchoolReaderServiceProxyImpl) {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)
  val logger: Logger.type = Logger
  private val config = ConfigFactory.load()
  val http = Http(system)
  private val userWriterServiceHostname = config.getString("services.user-writer-host")

  //  case class SchoolId(value: String)
  private val userWriterServicePort = config.getString("services.user-writer-port")
  private var theSchools: Map[String, io.sudostream.timetoteach.messages.systemwide.model.School] = Map.empty
  private val minikubeRun: Boolean = sys.props.getOrElse("minikubeEnv", "false").toBoolean
  val protocol: String = if (userWriterServicePort.toInt > 9000 && !minikubeRun) "http" else "https"

  populateTheSchools

  def convertToUserPreferences(newUserPreferences: InitialUserPreferences): UserPreferences = {
    UserPreferences(
      allSchoolTimes = List(
        SchoolTimes(
          schoolId = newUserPreferences.schoolId,
          schoolStartTime = newUserPreferences.schoolStartTime,
          morningBreakStartTime = newUserPreferences.morningBreakStartTime,
          morningBreakEndTime = newUserPreferences.morningBreakEndTime,
          lunchStartTime = newUserPreferences.lunchStartTime,
          lunchEndTime = newUserPreferences.lunchEndTime,
          schoolEndTime = newUserPreferences.schoolEndTime
        )
      )
    )
  }

  def updateUserPreferences(userId: TimeToTeachUserId, newUserPreferences: InitialUserPreferences): Future[Boolean] = {
    val userPreferences: UserPreferences = convertToUserPreferences(newUserPreferences)
    logger.debug(s"updateUserPreferences: ${userPreferences.toString}")
    val uriString = s"$protocol://$userWriterServiceHostname:$userWriterServicePort/api/users/${userId.value}/editprefs"
    logger.debug(s"uri for updating user preferences is $uriString")
    val userWriterServiceUri = Uri(uriString)

    val userPreferencesSerializer = new UserPreferencesSerializer
    val userPreferencesBytes = userPreferencesSerializer.serialize("ignore", userPreferences)

    val postNewUserRequest = HttpRequest(HttpMethods.POST, uri = userWriterServiceUri, entity = userPreferencesBytes)

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )

    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val response: Future[HttpResponse] = http.singleRequest(postNewUserRequest, badCtx)

    response onComplete {
      case Success(httpResponse) => logger.info("User Preferences Updated!")
      case Failure(ex) => logger.error(s"Issue updating user preferences: ${ex.getMessage}")
    }

    response map { httpResponse =>
      if (httpResponse.status.isSuccess()) true else false
    }
  }

  def createNewUser(user: TimeToTeachUser): Future[TimeToTeachUserId] = {
    val userMessage = convertUserToMessage(user)
    val uriString = s"$protocol://$userWriterServiceHostname:$userWriterServicePort/api/users"
    logger.debug(s"uri string is $uriString")
    val userWriterServiceUri = Uri(uriString)

    val userSerializer = new UserSerializer
    val userBytes = userSerializer.serialize("ignore", userMessage)

    val postNewUserRequest = HttpRequest(HttpMethods.POST, uri = userWriterServiceUri, entity = userBytes)

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )
    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val response: Future[HttpResponse] = http.singleRequest(postNewUserRequest, badCtx)

    response onComplete {
      case Success(httpResponse) => logger.info("User Created!")
      case Failure(ex) => logger.error(s"Issue creating user: ${ex.getMessage}")
    }

    response map { httpResponse =>
      TimeToTeachUserId(userMessage.timeToTeachId)
    }
  }

  def convertUserToMessage(user: TimeToTeachUser): io.sudostream.timetoteach.messages.systemwide.model.User = {
    if (theSchools.isEmpty) {
      populateTheSchools
    }

    val socialNetwork = user.socialNetworkName.toString.toUpperCase match {
      case "FACEBOOK" => SocialNetwork.FACEBOOK
      case "GOOGLE" => SocialNetwork.GOOGLE
      case "TWITTER" => SocialNetwork.TWITTER
      case _ => SocialNetwork.OTHER
    }

    val theSocialNetworkIds = SocialNetworkIdWrapper(
      SocialNetworkId(socialNetwork = socialNetwork, id = user.socialNetworkUserId)
    )

    logger.debug(s"Looking up school with id '${user.schoolId}'")

    val theSchool = theSchools.getOrElse(user.schoolId, io.sudostream.timetoteach.messages.systemwide.model.School(
      id = user.schoolId,
      name = "TODO - Failed to get school details from school reader service",
      address = "TODO - Failed to get school details from school reader service",
      postCode = "TODO - Failed to get school details from school reader service",
      telephone = "TODO - Failed to get school details from school reader service",
      localAuthority = LocalAuthority.OTHER,
      country = Country.OTHER
    ))

    io.sudostream.timetoteach.messages.systemwide.model.User(
      timeToTeachId = "user_" +
        user.firstName.hashCode.toString + user.familyName.hashCode.toString +
        theSocialNetworkIds.socialNetworkId.socialNetwork.toString.hashCode +
        theSocialNetworkIds.socialNetworkId.id.hashCode.toString,
      socialNetworkIds = List(theSocialNetworkIds),
      fullName = user.firstName + " " + user.familyName,
      givenName = if (user.firstName.isEmpty) None else Some(user.firstName),
      familyName = if (user.familyName.isEmpty) None else Some(user.familyName),
      imageUrl = if (user.picture.isEmpty) None else Some(user.picture),
      userAccountCreated = UserAccountCreatedDetails(
        dateSignedUp_Iso8601 = LocalDate.now().toString,
        timeSignedUp_Iso8601 = LocalTime.now().toString
      ),
      emails = List(
        EmailDetails(
          emailAddress = user.emailAddress,
          validated = true, // TODO: Pull this info through from signup
          preferred = true // TODO: Pull this info through from signup
        )
      ),
      userRole = UserRole.TEACHER,
      schools = List(SchoolWrapper(theSchool)),
      userPreferences = None
    )
  }

  private def populateTheSchools = {
    val theSchoolsFuture: Future[Seq[timetoteach.School]] = schoolReader.getAllSchoolsFuture

    theSchoolsFuture.onComplete {
      case Success(seqSchools) =>
        logger.debug(s"${seqSchools.size} future schools gotted")
        theSchools = seqSchools map {
          school => school.id -> convertLocalSchoolToMessageSchool(school)
        } toMap
      case Failure(t) =>
        t.printStackTrace()
        logger.error(s"Failed to get schools on loadup. The error was ... ${t.toString} \n\n" + t.getStackTrace.map {
          line => line.toString
        }.toString)
    }
  }
}


