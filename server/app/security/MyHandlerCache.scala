package security

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{DeadboltHandler, HandlerKey}
import controllers.serviceproxies.UserReaderServiceProxyImpl

/**
  * @author Steve Chaloner (steve@objectify.be)
  */
@Singleton
class MyHandlerCache @Inject()(userReader: UserReaderServiceProxyImpl) extends HandlerCache {

  val defaultHandler: DeadboltHandler = new MyDeadboltHandler(userReader)

  val handlers: Map[Any, DeadboltHandler] = Map(HandlerKeys.defaultHandler -> defaultHandler,
    HandlerKeys.altHandler -> new MyDeadboltHandler(userReader, Some(MyAlternativeDynamicResourceHandler)),
    HandlerKeys.userlessHandler -> new MyUserlessDeadboltHandler(userReader))

  override def apply(): DeadboltHandler = defaultHandler

  override def apply(handlerKey: HandlerKey): DeadboltHandler = handlers(handlerKey)
}
