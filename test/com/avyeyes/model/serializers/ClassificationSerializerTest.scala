package com.avyeyes.model.serializers

import com.avyeyes.model.Classification
import helpers.BaseSpec
import org.json4s.{Extraction, Formats}

class ClassificationSerializerTest extends BaseSpec {

  implicit val formats: Formats = avyeyesFormats

  "ClassificationSerializer" should {
    "serialize and deserialize a Classification instance" in {
      val classification = genClassification.generate
      val extractedClassification = Extraction.decompose(classification).extract[Classification]
      extractedClassification mustEqual classification
    }
  }
}
