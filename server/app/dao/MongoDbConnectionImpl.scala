package dao

import java.net.URI
import javax.inject.Singleton

import com.mongodb.connection.ClusterSettings
import com.typesafe.config.ConfigFactory
import org.mongodb.scala.connection.{NettyStreamFactoryFactory, SslSettings}
import org.mongodb.scala.{Document, MongoClient, MongoClientSettings, MongoCollection, ServerAddress}
import play.api.Logger

import scala.collection.JavaConverters._

@Singleton
class MongoDbConnectionImpl extends MongoDbConnection with MiniKubeHelper {

  val logger: Logger = Logger
  private val config = ConfigFactory.load()
  private val mongoDbUriString = config.getString("mongodb.connection_uri")
  private val mongoDbUri = new URI(mongoDbUriString)

  private val planningDatabaseName = config.getString("mongodb.planning-database-name")
  private val termlyPlanningCollectionName = config.getString("termly-planning-collection-name")

  private val isLocalMongoDb: Boolean = try {
    if (sys.env("LOCAL_MONGO_DB") == "true") true else false
  } catch {
    case e: Exception => false
  }

  private def createMongoClient: MongoClient = {
    if (isLocalMongoDb || isMinikubeRun) {
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

  lazy val mongoDbClient: MongoClient = createMongoClient

  override def getTermlyPlanningCollection: MongoCollection[Document] = {
    val planningDatabase = mongoDbClient.getDatabase(planningDatabaseName)
    planningDatabase.getCollection(termlyPlanningCollectionName )
  }

}
