package com.jamiesampey.avyeyes.model.serializers

import com.jamiesampey.avyeyes.model.Weather
import helpers.BaseSpec
import org.json4s.{Extraction, Formats}

class WeatherSerializerTest extends BaseSpec {

  implicit val formats: Formats = avyeyesFormats

  "WeatherSerializer" should {
    "serialize and deserialize a Weather instance" in {
      val weather = genWeather.generate
      val extractedWeather = Extraction.decompose(weather).extract[Weather]
      extractedWeather mustEqual weather
    }
  }
}
