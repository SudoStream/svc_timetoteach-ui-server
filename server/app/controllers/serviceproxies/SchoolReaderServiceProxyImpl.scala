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
import io.sudostream.timetoteach.kafka.serializing.systemwide.model.SchoolsDeserializer
import io.sudostream.timetoteach.messages.systemwide.model.SingleSchoolWrapper
import models.timetoteach.{Country, LocalAuthority, School}
import play.api.Logger
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class SchoolReaderServiceProxyImpl  @Inject()(ws: WSClient) {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)

  private val config = ConfigFactory.load()
  private val schoolReaderServiceHostname = config.getString("services.school-reader-service-host")
  private val schoolReaderServicePort = config.getString("services.school-reader-service-port")

  val logger: Logger.type = Logger

  def getAllSchoolsFuture: Future[Seq[School]] = {
    val protocol = if (schoolReaderServicePort.toInt > 9000) "http" else "https"
    val uriString = s"$protocol://$schoolReaderServiceHostname:$schoolReaderServicePort/api/schools"
    logger.debug(s"uri string is $uriString")
    val schoolServiceUri = Uri(uriString)

    //// andy
    val request: WSRequest = ws.url(schoolServiceUri.toString())
    logger.debug(s"Howdy doodly doo, request = ${request.toString}")
    val resp = request.get()
    import scala.util.{Success, Failure}
    resp onComplete {
      case Success(wsResponse) => logger.info(s"success: let us say ..... WOOHOOO ${wsResponse.toString}")
      case Failure(t) => logger.error(s"failure: ${t.getMessage}")
    }
    //// andy



    val req = HttpRequest(GET, uri = schoolServiceUri).withHeaders(Accept(mediaRanges = List(MediaRanges.`*/*`)))
    val allSchoolsResponseFuture: Future[HttpResponse] = Http().singleRequest(req)
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
                  name = singleSchoolWrapper.school.name,
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
