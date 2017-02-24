package com.microworkflow.rest

import akka.actor.{Actor, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

class RouterActor private(implicit val m: Materializer) extends Actor {

  import context.dispatcher

  override def receive: Receive = {
    case _ ⇒ /* nop */
  }

  implicit val actorSystem = context.system

  val svcConfig = context.system.settings.config.getConfig("svc")

  val utilitySlice = new UtilityServiceSlice()
  val healthSlice = new HealthServiceSlice()
  val skillSlice = new SkillSlice()

  val routes: Route = {
    utilitySlice.routes ~
      healthSlice.routes ~
      skillSlice.routes
  }

  val bindingF =
    Http()
      .bindAndHandle(
        routes
        , svcConfig.getString("interface")
        , svcConfig.getInt("defaultPort")
      )

  sys.addShutdownHook {
    bindingF.flatMap(_.unbind())
  }

}

object RouterActor {
  def props(m: Materializer): Props = Props(classOf[RouterActor], m)
}
