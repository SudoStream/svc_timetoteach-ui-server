package shared.util

trait ClassTimeTableUtilTestHelper {

  def createClassTimetableAsJson() : String = {
    """
      |{
      |    "schoolTimes": [
      |        {"sessionBoundaryName": "lunch-starts", "boundaryStartTime": "12:00"},
      |        {"sessionBoundaryName": "lunch-ends", "boundaryStartTime": "13:00"},
      |        {"sessionBoundaryName": "morning-break-ends", "boundaryStartTime": "10:45"},
      |        {"sessionBoundaryName": "morning-break-starts", "boundaryStartTime": "10:30"},
      |        {"sessionBoundaryName": "school-day-ends", "boundaryStartTime": "15:00"},
      |        {"sessionBoundaryName": "school-day-starts", "boundaryStartTime": "09:00"}
      |    ],
      |    "allSessionsOfTheWeek": [
      |        {
      |            "dayOfTheWeek": "Wednesday",
      |            "sessions": [
      |                {
      |                    "sessionOfTheWeek": "wednesday-early-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "09:00",
      |                    "endTimeIso8601": "10:30",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "wednesday-late-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "10:45",
      |                    "endTimeIso8601": "12:00",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "wednesday-afternoon-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "13:00",
      |                    "endTimeIso8601": "15:00",
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
      |                    "startTimeIso8601": "09:00",
      |                    "endTimeIso8601": "10:30",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "thursday-late-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "10:45",
      |                    "endTimeIso8601": "12:00",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "thursday-afternoon-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "13:00",
      |                    "endTimeIso8601": "15:00",
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
      |                    "startTimeIso8601": "09:00",
      |                    "endTimeIso8601": "09:45",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-early-morning-session",
      |                    "subject": "subject-art",
      |                    "startTimeIso8601": "09:45",
      |                    "endTimeIso8601": "10:30",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-late-morning-session",
      |                    "subject": "subject-writing",
      |                    "startTimeIso8601": "10:45",
      |                    "endTimeIso8601": "11:25",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-late-morning-session",
      |                    "subject": "subject-maths",
      |                    "startTimeIso8601": "11:25",
      |                    "endTimeIso8601": "12:00",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-afternoon-session",
      |                    "subject": "subject-art",
      |                    "startTimeIso8601": "13:00",
      |                    "endTimeIso8601": "14:00",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "tuesday-afternoon-session",
      |                    "subject": "subject-geography",
      |                    "startTimeIso8601": "14:00",
      |                    "endTimeIso8601": "15:00",
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
      |                    "startTimeIso8601": "09:00",
      |                    "endTimeIso8601": "10:30",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "monday-late-morning-session",
      |                    "subject": "subject-spelling",
      |                    "startTimeIso8601": "10:45",
      |                    "endTimeIso8601": "11:10",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "monday-late-morning-session",
      |                    "subject": "subject-writing",
      |                    "startTimeIso8601": "11:10",
      |                    "endTimeIso8601": "12:00",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "monday-afternoon-session",
      |                    "subject": "subject-topic",
      |                    "startTimeIso8601": "13:00",
      |                    "endTimeIso8601": "15:00",
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
      |                    "startTimeIso8601": "09:00",
      |                    "endTimeIso8601": "10:30",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "friday-late-morning-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "10:45",
      |                    "endTimeIso8601": "12:00",
      |                    "lessonAdditionalInfo": ""
      |                },
      |                {
      |                    "sessionOfTheWeek": "friday-afternoon-session",
      |                    "subject": "subject-empty",
      |                    "startTimeIso8601": "13:00",
      |                    "endTimeIso8601": "15:00",
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
