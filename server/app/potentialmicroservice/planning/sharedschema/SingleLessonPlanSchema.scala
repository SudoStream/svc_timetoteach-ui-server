package potentialmicroservice.planning.sharedschema

object SingleLessonPlanSchema {
  val _ID = "_id"
  val TTT_USER_ID = "tttUserId"
  val CLASS_ID = "classId"
  val SUBJECT = "subject"
  val SUBJECT_ADDITIONAL_INFO = "subjectAdditionalInfo"
  val WEEK_BEGINNING_ISO_DATE = "weekBeginningIsoDate"
  val LESSON_DATE = "lessonDateIso"
  val LESSON_START_TIME = "lessonStartTimeIso"
  val LESSON_END_TIME = "lessonEndTimeIso"
  val CREATED_TIMESTAMP = "timestamp"

  ////////////

  val GROUP_ID = "groupId"

  val ACTIVITIES_PER_GROUP = "activitiesPerGroup"
  val ACTIVITY = "activity"

  val RESOURCES = "resources"
  val RESOURCE = "resource"

  val LEARNING_INTENTIONS_PER_GROUP = "learningIntentionsPerGroup"
  val LEARNING_INTENTION = "learningIntention"

  val SUCCESS_CRITERIA_PER_GROUP = "successCriteriaPerGroup"
  val SUCCESS_CRITERIA = "successCriteria"

  val PLENARIES = "plenaries"
  val PLENARY = "plenary"

  val FORMATIVE_ASSESSMENT_PER_GROUP = "formativeAssessmentPerGroup"
  val FORMATIVE_ASSESSMENT = "formativeAssessment"

  val NOTES_BEFORE = "notesBefore"
  val NOTES_AFTER = "notesAfter"
  val NOTE = "note"

}
