package potentialmicroservice.planning.reader.dao

import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument}

object PlanReaderDaoHelperTermlyCurriculumSelectionTestHelper
{
  def provideSingleTermlyCurriculumSelectionDocument(): Document =
  {
    createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("5a7e0f39e9ff0d6d4d390e3c", "2018-02-09 21:14:33.347")
  }

  def createAListOfSeveralTermlyCurriculumSelectionDocumentsMixedUp(): List[Document] =
  {
    List(
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("204534664378917127976998", "2018-02-09 21:54:00.341"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("1a7e0f39e9ff0d6d4d390e3c", "2018-02-09 21:33:43.347"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("a21029985165479d9a049551", "2018-02-09 22:14:33.344"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("b38bcf3f4450361f4271b465", "2018-02-09 21:34:00.343"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("9a95defb5aa9a109ed704fea", "2018-02-09 21:17:30.349")
    )
  }

  ///////////////////////////

  private def createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp(id: String, timestamp: String): Document =
  {
    Document(
      "_id" -> id,
      "tttUserId" -> "2045346643789171279756998-1507056447",
      "classId" -> "classId_1b22d43d-5585-47b4-bb41-94f26d3edba5",
      "curriculumPlanningAreas" -> BsonArray(
        "EXPRESSIVE_ARTS__ART",
        "EXPRESSIVE_ARTS__DRAMA",
        "EXPRESSIVE_ARTS__MUSIC",
        "HEALTH_AND_WELLBEING",
        "LITERACY__WRITING"),
      "timestamp" -> timestamp,
      "schoolTerm" -> BsonDocument(
        "schoolYear" -> "2017-2018",
        "schoolTermName" -> "SPRING_SECOND_TERM",
        "schoolTermFirstDay" -> "2018-02-19",
        "schoolTermLastDay" -> "2018-03-29"
      )
    )
  }

}
