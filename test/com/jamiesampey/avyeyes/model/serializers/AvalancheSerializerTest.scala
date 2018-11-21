package com.jamiesampey.avyeyes.model.serializers

import com.jamiesampey.avyeyes.model._
import com.jamiesampey.avyeyes.model.enums.ExperienceLevel
import helpers.BaseSpec
import org.joda.time.DateTime
import org.json4s.{Extraction, Formats}
import com.jamiesampey.avyeyes.util.Converters._
import org.json4s.JsonAST.JString
class AvalancheSerializerTest extends BaseSpec {

  implicit val formats: Formats = avyeyesFormats

  "AvalancheSerializer" should {
    "serialize an Avalanche to JSON" in {
      val avalanche = genAvalanche.generate
      val json = Extraction.decompose(avalanche)

      (json \ "createTime").extract[DateTime] mustEqual strToDate(dateToStr(avalanche.createTime))
      (json \ "updateTime").extract[DateTime] mustEqual strToDate(dateToStr(avalanche.updateTime))
      (json \ "viewable").extract[Boolean] mustEqual avalanche.viewable
      (json \ "extId").extract[String] mustEqual avalanche.extId
      (json \ "submitterEmail").extract[String] mustEqual avalanche.submitterEmail
      (json \ "submitterExp").extract[String] mustEqual ExperienceLevel.toCode(avalanche.submitterExp)
      (json \ "location").extract[Coordinate] mustEqual avalanche.location
      (json \ "date").extract[DateTime] mustEqual strToDate(dateToStr(avalanche.date))
      (json \ "areaName").extract[String] mustEqual avalanche.areaName
      (json \ "slope").extract[Slope] mustEqual avalanche.slope
      (json \ "weather").extract[Weather] mustEqual avalanche.weather
      (json \ "classification").extract[Classification] mustEqual avalanche.classification
      (json \ "perimeter").extract[Seq[Coordinate]] mustEqual avalanche.perimeter
      (json \ "comments").extractOpt[String] mustEqual avalanche.comments
    }

    "deserialize an Avalanche from JSON" in {
      val avalanche = genAvalanche.generate
      val json = Extraction.decompose(avalanche)
      val extractedAvalanche = json.extract[Avalanche]

      extractedAvalanche.createTime mustEqual strToDate(dateToStr(avalanche.createTime))
      extractedAvalanche.updateTime mustEqual strToDate(dateToStr(avalanche.updateTime))
      extractedAvalanche.viewable mustEqual avalanche.viewable
      extractedAvalanche.extId mustEqual avalanche.extId
      extractedAvalanche.submitterEmail mustEqual avalanche.submitterEmail
      extractedAvalanche.submitterExp mustEqual avalanche.submitterExp
      extractedAvalanche.location mustEqual avalanche.location
      extractedAvalanche.date mustEqual strToDate(dateToStr(avalanche.date))
      extractedAvalanche.areaName mustEqual avalanche.areaName
      extractedAvalanche.slope mustEqual avalanche.slope
      extractedAvalanche.weather mustEqual avalanche.weather
      extractedAvalanche.classification mustEqual avalanche.classification
      extractedAvalanche.perimeter mustEqual avalanche.perimeter
    }

    "escape encoded chars in comments" in {
      val avalanche = genAvalanche.generate
      val json = Extraction.decompose(avalanche).replace(List("comments"),
        JString("""Some comments with a
            |newline in the middle""".stripMargin))

      val extractedAvalanche = json.extract[Avalanche]

      extractedAvalanche.comments must beSome
      extractedAvalanche.comments.get.contains("\\n") must beTrue
    }

    "translate empty comments to None" in {
      val avalanche = genAvalanche.generate
      val json = Extraction.decompose(avalanche).replace(List("comments"), JString(""))

      val extractedAvalanche = json.extract[Avalanche]

      extractedAvalanche.comments must beNone
    }
  }
}
