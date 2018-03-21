package dao

import com.google.inject.ImplementedBy
import org.mongodb.scala.{Document, MongoCollection}

@ImplementedBy(classOf[MongoDbConnectionImpl])
trait MongoDbConnection
{
  def getTermlyPlanningCollection: MongoCollection[Document]
  def getTermlyCurriculumSelectionCollection: MongoCollection[Document]
  def getSchoolTermsCollection: MongoCollection[Document]

  def ensureTermlyCurriculumSelectionIndexes()
  def ensureTermlyPlanningIndexes()
}
