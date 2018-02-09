package dao

import java.net.URI
import javax.inject.Singleton

import com.mongodb.connection.ClusterSettings
import com.typesafe.config.ConfigFactory
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.connection.{NettyStreamFactoryFactory, SslSettings}
import org.mongodb.scala.{Document, MongoClient, MongoClientSettings, MongoCollection, ServerAddress}
import play.api.Logger
import potentialmicroservice.planning.schema.TermlyPlanningSchema

import scala.collection.JavaConverters._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MongoDbConnectionImpl extends MongoDbConnection with MiniKubeHelper {

  val logger: Logger = Logger
  private val config = ConfigFactory.load()
  private val mongoDbUriString = config.getString("mongodb.connection_uri")
  private val mongoDbUri = new URI(mongoDbUriString)

  private val planningDatabaseName = config.getString("mongodb.planning-database-name")
  private val termlyPlanningCollectionName = config.getString("mongodb.termly-planning-collection-name")

  private val isLocalMongoDb: Boolean = config.getString("mongodb.localmongodb").toBoolean
  logger.info(s"======================================================== isLocalMongoDb: '$isLocalMongoDb'")


  private def createMongoClient: MongoClient = {
    if (isLocalMongoDb || isMinikubeRun) {
      logger.info("Building local mongo db")
      buildLocalMongoDbClient
    } else {
      logger.info(s"connecting to mongo db at '${mongoDbUri.getHost}:${mongoDbUri.getPort}'")
      System.setProperty("org.mongodb.async.type", "netty")
      MongoClient(mongoDbUriString)
    }
  }

  private def buildLocalMongoDbClient = {
    val mongoKeystorePassword = try {
      sys.env("MONGODB_KEYSTORE_PASSWORD")
    } catch {
      case e: Exception => ""
    }

    val mongoDbHost = mongoDbUri.getHost
    val mongoDbPort = mongoDbUri.getPort
    println(s"mongo host = '$mongoDbHost'")
    println(s"mongo port = '$mongoDbPort'")

    val clusterSettings: ClusterSettings = ClusterSettings.builder().hosts(
      List(new ServerAddress(mongoDbHost, mongoDbPort)).asJava).build()

    val mongoSslClientSettings = MongoClientSettings.builder()
      .sslSettings(SslSettings.builder()
        .enabled(true)
        .invalidHostNameAllowed(true)
        .build())
      .streamFactoryFactory(NettyStreamFactoryFactory())
      .clusterSettings(clusterSettings)
      .build()

    MongoClient(mongoSslClientSettings)
  }

  private lazy val mongoDbClient: MongoClient = createMongoClient

  ensureIndexes()

  override def getTermlyPlanningCollection: MongoCollection[Document] = {
    val planningDatabase = mongoDbClient.getDatabase(planningDatabaseName)
    planningDatabase.getCollection(termlyPlanningCollectionName)
  }

  override def ensureIndexes(): Unit = {
    val mainIndex = BsonDocument(
      TermlyPlanningSchema.TTT_USER_ID -> 1,
      TermlyPlanningSchema.CLASS_ID -> 1,
      TermlyPlanningSchema.GROUP_ID -> 1,
      TermlyPlanningSchema.SUBJECT_NAME -> 1
    )
    logger.info(s"Ensuring index created : ${mainIndex.toString}")
    val obs = getTermlyPlanningCollection.createIndex(mainIndex)
    obs.toFuture().onComplete {
      case Success(msg) => logger.info(s"Ensure index attempt completed with msg : $msg")
      case Failure(ex) => logger.info(s"Ensure index failed to complete: ${ex.getMessage}")
    }
  }
}
