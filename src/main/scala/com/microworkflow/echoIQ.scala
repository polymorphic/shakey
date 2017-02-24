package com.microworkflow


import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by dam on 2/22/17.
  */
sealed trait EchoRequest

case class IntentRequest(intent: String, slots: Seq[String]) extends EchoRequest

case object LaunchRequest extends EchoRequest

case object SessionEndedRequest extends EchoRequest

trait EchoIQ {
  implicit def ec: ExecutionContext

  import io.circe._
  import io.circe.optics.JsonPath._
  import io.circe.parser._

  /*
  https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/alexa-skills-kit-interface-reference#request-format
 */
  def unpackEchoServiceRequest(echoJsonRequest: Json): Either[String, EchoRequest] = {
    val requestType = root.request.`type`.string.getOption(echoJsonRequest)
    requestType match {
      case Some("IntentRequest") ⇒
        val _intent = root.request.intent.name.string
        val intentOpt: Option[String] = _intent.getOption(echoJsonRequest)
        val slots: Seq[String] = root.request.intent.slots.each.string.getAll(echoJsonRequest)
        intentOpt match {
          case Some(intent) ⇒ Right(IntentRequest(intent, slots))
          case None ⇒ Left("could not extract intent")
        }
      case Some("LaunchRequest") ⇒ Left("launch request not implemented")
      case Some("SessionEndedRequest") ⇒ Left("session ended request not implemented")
      case _ ⇒ Left("unknown request type")
    }
  }

  /*
  https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/alexa-skills-kit-interface-reference#response-format
   */
  def buildEchoServiceResponse(response: String = "I'm sorry Dave, I can't do that"): Future[Json] = {
    val responseJsonString =
      """
        |{
        |  "version": "1.0",
        |  "response": {
        |    "outputSpeech": {
        |      "type": "PlainText",
        |      "text": ""
        |    },
        |    "shouldEndSession": true
        |  }
        |}
        |
      """.stripMargin
    val changeText: Json ⇒ Json = root.response.outputSpeech.text.string.modify(_ ⇒ response)
    Future {
      val json = parse(responseJsonString)
      json match {
        case Right(j) ⇒ changeText(j)
        case Left(parsingFailure) ⇒ throw parsingFailure.underlying
      }
    }
  }

}
