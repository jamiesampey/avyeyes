package com.avyeyes.model.serializers

import com.avyeyes.model.Slope
import helpers.BaseSpec
import org.json4s.{Extraction, Formats}

class SlopeSerializerTest extends BaseSpec {

  implicit val formats: Formats = avyeyesFormats

  "SlopeSerializer" should {
    "serialize and deserialize a Slope instance" in {
      val slope = genSlope.generate
      val extractedSlope = Extraction.decompose(slope).extract[Slope]
      extractedSlope mustEqual slope
    }
  }
}
