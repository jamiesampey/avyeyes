package com.avyeyes.model.serializers

import com.avyeyes.model.Slope
import com.avyeyes.model.enums._
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

object SlopeSerializer extends CustomSerializer[Slope]( implicit formats => (
  {
    case json: JValue => Slope(
      aspect = Direction.fromCode((json \ "aspect").extract[String]),
      angle = (json \ "angle").extract[Int],
      elevation = (json \ "elevation").extract[Int]
    )
  },{
  case slope: Slope =>
    ("aspect" -> Direction.toCode(slope.aspect)) ~
    ("angle" -> slope.angle) ~
    ("elevation" -> slope.elevation)
  }
))
