package com.microworkflow.rest

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.{ConfigFactory, ConfigParseOptions, ConfigRenderOptions, ConfigSyntax}

//noinspection TypeAnnotation
class UtilityServiceSlice {

  val options = ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF)
  val intentSchema = ConfigFactory.parseResources("intent-schema.json", options)

  val routes: Route = {
    path("info") {
      get {
        complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, buildinfo.BuildInfo.toJson)))
      }
    } ~ path("intent-schema") {
      pathEnd {
        get {
          complete {
            HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, intentSchema.root().render(ConfigRenderOptions.concise())))
          }
        }
      }
    }
  }
}
