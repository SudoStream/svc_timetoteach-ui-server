package potentialmicroservice.planning.sharedschema

object WeeklyPlanningSchema {
  val _ID = "_id"
  val TTT_USER_ID = "tttUserId"
  val CLASS_ID = "classId"
  val SUBJECT = "subject"
  val WEEK_BEGINNING_ISO_DATE = "weekBeginningIsoDate"
  val CREATED_TIMESTAMP = "timestamp"

  // ES & OS and Benchmarks for the week
  val GROUP_ID = "groupId"

  val SELECTED_ES_OS_AND_BENCHMARKS_BY_GROUP = "selectedEsOsBenchmarksByGroup"
  val SELECTED_ES_AND_OS_WITH_BENCHMARKS = "selectedEsAndOsWithBenchmarks"
  val SELECTED_SECTION_NAME = "selectedSectionName"
  val SELECTED_SUBSECTION_NAME = "selectedSubsectionName"
  val SELECTED_ES_AND_OS = "selectedEsAndOs"
  val SELECTED_BENCHMARKS = "selectedBenchmarks"

  val COMPLETED_ES_OS_AND_BENCHMARKS_BY_GROUP = "completedEsOsBenchmarksByGroup"
  val COMPLETED_ES_AND_OS_WITH_BENCHMARKS = "selectedEsAndOsWithBenchmarks"
  val COMPLETED_SECTION_NAME = "selectedSectionName"
  val COMPLETED_SUBSECTION_NAME = "selectedSubsectionName"
  val COMPLETED_ES_AND_OS = "selectedEsAndOs"
  val COMPLETED_BENCHMARKS = "selectedBenchmarks"
}
