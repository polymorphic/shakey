package com.microworkflow.rest

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.pattern.pipe
import akka.stream.Materializer
import akka.util.ByteString
import com.microworkflow.rest.SkillHandler.ProcessRequest
import com.microworkflow.{EchoIQ, IntentRequest, Usgs}
import io.circe.Json

import scala.collection.immutable.Seq
import scala.concurrent.Future

/**
  * Docs at
  * - https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/overviews/understanding-custom-skills
  * - https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/developing-an-alexa-skill-as-a-web-service
  *
  * Intents = actions that the users can perform
  * Sample utterances = what do users say to invoke the intents. The mapping utterances->intents = interaction model
  * Invocation name = how would the user invoke the skill
  *
  */
object SkillHandler {

  sealed trait SkillHandlerMessages

  case class ProcessRequest(json: Json) extends SkillHandlerMessages

  def props(m: Materializer, http: HttpExt): Props = Props(classOf[SkillHandler], m, http)
}

class SkillHandler(implicit val m: Materializer, http: HttpExt) extends Actor with ActorLogging with EchoIQ {
  implicit val ec = context.dispatcher

  val actorSystem = context.system
  val config = actorSystem.settings.config

  def callUsgsService(uriString: String) = {
    http.singleRequest(HttpRequest(uri = uriString)).pipeTo(self)
  }

  override def receive: Receive = {
    case ProcessRequest(echoJsonRequest) ⇒
      /*
      Request format: https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/alexa-skills-kit-interface-reference#request-format
       */
      unpackEchoServiceRequest(echoJsonRequest) match {
        case Right(intentRequest: IntentRequest) ⇒ processIntentRequest(intentRequest.intent, intentRequest.slots)
        case Right(_) ⇒
        case Left(message) ⇒ buildEchoServiceResponse(message)
      }
  }

  private def processIntentRequest(intent: String, slots: Seq[String]) = {
    intent match {
      case "GetEarthquakes" ⇒
        val uri = config.getString("provider.url25day")
        context.become(waitingReceive(sender()))
        callUsgsService(uri)
      case _ ⇒ /* nop */
    }
  }

  def waitingReceive(respondTo: ActorRef): Receive = {
    case okResponse: HttpResponse if okResponse.status.isSuccess() ⇒
      import io.circe._
      import io.circe.parser._
      val responseF = okResponse.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).
        map(bs ⇒ parse(bs.utf8String).getOrElse(Json.Null)).
        flatMap(usgsJson ⇒ decodeUsgsResponse(usgsJson)).
        flatMap(response ⇒ buildEchoServiceResponse(response))
       responseF.pipeTo(respondTo)
      responseF.onComplete(_ ⇒ context.stop(self))
    case koResponse: HttpResponse if koResponse.status.isFailure() ⇒
      koResponse.entity.discardBytes(m)
      done(respondTo)
    case Failure(cause) ⇒
      log.error(cause, "oops")
      done(respondTo)
  }

  private def done(respondTo: ActorRef) = {
    respondTo ! buildEchoServiceResponse()
    context.stop(self)
  }

  val max = 7

  def decodeUsgsResponse(json: Json): Future[String] = Future {
    val earthquakes = Usgs.
      extractLocations(json).
      sortWith((ed1, ed2) ⇒ ed1.time < ed2.time)
    val sb = new StringBuilder()
    val quakes = if (earthquakes.size >= max) {
      sb.append(s"Too many results! Returning the $max most recent. ")
      earthquakes.takeRight(max)
    } else {
      earthquakes
    }
    val quakeList = quakes.map( ed ⇒ s"Magnitude ${ed.magnitude} at ${ed.place}")
    sb.append(quakeList.mkString("", "; ", "."))
    sb.result()
  }

}


