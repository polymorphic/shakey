package com.microworkflow.rest

import org.scalatest.FunSuite

/**
  * Created by dam on 2/25/17.
  */
class SkillHandlerTest extends FunSuite {

  test("should translate cardinal points to English") {
    val places = Seq(
      ("6km E of East Foothills, California", "East")
      , ("101km NE of San Isidro, Philippines", "Northeast")
      , ("221km SSE of Hachijo-jima, Japan", "South-southeast")
    )
    val output = places.map(p ⇒ (SkillHandler.expandAbbreviation(p._1), p._2))
    assert(output.forall(e ⇒ e._1.contains(e._2)))
  }

}
