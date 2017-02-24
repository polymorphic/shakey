package com.microworkflow.domain

sealed trait DomainObject

final case class HealthResponseDocument(body: String) extends DomainObject

final case class OutputSpeechElement(
                                    `type`: String
                                    , text: String
                                    )
final case class EchoResponse(
                               shouldEndSession: Boolean
                              , outputSpeechElement: OutputSpeechElement
                             )

final case class EarthquakeData(place: String, magnitude: Double, time: Long) extends DomainObject
