package controllers.serviceproxies

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.inject.Singleton
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import io.sudostream.timetoteach.kafka.serializing.systemwide.model.SchoolsDeserializer
import io.sudostream.timetoteach.messages.systemwide.model.SingleSchoolWrapper
import models.timetoteach
import models.timetoteach.{Country, LocalAuthority, School}
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

@Singleton
class SchoolReaderServiceProxyImpl @Inject()(ws: WSClient) {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)

  private val config = ConfigFactory.load()
  private val schoolReaderServiceHostname = config.getString("services.school-reader-service-host")
  private val schoolReaderServicePort = config.getString("services.school-reader-service-port")
  println("\n\n\n" + sys.props.get("minikubeEnv").toString + "\n\n\n")
  private val minikubeRun: Boolean = sys.props.getOrElse("minikubeEnv", "false").toBoolean
  val protocol: String = if (schoolReaderServicePort.toInt > 9000 && !minikubeRun) "http" else "https"

  val logger: Logger.type = Logger

  private var schoolIdToSchoolMap: Map[String, School] = Map.empty
  populateTheSchools

  private def populateTheSchools = {
    val theSchoolsFuture: Future[Seq[timetoteach.School]] = getAllSchoolsFuture

    theSchoolsFuture.onComplete {
      case Success(seqSchools) =>
        this.synchronized(schoolIdToSchoolMap = seqSchools map {
          school => school.id -> school
        } toMap
        )

        logger.debug(s"${schoolIdToSchoolMap.size} future schools gotted for cache")

      case Failure(t) =>
        t.printStackTrace()
        logger.error(s"Failed to get schools on loadup. The error was ... ${t.toString} \n\n" + t.getStackTrace.map {
          line => line.toString
        }.toString)
    }
  }

  def getAllSchools(): Option[List[School]] = {
    logger.debug(s"okay so ... getAllSchools() schoolIdToSchoolMap size = ${schoolIdToSchoolMap.size}")

    var counter = 0
    while (schoolIdToSchoolMap.isEmpty && counter < 5) {
      populateTheSchools
      logger.info("No schools in cache ... populating now.")
      Thread.sleep(1000L * counter + 1)
      counter = counter + 1
    }

    if (schoolIdToSchoolMap.isEmpty) {
      logger.info("Empty .. returning None")
      None
    } else {
      val schools = schoolIdToSchoolMap.values.toList
      logger.info(s"Cool we have ${schools.size} to return")
      Some(schools)
    }
  }

  def getSchoolWithId(schoolIdToSearchFor: String): Option[School] = {
    if (schoolIdToSchoolMap.isEmpty) {
      None
    } else {
      val schoolsWithId = schoolIdToSchoolMap.filter(school => school._1 == schoolIdToSearchFor)
      if (schoolsWithId.values.toList.lengthCompare(1) != 0) {
        None
      } else {
        Some(schoolsWithId.head._2)
      }
    }
  }


  def getAllSchoolsFuture: Future[Seq[School]] = {
    val uriString = s"$protocol://$schoolReaderServiceHostname:$schoolReaderServicePort/api/schools"
    logger.debug(s"++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ uri string is $uriString")
    val schoolServiceUri = Uri(uriString)
    val req = HttpRequest(GET, uri = schoolServiceUri).withHeaders(Accept(mediaRanges = List(MediaRanges.`*/*`)))

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )
    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val allSchoolsResponseFuture: Future[HttpResponse] = Http().singleRequest(req, badCtx)

    val allSchoolsEventualFuture: Future[Future[Seq[School]]] = allSchoolsResponseFuture map {
      httpResponse =>
        val eventualSchools = extractSchoolsFromHttpResponse(httpResponse)
        val schoolsConverted = eventualSchools map {
          schoolsInWrapper =>
            val schools = schoolsInWrapper map {
              singleSchoolWrapper =>
                val theLocalAuthority = singleSchoolWrapper.school.localAuthority.toString.toUpperCase.stripPrefix("SCOTLAND__")
                val theCountry = singleSchoolWrapper.school.country.toString.toUpperCase
                School(
                  id = singleSchoolWrapper.school.id,
                  name = singleSchoolWrapper.school.name.replace("'",""),
                  address = singleSchoolWrapper.school.address,
                  postCode = singleSchoolWrapper.school.postCode,
                  telephone = singleSchoolWrapper.school.telephone,
                  localAuthority = LocalAuthority(theLocalAuthority),
                  country = Country(theCountry)
                )
            }
            schools
        }
        schoolsConverted
    }

    allSchoolsEventualFuture.flatMap {
      res => res
    }
  }

  def extractSchoolsFromHttpResponse(httpResponse: HttpResponse): Future[List[SingleSchoolWrapper]] = {
    if (httpResponse.status.isSuccess()) {
      val smallTimeout = 3000.millis
      val dataFuture = httpResponse.entity.toStrict(smallTimeout) map {
        httpEntity =>
          httpEntity.getData()
      }
      val userExtractedFuture = dataFuture map {
        databytes =>
          val bytesAsArray = databytes.toArray
          val schoolsDeserializer = new SchoolsDeserializer
          val schools = schoolsDeserializer.deserialize("ignore", bytesAsArray)
          schools.schools
      }
      userExtractedFuture
    } else {
      logger.warn(s"Issue finding schools : ${httpResponse.toString()}")
      Future {
        List.empty
      }
    }
  }

  //  def getAllSchoolsAsSeqDummy: Seq[School] = {
  //    val stNiniansStirling = School(
  //      id = "id1",
  //      name = "Some School",
  //      address = "Torbrex Rd, Stirling",
  //      postCode = "FK7 9HN",
  //      telephone = "01786 237975",
  //      localAuthority = LocalAuthority("STIRLING"),
  //      country = Country("SCOTLAND")
  //    )
  //
  //    val allansStirling = School(
  //      id = "id2",
  //      name = "Allan's Primary School",
  //      address = "Spittal Street, Stirling",
  //      postCode = "FK8 1DU",
  //      telephone = "01786 474757",
  //      localAuthority = LocalAuthority("STIRLING"),
  //      country = Country("SCOTLAND")
  //    )
  //
  //    val riversideStirling = School(
  //      id = "id3",
  //      name = "Riverside Primary School",
  //      address = "Forrest Road, Stirling",
  //      postCode = "FK8 1UJ",
  //      telephone = "01786 474128",
  //      localAuthority = LocalAuthority("STIRLING"),
  //      country = Country("SCOTLAND")
  //    )
  //
  //    val aStirlingSchool = School(
  //      name = "St. Ninians Primary School",
  //      address = "123 some street",
  //      postCode = "AB1 CD2",
  //      telephone = "0123456789",
  //      localAuthority = LocalAuthority("STIRLING"),
  //      country = Country("SCOTLAND")
  //    )
  //
  //    val aGlasgowSchool = School(
  //      name = "Another School",
  //      address = "123 another street",
  //      postCode = "HS2 PL2",
  //      telephone = "11131517191",
  //      localAuthority = LocalAuthority("GLASGOW"),
  //      country = Country("SCOTLAND")
  //    )
  //
  //    val aNewcastleSchool = School(
  //      name = "One More School",
  //      address = "123 one more street",
  //      postCode = "CS2 KP2",
  //      telephone = "143546317191",
  //      localAuthority = LocalAuthority("NEWCASTLE"),
  //      country = Country("ENGLAND")
  //    )
  //
  //    List(stNiniansStirling, riversideStirling, allansStirling, aGlasgowSchool, aStirlingSchool, aNewcastleSchool)
  //  }

}
