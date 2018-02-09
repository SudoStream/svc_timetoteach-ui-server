package potentialmicroservice.planning.dao.schema

object TermlyPlanningSchema {
  val _ID = "_id"
  val TTT_USER_ID = "tttUserId"
  val SCHOOL_ID = "schoolId"
  val PLAN_TYPE = "planType"
  val GROUP_ID = "groupId"
  val SUBJECT_NAME = "subjectName"
  val CREATED_TIMESTAMP = "timestamp"
  val SELECTED_ES_AND_OS = "selectedEsAndOs"
  val SELECTED_BENCHMARKS = "selectedBenchmarks"

  /// School Term
  val SCHOOL_YEAR = "schoolYear"
  val SCHOOL_TERM_NAME = "schoolTermName"
  val SCHOOL_TERM_FIRST_DAY = "schoolTermFirstDay"
  val SCHOOL_TERM_LAST_DAY = "schoolTermLastDay"
}
