package security
import be.objectify.deadbolt.scala.filters.AuthorizedRoutes
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

class CustomDeadboltFilterHook extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[AuthorizedRoutes].to[MyAuthorizedRoutes]
  )
}
