package com.avyeyes.model

import com.avyeyes.model.JsonFormats.formats
import com.avyeyes.model.enums.Aspect
import com.avyeyes.model.enums.Aspect.Aspect
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._
import net.liftweb.json.{CustomSerializer, Extraction}


case class Slope(aspect: Aspect = Aspect.N,
                 angle: Int = 0,
                 elevation: Int = 0) {
  override def toString = s"${aspect.toString}-$angle-$elevation"
}

object Slope {
  def fromString(str: String) = {
    val arr = str.split("-")
    Slope(
      aspect = Aspect.withName(arr(0)),
      angle = arr(1).toInt,
      elevation = arr(2).toInt
    )
  }

  object JsonSerializer extends CustomSerializer[Slope](format => (
    {
      case json: JValue =>
        Slope(
          aspect = (json \ "aspect").extract[Aspect],
          angle = (json \ "angle").extract[Int],
          elevation = (json \ "elevation").extract[Int]
        )
    },
    {
      case Slope(aspect, angle, elevation) =>
        ("aspect" -> Extraction.decompose(aspect)) ~
        ("angle" -> angle) ~
        ("elevation" -> elevation)
    }))
}
