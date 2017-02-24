package com.microworkflow.rest

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RequestContext, Route}
import akka.stream.Materializer
import com.microworkflow.rest.SkillHandler.ProcessRequest
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.Json
import io.circe.generic.auto._
import io.github.todokr.Emojipolation._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

class SkillSlice()(implicit actorSystem: ActorSystem, m: Materializer) {
  val http = Http()

  val routes: Route = {
    path("") {
      pathEnd {
        get {
          complete(HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, emoji":+1:")))
        } ~ post {
          entity(as[Json]) { json ⇒
            val q = json
            processRequest(json)
          }
        }
      }
    }
  }

  def processRequest(json: Json): Route = {
    requestCotext: RequestContext ⇒ {
      implicit val askTimeout: Timeout = 3456.millis
      val requestHandler = actorSystem.actorOf(SkillHandler.props(m, http))
      requestCotext.complete(requestHandler.ask(ProcessRequest(json)).mapTo[Json])
    }
  }


}
