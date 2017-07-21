package com.jamiesampey.avyeyes.model.serializers

import com.jamiesampey.avyeyes.model.Weather
import com.jamiesampey.avyeyes.model.enums._
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

object WeatherSerializer extends CustomSerializer[Weather]( implicit formats => (
  {
    case json: JValue => Weather(
      recentSnow = (json \ "recentSnow").extract[Int],
      recentWindSpeed = WindSpeed.fromCode((json \ "recentWindSpeed").extract[String]),
      recentWindDirection = Direction.fromCode((json \ "recentWindDirection").extract[String])
    )
  },
  {
  case w: Weather =>
    ("recentSnow" -> w.recentSnow) ~
    ("recentWindSpeed" -> WindSpeed.toCode(w.recentWindSpeed)) ~
    ("recentWindDirection" -> Direction.toCode(w.recentWindDirection))
  }
))
