package dao

import com.google.inject.ImplementedBy
import org.mongodb.scala.{Document, MongoCollection}

@ImplementedBy(classOf[MongoDbConnectionImpl])
trait MongoDbConnection {
  def getTermlyPlanningCollection: MongoCollection[Document]
  def getTermlyCurriculumSelectionCollection: MongoCollection[Document]
  def getWeeklyPlanningCollection: MongoCollection[Document]
  def getLessonPlanningCollection: MongoCollection[Document]
  def getEAndOBenchmarkStatusCollection: MongoCollection[Document]
  def getSchoolTermsCollection: MongoCollection[Document]
  def getSystemDateCollection: MongoCollection[Document]

  def ensureTermlyCurriculumSelectionIndexes()
  def ensureTermlyPlanningIndexes()
  def ensureWeeklyPlanningIndexes()
  def ensureLessonPlanningIndexes()
  def ensureEandOBenchmarkStatusIndexes()
}
