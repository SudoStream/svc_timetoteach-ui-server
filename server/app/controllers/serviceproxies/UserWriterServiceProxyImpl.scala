package controllers.serviceproxies

import com.google.inject.Singleton
import models.timetoteach.TimeToTeachUser

@Singleton
class UserWriterServiceProxyImpl {
  def createNewUser(user: TimeToTeachUser): TimeToTeachUserId = {
    TimeToTeachUserId("howdydoodlydoo")
  }
}

case class TimeToTeachUserId(value: String)