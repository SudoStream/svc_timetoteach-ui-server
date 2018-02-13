package utils.mongodb

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

}
