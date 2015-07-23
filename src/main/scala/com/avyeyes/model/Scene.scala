package com.avyeyes.model

import com.avyeyes.model.JsonFormats.formats
import com.avyeyes.model.enums.Precipitation.Precipitation
import com.avyeyes.model.enums.SkyCoverage.SkyCoverage
import com.avyeyes.model.enums.{Precipitation, SkyCoverage}
import net.liftweb.json.{Extraction, CustomSerializer}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._


case class Scene(skyCoverage: SkyCoverage = SkyCoverage.U,
                 precipitation: Precipitation = Precipitation.U) {
  override def toString = s"${skyCoverage.toString}-${precipitation.toString}"
}

object Scene {
  def fromString(str: String) = {
    val arr = str.split("-")
    Scene(
      skyCoverage = SkyCoverage.withName(arr(0)),
      precipitation = Precipitation.withName(arr(1))
    )
  }

  object JsonSerializer extends CustomSerializer[Scene](format => (
    {
      case json: JValue =>
        Scene(
          skyCoverage = (json \ "skyCoverage").extract[SkyCoverage],
          precipitation = (json \ "precipitation").extract[Precipitation]
        )
    },
    {
      case Scene(sky, precip) =>
        ("skyCoverage" -> Extraction.decompose(sky)) ~
        ("precipitation" -> Extraction.decompose(precip))
    }))
}
