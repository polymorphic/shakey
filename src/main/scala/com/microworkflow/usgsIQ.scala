package com.microworkflow

import com.microworkflow.domain.EarthquakeData

import scala.collection.immutable.Seq

/**
  * Created by dam on 2/22/17.
  */
object Usgs {
  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._, io.circe.optics.JsonPath._

  def extractLocations(responseJson: Json): Seq[EarthquakeData] = {

    def extractLocation(featureJson: Json): Option[EarthquakeData] = {
      for {
        place ← root.properties.place.string.getOption(featureJson)
        magnitude ← root.properties.mag.double.getOption(featureJson)
        time ← root.properties.time.long.getOption(featureJson)
      } yield EarthquakeData(place, magnitude, time)
    }

    val allFeatures: Seq[Json] = root.features.each.json.getAll(responseJson)
    val features = allFeatures.filter(j ⇒ {
      val cursor = j.hcursor
      val elementType = cursor.get[String]("type")
      elementType match {
        case Right(s) ⇒ s == "Feature"
        case Left(_) ⇒ false
      }
    })
    features.map(extractLocation).collect { case Some(s) ⇒ s }
  }
}
