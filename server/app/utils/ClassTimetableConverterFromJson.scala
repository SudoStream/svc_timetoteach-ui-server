package utils

import play.api.libs.json.{JsArray, JsObject, JsString, JsValue}
import shared.model.classtimetable.SchoolDayTimeBoundary
import utils.ClassTimetableConverterToAvro.convertSchoolDayTimeBoundaryToStartTimeTuplesToMap

trait ClassTimetableConverterFromJson {

  def createSchoolTimesFromJson(classTimetableAsJson: JsValue): Map[SchoolDayTimeBoundary, String] = {
    val schoolTimesJsArray = (classTimetableAsJson \ "schoolTimes").get.as[JsArray]

    val schoolDayTimeBoundaryToStartTimeTuples = {
      for {
        boundaryJsValue <- schoolTimesJsArray.value
        boundaryJsObject = boundaryJsValue.asInstanceOf[JsObject]
        sessionBoundaryName = boundaryJsObject("sessionBoundaryName").asInstanceOf[JsString]
        boundaryStartTime = boundaryJsObject("boundaryStartTime").asInstanceOf[JsString].value
        schoolDayTimeBoundary = SchoolDayTimeBoundary.createSchoolDayTimeBoundaryFromString(sessionBoundaryName.value)
      } yield (schoolDayTimeBoundary, boundaryStartTime)
    }.toList

    val schoolDayTimeBoundaryToStartTime: Map[SchoolDayTimeBoundary, String] =
      convertSchoolDayTimeBoundaryToStartTimeTuplesToMap(schoolDayTimeBoundaryToStartTimeTuples)
    schoolDayTimeBoundaryToStartTime
  }

  private[utils] def convertSchoolDayTimeBoundaryToStartTimeTuplesToMap(incomingTuples: List[(SchoolDayTimeBoundary, String)]): Map[SchoolDayTimeBoundary, String] = {
    def loop(currentMap: Map[SchoolDayTimeBoundary, String],
             tupleToAdd: (SchoolDayTimeBoundary, String),
             remainingTuples: List[(SchoolDayTimeBoundary, String)]): Map[SchoolDayTimeBoundary, String] = {
      if (remainingTuples.isEmpty) {
        currentMap + (tupleToAdd._1 -> tupleToAdd._2)
      } else {
        loop(currentMap + (tupleToAdd._1 -> tupleToAdd._2), remainingTuples.head, remainingTuples.tail)
      }
    }

    loop(Map(), incomingTuples.head, incomingTuples.tail)
  }

}
