package bootstrap

import javax.inject._

import com.typesafe.config.ConfigFactory


@Singleton
class Bootstrap {
  println("========================== Bootstrap ==========================")
  ConfigFactory.load()
}

import com.google.inject.AbstractModule

class StartModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Bootstrap]).asEagerSingleton()
  }
}