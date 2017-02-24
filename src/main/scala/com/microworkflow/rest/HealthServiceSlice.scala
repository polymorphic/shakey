package com.microworkflow.rest

import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import com.microworkflow.domain.HealthResponseDocument
import de.heikoseeberger.akkahttpcirce.CirceSupport._
import io.circe.generic.auto._

class HealthServiceSlice {

  val routes: Route = {
    path("ping") {
      get {
        complete(HealthResponseDocument("pong"))
      }
    }
  }
}
