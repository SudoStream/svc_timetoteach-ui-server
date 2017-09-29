package controllers.serviceproxies

import com.google.inject.Singleton
import models.timetoteach.TimeToTeachUser

@Singleton
class UserWriterServiceProxyImpl {
  def createNewUser(user: TimeToTeachUser): TimeToTeachUserId = {
    // TODO: request user-writer service to add user
    TimeToTeachUserId("TODO: This should be a real result back from the user writer service")
  }
}

case class TimeToTeachUserId(value: String)