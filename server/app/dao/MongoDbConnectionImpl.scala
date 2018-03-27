package dao

import java.net.URI

import com.mongodb.connection.ClusterSettings
import com.typesafe.config.ConfigFactory
import javax.inject.Singleton
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.connection.{NettyStreamFactoryFactory, SslSettings}
import org.mongodb.scala.{Document, MongoClient, MongoClientSettings, MongoCollection, ServerAddress}
import play.api.Logger
import potentialmicroservice.planning.sharedschema.{TermlyCurriculumSelectionSchema, TermlyPlanningSchema}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Singleton
class MongoDbConnectionImpl extends MongoDbConnection with MiniKubeHelper {

  val logger: Logger = Logger
  private val config = ConfigFactory.load()
  private val mongoDbUriString = config.getString("mongodb.connection_uri")
  private val mongoDbUri = new URI(mongoDbUriString)

  private val planningDatabaseName = config.getString("mongodb.planning-database-name")
  private val termlyPlansCollectionName = config.getString("mongodb.termly-plans-collection-name")
  private val termlyCurriculumSelectionCollectionName = config.getString("mongodb.termly-curriculum-selection-collection-name")

  private val calendarDatabaseName = config.getString("mongodb.calendar-database-name")
  private val schoolTermsCollectionName = config.getString("mongodb.school-terms-collection-name")

  private val systemTimeDatabaseName = config.getString("mongodb.system-time-database-name")
  private val systemDateCollectionName = config.getString("mongodb.system-date-collection-name")

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

  ensureTermlyPlanningIndexes()
  ensureTermlyCurriculumSelectionIndexes()

  override def getTermlyPlanningCollection: MongoCollection[Document] = {
    val planningDatabase = mongoDbClient.getDatabase(planningDatabaseName)
    planningDatabase.getCollection(termlyPlansCollectionName)
  }

  override def getTermlyCurriculumSelectionCollection: MongoCollection[Document] = {
    val planningDatabase = mongoDbClient.getDatabase(planningDatabaseName)
    planningDatabase.getCollection(termlyCurriculumSelectionCollectionName)
  }

  override def getSchoolTermsCollection: MongoCollection[Document] = {
    val planningDatabase = mongoDbClient.getDatabase(calendarDatabaseName)
    planningDatabase.getCollection(schoolTermsCollectionName)
  }

  override def getSystemDateCollection: MongoCollection[Document] = {
    val planningDatabase = mongoDbClient.getDatabase(systemTimeDatabaseName)
    planningDatabase.getCollection(systemDateCollectionName)
  }

  override def ensureTermlyPlanningIndexes(): Unit = {
    val mainIndex = BsonDocument(
      TermlyPlanningSchema.TTT_USER_ID -> 1,
      TermlyPlanningSchema.CLASS_ID -> 1,
      TermlyPlanningSchema.GROUP_ID -> 1,
      TermlyPlanningSchema.CURRICULUM_PLANNING_AREA -> 1
    )
    logger.info(s"Ensuring index created on termly planning collection : ${mainIndex.toString}")
    val obs = getTermlyPlanningCollection.createIndex(mainIndex)
    obs.toFuture().onComplete {
      case Success(msg) => logger.info(s"Ensure termly planning index 1 attempt completed with msg : $msg")
      case Failure(ex) => logger.info(s"Ensure termly planning index 1 failed to complete: ${ex.getMessage}")
    }
    ////
    val secondIndex = BsonDocument(
      TermlyPlanningSchema.TTT_USER_ID -> 1,
      TermlyPlanningSchema.CLASS_ID -> 1
    )
    logger.info(s"Ensuring index created on termly planning collection : ${secondIndex.toString}")
    val obs2 = getTermlyPlanningCollection.createIndex(secondIndex)
    obs2.toFuture().onComplete {
      case Success(msg) => logger.info(s"Ensure termly planning index 2 attempt completed with msg : $msg")
      case Failure(ex) => logger.info(s"Ensure termly planning index 2 failed to complete: ${ex.getMessage}")
    }

  }

  override def ensureTermlyCurriculumSelectionIndexes(): Unit = {
    val mainIndex = BsonDocument(
      TermlyCurriculumSelectionSchema.TTT_USER_ID -> 1,
      TermlyCurriculumSelectionSchema.CLASS_ID -> 1
    )
    logger.info(s"Ensuring index created on termly curriculum selection collection: ${mainIndex.toString}")
    val obs = getTermlyCurriculumSelectionCollection.createIndex(mainIndex)
    obs.toFuture().onComplete {
      case Success(msg) => logger.info(s"Ensure termly curriculum selection index attempt completed with msg : $msg")
      case Failure(ex) => logger.info(s"Ensure termly curriculum selection index failed to complete: ${ex.getMessage}")
    }
  }
}
