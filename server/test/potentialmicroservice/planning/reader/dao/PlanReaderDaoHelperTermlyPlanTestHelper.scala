package potentialmicroservice.planning.reader.dao

import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument}

object PlanReaderDaoHelperTermlyPlanTestHelper
{

  def createTermlyPlanMongoDocumentWithIdAndTimestamp(id: String, timestamp: String): Document =
  {
    Document(
      "_id" -> id,
      "tttUserId" -> "2045346643789171279756998-1507056447",
      "classId" -> "classId_1b22d43d-5585-47b4-bb41-94f26d3edba5",
      "planType" -> "GROUP_LEVEL_PLAN",
      "groupId" -> "groupId_f842e787-cc90-483f-a321-49b68a252a80",
      "curriculumPlanningArea" -> "MATHEMATICS",
      "timestamp" -> timestamp,
      "schoolTerm" -> BsonDocument(
        "schoolYear" -> "2017-2018",
        "schoolTermName" -> "SPRING_SECOND_TERM",
        "schoolTermFirstDay" -> "2018-02-19",
        "schoolTermLastDay" -> "2018-03-29"
      ),
      "selectedEsAndOsWithBenchmarks" ->
        BsonArray(
          BsonDocument(
            "selectedEsAndOs" -> BsonArray(
              "MNU 0-11a"
            ),
            "selectedBenchmarks" -> BsonArray(
              "Shares relevant experiences in which measurements of lengths, heights, mass and capacities are used, for example, in baking.",
              "Compares and describes lengths, heights, mass and capacities using everyday language, including longer, shorter, taller, heavier, lighter, more and less.",
              "Describes common objects using appropriate measurement language, including tall, heavy and empty.",
              "Estimates, then measures, the length, height, mass and capacity of familiar objects using a range of appropriate non-standard units"
            )
          ),
          BsonDocument(
            "selectedEsAndOs" -> BsonArray(
              "MNU 0-07a"
            ),
            "selectedBenchmarks" -> BsonArray(
              "Shares out a group of items equally into smaller groups"
              ,
              "Splits a whole into smaller parts and explains that equal parts are the same size."
              ,
              "Uses appropriate vocabulary to describe halves"
            )
          ),
          BsonDocument(
            "selectedEsAndOs" -> BsonArray(
              "MNU 0-09a"
            ),
            "selectedBenchmarks" -> BsonArray(
              "Identifies all coins to £2",
              "Applies addition and subtraction skills and uses 1p, 2p, 5p and 10p coins to pay the exact value for items to 10p"
            )
          )
        )
    )
  }

  def provideSingleTermlyPlanDocument(): Document =
  {
    createTermlyPlanMongoDocumentWithIdAndTimestamp("5a7e0f39e9ff0d6d4d390e3c", "2018-02-09 21:14:33.347")
  }

  def createAListOfSeveralTermlyPlanDocumentsMixedUp(): List[Document] =
  {
    List(
      createTermlyPlanMongoDocumentWithIdAndTimestamp("204534664378917127976998", "2018-02-09 21:54:00.341"),
      createTermlyPlanMongoDocumentWithIdAndTimestamp("1a7e0f39e9ff0d6d4d390e3c", "2018-02-09 21:33:43.347"),
      createTermlyPlanMongoDocumentWithIdAndTimestamp("a21029985165479d9a049551", "2018-02-09 22:14:33.344"),
      createTermlyPlanMongoDocumentWithIdAndTimestamp("b38bcf3f4450361f4271b465", "2018-02-09 21:34:00.343"),
      createTermlyPlanMongoDocumentWithIdAndTimestamp("9a95defb5aa9a109ed704fea", "2018-02-09 21:17:30.349")
    )
  }

}
