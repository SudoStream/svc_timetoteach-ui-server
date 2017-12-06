package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import be.objectify.deadbolt.scala.cache.HandlerCache
import controllers.serviceproxies.{ClassTimetableWriterServiceProxyImpl, SchoolReaderServiceProxyImpl, UserReaderServiceProxyImpl, UserWriterServiceProxyImpl}
import play.api.mvc.Controller

class ClassTimetableController @Inject()(classTimetableWriter: ClassTimetableWriterServiceProxyImpl,
                                         deadbolt: DeadboltActions,
                                         handlers: HandlerCache,
                                         actionBuilder: ActionBuilders) extends Controller {

}
