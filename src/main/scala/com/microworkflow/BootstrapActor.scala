package com.microworkflow

import akka.actor.{Actor, Props}
import akka.pattern.BackoffSupervisor
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.duration._

class BootstrapActor private(propsBuilder: Materializer ⇒ Props) extends Actor {

  override def receive: Receive = {
    case _ ⇒ /* nop */
  }

  implicit val materializer = ActorMaterializer()
  val supervisorProps =
    BackoffSupervisor.props(
      propsBuilder(materializer)
      , "rest-router"
      , minBackoff = 5.seconds
      , maxBackoff = 50.seconds
      , randomFactor = 0.11
    )
  context.actorOf(supervisorProps)
}

object BootstrapActor {

  def props(propsBuilder: Materializer ⇒ Props): Props =
    Props(classOf[BootstrapActor], propsBuilder)
}