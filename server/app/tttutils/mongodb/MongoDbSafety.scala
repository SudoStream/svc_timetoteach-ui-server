package tttutils.mongodb

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.mongodb.scala.Document
import play.api.Logger

object MongoDbSafety
{
  val logger: Logger = Logger

  def safelyGetString(document: Document, key: String): Option[String] =
  {
    try {
      Some(document.getString(key))
    } catch {
      case ex: Exception =>
        logger.warn(s"Issue getting key($key) from document: ${document.toString()}." +
          s" Exception: ${ex.getMessage}")
        None
    }
  }

  def safelyGetStringNoneIfBlank(document: Document, key: String): Option[String] =
  {
    safelyGetString(document, key) match {
      case Some(value) =>
        if (value == null || value.isEmpty) {
          None
        } else {
          Some(value)
        }
      case None => None
    }
  }

  def safelyParseTimestamp(nextTimestampIso: String): LocalDateTime =
  {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    val formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    val time = try {
      LocalDateTime.parse(nextTimestampIso, formatter)
    } catch {
      case e: Throwable =>
        logger.warn(s"Failed to parse assumed fomat. ${formatter.toString}, trying ${formatter2.toString}")
        try {
          LocalDateTime.parse(nextTimestampIso, formatter2)
        } catch {
          case e2: Throwable =>
            logger.warn(s"Failed to parse assumed fomat. ${formatter.toString}, trying ${formatter3.toString}")
            LocalDateTime.parse(nextTimestampIso, formatter3)
        }
    }
//    logger.debug(s"Time parsed = '${time.toString}'")
    time
  }


}
