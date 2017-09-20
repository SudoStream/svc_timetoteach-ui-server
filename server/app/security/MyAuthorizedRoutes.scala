package security

import javax.inject.Inject

import be.objectify.deadbolt.scala.allOfGroup
import be.objectify.deadbolt.scala.filters.{AuthorizedRoute, AuthorizedRoutes, FilterConstraints}
import be.objectify.deadbolt.scala.filters._

class MyAuthorizedRoutes @Inject()(filterConstraints: FilterConstraints) extends AuthorizedRoutes {

  override val routes: Seq[AuthorizedRoute] =
    Seq(AuthorizedRoute(Get, "/profile", filterConstraints.subjectPresent, handler = None)
//      ,
//      AuthorizedRoute(Any, "/view/$foo<[^/]+>/$bar<[^/]+>", filterConstraints.restrict(allOfGroup("someRole")))
//      , handler = None
    )

}