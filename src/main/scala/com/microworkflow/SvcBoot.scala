package com.microworkflow

import akka.actor.ActorSystem
import com.microworkflow.rest.RouterActor
import com.typesafe.config.ConfigFactory

object SvcBoot extends App {

  val conf = ConfigFactory.load().resolve()

  val actorSystem = ActorSystem("rest-service-system", conf)

  actorSystem.actorOf(BootstrapActor.props(RouterActor.props _))
}
