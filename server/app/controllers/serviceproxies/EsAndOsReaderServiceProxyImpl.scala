package controllers.serviceproxies

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.inject.Singleton
import com.typesafe.config.ConfigFactory
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import io.sudostream.timetoteach.kafka.serializing.{ScottishBenchmarksDataDeserializer, ScottishEsAndOsDataDeserializer}
import io.sudostream.timetoteach.messages.scottish.{ScottishBenchmarksData, ScottishEsAndOsData}
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


@Singleton
class EsAndOsReaderServiceProxyImpl {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5 seconds)
  val logger: Logger.type = Logger
  private val config = ConfigFactory.load()
  val http = Http(system)
  private val esAndOsReaderServiceHostname = config.getString("services.es-and-os-reader-host")
  private val esAndOsReaderServicePort = config.getString("services.es-and-os-reader-port")
  private val minikubeRun: Boolean = sys.props.getOrElse("minikubeEnv", "false").toBoolean
  val protocol: String = if (esAndOsReaderServicePort.toInt > 9000 && !minikubeRun) "http" else "https"


  def readAllEsAndos(): Future[Option[ScottishEsAndOsData]] = {
    logger.debug(s"read all experiences and outcomes")

    val uriString =
      s"$protocol://$esAndOsReaderServiceHostname:$esAndOsReaderServicePort/api/esandos"
    logger.debug(s"uri for reading all es and os is $uriString")

    val esAndOsReaderServiceUri = Uri(uriString)
    val esAndOsHttpRequest = HttpRequest(HttpMethods.GET, uri = esAndOsReaderServiceUri)

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )

    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val response: Future[HttpResponse] = http.singleRequest(esAndOsHttpRequest, badCtx)

    response onComplete {
      case Success(httpResponse) =>
        logger.debug("Es And Os bytes received")

      case Failure(ex) => logger.error(s"Issue receiving the es and os: ${ex.getMessage}")
    }

    val eventualFutureEsAndOs = response map { httpResponse =>
      extractClassTimeTableFromHttpResponse(httpResponse)
    }

    eventualFutureEsAndOs flatMap {
      res => res
    }
  }

  private def extractClassTimeTableFromHttpResponse(httpResponse: HttpResponse): Future[Option[ScottishEsAndOsData]] = {
    if (httpResponse.status.isSuccess()) {
      val smallTimeout = 3000.millis
      val dataFuture = httpResponse.entity.toStrict(smallTimeout) map {
        httpEntity =>
          httpEntity.getData()
      }
      val futureEsAndOs = dataFuture map {
        databytes =>
          val bytesAsArray = databytes.toArray
          val esAndOsDeserializer = new ScottishEsAndOsDataDeserializer
          esAndOsDeserializer.deserialize("ignore", bytesAsArray)
      }

      futureEsAndOs map {
        scottishEsAndOsData =>
          Some(scottishEsAndOsData)
      }
    } else {
      logger.warn(s"Issue finding es and os : ${httpResponse.toString()}")
      Future {
        None
      }
    }
  }


  def readAllBenchmarks(): Future[Option[ScottishBenchmarksData]] = {
    logger.debug(s"read all benchmarks")

    val uriString =
      s"$protocol://$esAndOsReaderServiceHostname:$esAndOsReaderServicePort/api/benchmarks"
    logger.debug(s"uri for reading all benchmarks is $uriString")

    val benchmarksUri = Uri(uriString)
    val allBenchmarksHttpRequest = HttpRequest(HttpMethods.GET, uri = benchmarksUri)

    val badSslConfig = AkkaSSLConfig().mapSettings(s =>
      s.withLoose(s.loose.withDisableSNI(true))
        .withLoose(s.loose.withDisableHostnameVerification(true))
        .withLoose(s.loose.withAcceptAnyCertificate(true))
    )

    logger.info(s"ssl config = ${badSslConfig.toString}")
    val badCtx = Http().createClientHttpsContext(badSslConfig)

    val response: Future[HttpResponse] = http.singleRequest(allBenchmarksHttpRequest, badCtx)

    response onComplete {
      case Success(httpResponse) =>
        logger.debug("All Benchmarks bytes received")

      case Failure(ex) => logger.error(s"Issue receiving the benchmarks: ${ex.getMessage}")
    }

    val eventualFutureEsAndOs = response map { httpResponse =>
      extractBenchmarksFromHttpResponse(httpResponse)
    }

    eventualFutureEsAndOs flatMap {
      res => res
    }
  }

  private def extractBenchmarksFromHttpResponse(httpResponse: HttpResponse): Future[Option[ScottishBenchmarksData]] = {
    if (httpResponse.status.isSuccess()) {
      val smallTimeout = 3000.millis
      val dataFuture = httpResponse.entity.toStrict(smallTimeout) map {
        httpEntity =>
          httpEntity.getData()
      }
      val futureBenchmarks = dataFuture map {
        databytes =>
          val bytesAsArray = databytes.toArray
          val esAndOsDeserializer = new ScottishBenchmarksDataDeserializer
          esAndOsDeserializer.deserialize("ignore", bytesAsArray)
      }

      futureBenchmarks map {
        scottishBenchmarksData =>
          Some(scottishBenchmarksData)
      }
    } else {
      logger.warn(s"Issue finding benchmarks : ${httpResponse.toString()}")
      Future {
        None
      }
    }
  }


}
