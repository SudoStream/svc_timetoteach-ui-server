package shared.util

trait ClassTimeTableUtilTestHelper {

  def createClassTimetableAsJson() : String = {
    """
      |{
      |    "schoolTimes": [
      |        {"sessionBoundaryName": "lunch-starts", "boundaryStartTime": "12:05"},
      |        {"sessionBoundaryName": "lunch-ends", "boundaryStartTime": "13:05"},
      |        {"sessionBoundaryName": "morning-break-ends", "boundaryStartTime": "10:50"},
      |        {"sessionBoundaryName": "morning-break-starts", "boundaryStartTime": "10:35"},
      |        {"sessionBoundaryName": "school-day-ends", "boundaryStartTime": "15:05"},
      |        {"sessionBoundaryName": "school-day-starts", "boundaryStartTime": "09:05"}
      |    ],
      |    "allSessionsOfTheWeek": [
      |        {
      |            "dayOfTheWeek": "Wednesday",
      |            "sessions": [
      |                {
      |                    "sessionOfTheWeek": "wednesday-early-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "09:05",
      |                    "endTimeIso8601": "10:35",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "wednesday-late-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "10:50",
      |                    "endTimeIso8601": "12:05",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "wednesday-afternoon-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "13:05",
      |                    "endTimeIso8601": "15:05",
      |                    "lessonAdditionalInfo": ""
      |                }
      |            ]
      |        },
      |        {
      |            "dayOfTheWeek": "Thursday",
      |            "sessions": [
      |                {
      |                    "sessionOfTheWeek": "thursday-early-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "09:05",
      |                    "endTimeIso8601": "10:35",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "thursday-late-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "10:50",
      |                    "endTimeIso8601": "12:05",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "thursday-afternoon-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "13:05",
      |                    "endTimeIso8601": "15:05",
      |                    "lessonAdditionalInfo": ""
      |                }
      |            ]
      |        },
      |        {
      |            "dayOfTheWeek": "Tuesday",
      |            "sessions": [
      |                {
      |                    "sessionOfTheWeek": "tuesday-early-morning-session",
      |                    "subject": "subject-science",
      |                    "startTimeIso8601": "09:05",
      |                    "endTimeIso8601": "09:50",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-early-morning-session",
      |                    "subject": "subject-art",
      |                    "startTimeIso8601": "09:50",
      |                    "endTimeIso8601": "10:35",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-late-morning-session",
      |                    "subject": "subject-writing",
      |                    "startTimeIso8601": "10:50",
      |                    "endTimeIso8601": "11:30",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-late-morning-session",
      |                    "subject": "subject-maths",
      |                    "startTimeIso8601": "11:30",
      |                    "endTimeIso8601": "12:05",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-afternoon-session",
      |                    "subject": "subject-art",
      |                    "startTimeIso8601": "13:05",
      |                    "endTimeIso8601": "14:05",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-afternoon-session",
      |                    "subject": "subject-geography",
      |                    "startTimeIso8601": "14:05",
      |                    "endTimeIso8601": "15:05",
      |                    "lessonAdditionalInfo": ""
      |                }
      |            ]
      |        },
      |        {
      |            "dayOfTheWeek": "Monday",
      |            "sessions": [
      |                {
      |                    "sessionOfTheWeek": "monday-early-morning-session",
      |                    "subject": "subject-reading",
      |                    "startTimeIso8601": "09:05",
      |                    "endTimeIso8601": "10:35",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "monday-late-morning-session",
      |                    "subject": "subject-spelling",
      |                    "startTimeIso8601": "10:50",
      |                    "endTimeIso8601": "11:15",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "monday-late-morning-session",
      |                    "subject": "subject-writing",
      |                    "startTimeIso8601": "11:15",
      |                    "endTimeIso8601": "12:05",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "monday-afternoon-session",
      |                    "subject": "subject-topic",
      |                    "startTimeIso8601": "13:05",
      |                    "endTimeIso8601": "15:05",
      |                    "lessonAdditionalInfo": ""
      |                }
      |            ]
      |        },
      |        {
      |            "dayOfTheWeek": "Friday",
      |            "sessions": [
      |                {
      |                    "sessionOfTheWeek": "friday-early-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "09:05",
      |                    "endTimeIso8601": "10:35",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "friday-late-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "10:50",
      |                    "endTimeIso8601": "12:05",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "friday-afternoon-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "13:05",
      |                    "endTimeIso8601": "15:05",
      |                    "lessonAdditionalInfo": ""
      |                }
      |            ]
      |        }
      |    ]
      |}
      |
    """
    .stripMargin
  }
}
