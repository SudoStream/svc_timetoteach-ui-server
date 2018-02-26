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

  def createAListOfSeveralTermlyCurriculumSelectionDocumentsMixedUpWithDifferentClassIds(): List[Document] =
  {
    List(
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("204534664378917127976998", "2018-02-22 21:54:00.341", "classId1"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("1a7e0f39e9ff0d6d4d390e3c", "2018-02-20 21:33:43.347", "classId1"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("a21029985165479d9a049551", "2018-02-18 22:14:33.344", "classId1"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("b38bcf3f4450361f4271b465", "2018-02-16 21:34:00.343", "classId1"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("9a95defb5aa9a109ed704fea", "2018-02-10 21:17:30.349", "classId1"),

      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("123534664378917127976321", "2018-02-10 21:54:00.341", "classId2"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("222e0f39e9ff0d6d4d390222", "2018-02-10 21:33:43.347", "classId2"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("111029985165479d9a049511", "2018-02-12 22:14:33.344", "classId2"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("448bcf3f4450361f4271b444", "2018-02-17 21:34:00.343", "classId2"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("555defb5aa9a109ed704f555", "2018-02-15 21:17:30.349", "classId2"),

      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("678534664378917127976867", "2018-02-04 21:54:00.341", "classId3"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("888e0f39e9ff0d6d4d390888", "2018-02-01 21:33:43.347", "classId3"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("777029985165479d9a049777", "2018-02-19 22:14:33.344", "classId3"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("000bcf3f4450361f4271b000", "2018-02-21 21:34:00.343", "classId3"),
      createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp("1010defb5aa9a109ed701010", "2018-02-29 21:17:30.349", "classId3")
    )
  }

  ///////////////////////////

  private def createTermlyCurriculumSelectionMongoDocumentWithIdAndTimestamp(id: String, timestamp: String, classId: String = "classId_1b22d43d-5585-47b4-bb41-94f26d3edba5"): Document =
  {
    Document(
      "_id" -> id,
      "tttUserId" -> "2045346643789171279756998-1507056447",
      "classId" -> classId,
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
