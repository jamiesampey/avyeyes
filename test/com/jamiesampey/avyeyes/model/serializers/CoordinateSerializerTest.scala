package com.jamiesampey.avyeyes.model.serializers

import com.jamiesampey.avyeyes.model.Coordinate
import helpers.BaseSpec
import org.json4s.{Extraction, Formats}

class CoordinateSerializerTest extends BaseSpec {

  implicit val formats: Formats = avyeyesFormats

  "CoordinateSerializer" should {
    "serialize and deserialize a Coordinate instance" in {
      val coordinate = genCoordinate.generate
      val extractedCoordinate = Extraction.decompose(coordinate).extract[Coordinate]
      extractedCoordinate mustEqual coordinate
    }
  }
}
