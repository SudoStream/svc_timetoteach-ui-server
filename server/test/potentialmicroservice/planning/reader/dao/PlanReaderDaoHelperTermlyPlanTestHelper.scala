package potentialmicroservice.planning.reader.dao

import duplicate.model._
import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumPlanningArea
import org.mongodb.scala.Document
import org.mongodb.scala.bson.{BsonArray, BsonDocument}

object PlanReaderDaoHelperTermlyPlanTestHelper
{
  def provideSingleTermlyPlanDocument(): Document =
  {
    createTermlyPlanMongoDocumentWithIdAndTimestamp("5a7e0f39e9ff0d6d4d390e3c", "2018-02-09 21:14:33.347", Some("groupId_f842e787-cc90-483f-a321-49b68a252a80"))
  }

  def createAListOfSeveralTermlyPlanDocumentsMixedUp(): List[Document] =
  {
    List(
      createTermlyPlanMongoDocumentWithIdAndTimestamp("204534664378917127976998", "2018-02-09 21:54:00.341", Some("groupId_f842e787-cc90-483f-a321-49b68a252a80")),
      createTermlyPlanMongoDocumentWithIdAndTimestamp("1a7e0f39e9ff0d6d4d390e3c", "2018-02-09 21:33:43.347", Some("groupId_f842e787-cc90-483f-a321-49b68a252a80")),
      createTermlyPlanMongoDocumentWithIdAndTimestamp("a21029985165479d9a049551", "2018-02-09 22:14:33.344", Some("groupId_f842e787-cc90-483f-a321-49b68a252a80")),
      createTermlyPlanMongoDocumentWithIdAndTimestamp("b38bcf3f4450361f4271b465", "2018-02-09 21:34:00.343", Some("groupId_f842e787-cc90-483f-a321-49b68a252a80")),
      createTermlyPlanMongoDocumentWithIdAndTimestamp("9a95defb5aa9a109ed704fea", "2018-02-09 21:17:30.349", Some("groupId_f842e787-cc90-483f-a321-49b68a252a80"))
    )
  }

  def createAListOfSeveralTermlyPlanDocumentsWithGroupsMixedUpWithDifferentCurriculumPlanningAreas(): List[Document] =
    List(
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "204534664378917127976998",
        timestamp = "2018-02-09 21:54:00.341",
        maybeGroupId = Some("groupId_f842e787-cc90-483f-a321-49b68a252a80"),
        curriculumPlanningArea = "MATHEMATICS"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "1a7e0f39e9ff0d6d4d390e3c",
        timestamp = "2018-02-09 21:33:43.347",
        maybeGroupId = Some("groupId_f842e787-cc90-483f-a321-49b68a252a80"),
        curriculumPlanningArea = "MATHEMATICS"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "a21029985165479d9a049551",
        timestamp = "2018-02-09 22:14:33.344",
        maybeGroupId = Some("groupId_f842e787-cc90-483f-a321-49b68a252a80"),
        curriculumPlanningArea = "MATHEMATICS"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "b38bcf3f4450361f4271b465",
        timestamp = "2018-02-09 21:34:00.343",
        maybeGroupId = Some("groupId_f842e787-cc90-483f-a321-49b68a252a80"),
        curriculumPlanningArea = "MATHEMATICS"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "9a95defb5aa9a109ed704fea",
        timestamp = "2018-02-09 21:17:30.349",
        maybeGroupId = Some("groupId_f842e787-cc90-483f-a321-49b68a252a80"),
        curriculumPlanningArea = "MATHEMATICS"
      ),

      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "y785defb5aa9a109ed704fy78",
        timestamp = "2018-02-08 11:24:00.756",
        maybeGroupId = None,
        curriculumPlanningArea = "EXPRESSIVE_ARTS__ART"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "z785defb5aa9a109ed704fy7z",
        timestamp = "2018-02-09 21:54:00.341",
        maybeGroupId = None,
        curriculumPlanningArea = "EXPRESSIVE_ARTS__ART"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "w785defb5aa9a109ed704fy7w",
        timestamp = "2018-02-07 10:23:34.823",
        maybeGroupId = None,
        curriculumPlanningArea = "EXPRESSIVE_ARTS__ART"
      ),


      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "h32pdefb5a9a109ed704la67",
        timestamp = "2018-02-09 21:17:30.349",
        maybeGroupId = Some("groupId_writing_one"),
        curriculumPlanningArea = "LITERACY__WRITING"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "123pdefb5a9a109ed704l321",
        timestamp = "2018-02-13 21:17:30.349",
        maybeGroupId = Some("groupId_writing_one"),
        curriculumPlanningArea = "LITERACY__WRITING"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "444pdefb5a9a109ed704l444",
        timestamp = "2018-02-05 21:17:30.349",
        maybeGroupId = Some("groupId_writing_one"),
        curriculumPlanningArea = "LITERACY__WRITING"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "qq2pdefb5a9a109ed7040000",
        timestamp = "2018-02-09 21:17:30.349",
        maybeGroupId = Some("groupId_writing_two"),
        curriculumPlanningArea = "LITERACY__WRITING"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "qq2pdefb5a9a109ed7041111",
        timestamp = "2018-02-10 21:17:30.349",
        maybeGroupId = Some("groupId_writing_two"),
        curriculumPlanningArea = "LITERACY__WRITING"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "qq2pdefb5a9a109ed7042222",
        timestamp = "2018-02-11 21:17:30.349",
        maybeGroupId = Some("groupId_writing_two"),
        curriculumPlanningArea = "LITERACY__WRITING"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "ee2pdefb5a9a109ed704laee",
        timestamp = "2018-02-09 21:17:30.349",
        maybeGroupId = Some("groupId_writing_three"),
        curriculumPlanningArea = "LITERACY__WRITING"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "1111defb5a9a109ed704laee",
        timestamp = "2018-02-08 21:17:30.349",
        maybeGroupId = Some("groupId_writing_three"),
        curriculumPlanningArea = "LITERACY__WRITING"
      ),
      createTermlyPlanMongoDocumentWithIdAndTimestamp(
        id = "8888defb5a9a109ed704laee",
        timestamp = "2018-02-07 21:17:30.349",
        maybeGroupId = Some("groupId_writing_three"),
        curriculumPlanningArea = "LITERACY__WRITING"
      )


    )


  ///////////////////////////

  private def createTermlyPlanMongoDocumentWithIdAndTimestamp(id: String,
                                                              timestamp: String,
                                                              maybeGroupId: Option[String],
                                                              curriculumPlanningArea: String = "MATHEMATICS"): Document =
  {
    val planType = if (maybeGroupId.isDefined) "GROUP_LEVEL_PLAN" else "CLASS_LEVEL_PLAN"
    val groupId = maybeGroupId.getOrElse("")

    Document(
      "_id" -> id,
      "tttUserId" -> "2045346643789171279756998-1507056447",
      "classId" -> "classId_1b22d43d-5585-47b4-bb41-94f26d3edba5",
      "planType" -> planType,
      "groupId" -> groupId,
      "curriculumPlanningArea" -> curriculumPlanningArea,
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

  def createScottishCurriculumPlanningAreaList(): List[ScottishCurriculumPlanningArea] =
  {
    ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__DRAMA ::
    ScottishCurriculumPlanningArea.EXPRESSIVE_ARTS__ART ::
      ScottishCurriculumPlanningArea.MATHEMATICS ::
      ScottishCurriculumPlanningArea.LITERACY__WRITING :: Nil
  }

  def createClassGroupsList(): List[Group] =
  {
    Group(
      GroupId("groupId_f842e787-cc90-483f-a321-49b68a252a80"),
      GroupName("Triangles"),
      GroupDescription(""),
      MathsGroupType,
      EarlyLevel
    ) ::
      Group(
        GroupId("groupId_squares"),
        GroupName("Squares"),
        GroupDescription(""),
        MathsGroupType,
        FirstLevel
      ) ::
      Group(
        GroupId("groupId_circles"),
        GroupName("Circles"),
        GroupDescription(""),
        MathsGroupType,
        EarlyLevel
      ) ::
      Group(
        GroupId("groupId_writing_one"),
        GroupName("Shakeys"),
        GroupDescription(""),
        WritingGroupType,
        EarlyLevel
      ) ::
      Group(
        GroupId("groupId_writing_two"),
        GroupName("Kafkas"),
        GroupDescription(""),
        WritingGroupType,
        FirstLevel
      ) ::
      Group(
        GroupId("groupId_writing_three"),
        GroupName("Orwells"),
        GroupDescription(""),
        WritingGroupType,
        EarlyLevel
      ) :: Nil
  }

}
