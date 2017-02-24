package com.microworkflow.domain

import com.microworkflow.Usgs
import org.scalatest.FunSuite

import scala.io.Source
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.collection.immutable.Seq

/**
  * Created by dam on 2/12/17.
  */
class UsgsTest extends FunSuite {
  test("should work") {
    val jsonString = Source.fromFile(name = "data/sample.json").getLines().mkString
    parse(jsonString) match {
      case Right(json) ⇒
        val earthquakes: Seq[EarthquakeData] = Usgs.extractLocations(json)
        assert(24 == earthquakes.size)
      case Left(parsingFailure) ⇒ fail(parsingFailure.underlying)
    }

  }

}
