package potentialmicroservice.planning.reader.dao

import org.mongodb.scala.bson.collection.immutable.Document
import potentialmicroservice.planning.sharedschema.EsAndOsStatusSchema
import potentialmicroservice.planning.writer.dao.PlanWriterDaoTermlyCurriculumSelectionHelper

trait RetrieveFullWeekOfLessonsDaoHelper2TestCanned extends PlanWriterDaoTermlyCurriculumSelectionHelper {

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private[dao] val TEST_USER1 = "user1"
  private[dao] val TEST_USER2 = "user2"
  private[dao] val TEST_CLASSA = "classA"
  private[dao] val TEST_CLASSB = "classB"
  private[dao] val TEST_SUBJECT_MATHS = "MATHEMATICS"
  private[dao] val TEST_SUBJECT_ART = "EXPRESSIVE_ARTS__ART"

  private[dao] val TEST_TIMESTAMP_WEEK1_A = "2018-04-27 14:24:36.185"
  private[dao] val TEST_TIMESTAMP_WEEK1_B = "2018-04-27 14:33:13.222"
  private[dao] val TEST_TIMESTAMP_WEEK1_C = "2018-04-27 15:54:56.886"
  private[dao] val TEST_TIMESTAMP_WEEK1_D = "2018-04-28 09:11:39.982"
  private[dao] val TEST_TIMESTAMP_WEEK1_E = "2018-04-29 11:02:08.495"

  private[dao] val VALUE_TYPE_EANDO = "EANDO"
  private[dao] val VALUE_TYPE_BENCHMARK = "BENCHMARK"

  private[dao] val STATUS_NOT_STARTED = "NOT_STARTED"
  private[dao] val STATUS_COMPLETE = "COMPLETE"

  private[dao] val GROUP_ID_CLASS = "CLASS_LEVEL"
  private[dao] val GROUP_ID_MATHS1 = "maths1"
  private[dao] val GROUP_ID_MATHS2 = "maths2"
  private[dao] val GROUP_ID_MATHS3 = "maths3"

  private[dao] val TEST_ART_SECTION_NAME = "Art and design"
  private[dao] val TEST_ART_SUBSECTION_NAME = "NO_SUBSECTION_NAME"
  private[dao] val TEST_MATHS_SECTION_NUMBER_MONEY_MEASURE = "Number, money and measure"
  private[dao] val TEST_MATHS_SUBSECTION_MONEY = "Money"
  private[dao] val TEST_MATHS_SUBSECTION_SHAPES = "Shapes"

  private[dao] val TEST_ART_EANDO_VALUE1 = "ART1"
  private[dao] val TEST_ART_EANDO_VALUE2 = "ART2"
  private[dao] val TEST_ART_EANDO_VALUE3 = "ART3"
  private[dao] val TEST_ART_BENCHMARK_VALUE1 = "ART_BENCHMARK_1"
  private[dao] val TEST_ART_BENCHMARK_VALUE2 = "ART_BENCHMARK_2"
  private[dao] val TEST_ART_BENCHMARK_VALUE3 = "ART_BENCHMARK_3"
  private[dao] val TEST_ART_BENCHMARK_VALUE4 = "ART_BENCHMARK_4"

  private[dao] val TEST_MATHS_EANDO_VALUE1 = "EXA 1-02a"
  private[dao] val TEST_MATHS_EANDO_VALUE2 = "EXA 1-03a"
  private[dao] val TEST_MATHS_BENCHMARK_VALUE1 = "EXA 1-03a"
  ///////////////////////////////////////////////////////////////////////////////////


  private[dao] def create3ArtEAndOsStatusesWithVariousVersions(): List[Document] = {

    val art2Vals =  createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_B,
      VALUE_TYPE_EANDO, STATUS_NOT_STARTED, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE2
    ) :: createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_A,
      VALUE_TYPE_EANDO, STATUS_COMPLETE, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE2
    ) :: Nil

    val art3Vals =  createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_C,
      VALUE_TYPE_EANDO, STATUS_COMPLETE, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE3
    ) :: createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_B,
      VALUE_TYPE_EANDO, STATUS_NOT_STARTED, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE3
    ) :: createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_E,
      VALUE_TYPE_EANDO, STATUS_COMPLETE, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE3
    ) :: Nil

    create1ArtEAndOsWith5StatusesLatestComplete() ::: art2Vals ::: art3Vals
  }

  private[dao] def create1ArtEAndOsWith5StatusesLatestComplete(): List[Document] = {
    createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_A,
      VALUE_TYPE_EANDO, STATUS_COMPLETE, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE1
    ) :: createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_D,
      VALUE_TYPE_EANDO, STATUS_NOT_STARTED, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE1
    ) :: createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_E,
      VALUE_TYPE_EANDO, STATUS_COMPLETE, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE1
    ) :: createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_B,
      VALUE_TYPE_EANDO, STATUS_NOT_STARTED, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE1
    ) :: createEAndOBenchmarksStatusDoc(TEST_USER1, TEST_CLASSA, TEST_SUBJECT_ART, TEST_TIMESTAMP_WEEK1_C,
      VALUE_TYPE_EANDO, STATUS_COMPLETE, GROUP_ID_CLASS, TEST_ART_SECTION_NAME, TEST_ART_SUBSECTION_NAME,
      TEST_ART_EANDO_VALUE1
    ) :: Nil
  }

  private[dao] def createEAndOBenchmarksStatusDoc(
                                                   userId: String,
                                                   classId: String,
                                                   subject: String,
                                                   timestamp: String,
                                                   valueType: String,
                                                   status: String,
                                                   groupId: String,
                                                   section: String,
                                                   subsection: String,
                                                   value: String
                                                 ): Document = {
    Document(
      EsAndOsStatusSchema._ID -> java.util.UUID.randomUUID().toString,
      EsAndOsStatusSchema.TTT_USER_ID -> userId,
      EsAndOsStatusSchema.CLASS_ID -> classId,
      EsAndOsStatusSchema.SUBJECT -> subject,
      EsAndOsStatusSchema.CREATED_TIMESTAMP -> timestamp,
      EsAndOsStatusSchema.VALUE_TYPE -> valueType,
      EsAndOsStatusSchema.STATUS -> status,
      EsAndOsStatusSchema.GROUP_ID -> groupId,
      EsAndOsStatusSchema.SECTION -> section,
      EsAndOsStatusSchema.SUBSECTION -> subsection,
      EsAndOsStatusSchema.VALUE -> value
    )
  }

  //  {
  //    "_id" : ObjectId("5b100240f72a356ff9af80a1"),
  //    "tttUserId" : "user_204534664378917210805202512416869",
  //    "classId" : "classId_09704919-b57b-40a8-9ab5-a8dfa8e9b265",
  //    "subject" : "EXPRESSIVE_ARTS__ART",
  //    "timestamp" : "2018-05-31 15:10:08.550",
  //    "valueType" : "EANDO",
  //    "status" : "NOT_STARTED",
  //    "groupId" : "CLASS_LEVEL",
  //    "section" : "Art and design",
  //    "subsection" : "NO_SUBSECTION_NAME",
  //    "value" : "EXA 1-02a"
  //  }


}
