package com.avyeyes.model.serializers

import com.avyeyes.model.HumanNumbers
import helpers.BaseSpec
import org.json4s.{Extraction, Formats}

class HumanNumbersSerializerTest extends BaseSpec {

  implicit val formats: Formats = avyeyesFormats

  "HumanNumbersSerializer" should {
    "serialize and deserialize a HumanNumbers instance" in {
      val humanNumbers = genHumanNumbers.generate
      val extractedHumanNumbers = Extraction.decompose(humanNumbers).extract[HumanNumbers]
      extractedHumanNumbers mustEqual humanNumbers
    }
  }
}
