package com.avyeyes.model.serializers

import helpers.BaseSpec
import org.json4s.{Extraction, Formats}

class AvalancheImageSerializerTest extends BaseSpec {

  implicit val formats: Formats = avyeyesFormats

  "AvalancheImageSerializer" should {
    "serialize an AvalancheImage to JSON" in {
      val image = genAvalancheImage.generate
      val json = Extraction.decompose(image)

      (json \ "filename").extract[String] mustEqual image.filename
      (json \ "mimeType").extract[String] mustEqual image.mimeType
      (json \ "size").extract[Int] mustEqual image.size
      (json \ "caption").extractOpt[String] mustEqual image.caption
    }
  }
}
