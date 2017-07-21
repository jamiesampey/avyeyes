package com.jamiesampey.avyeyes.model.serializers

import com.jamiesampey.avyeyes.model.Coordinate
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

object CoordinateSerializer extends CustomSerializer[Coordinate]( implicit formats => (
  {
    case json: JValue => Coordinate(
      longitude = (json \ "longitude").extract[Double],
      latitude = (json \ "latitude").extract[Double],
      altitude = (json \ "altitude").extract[Double]
    )
  },
  {
    case c: Coordinate => ("latitude" -> c.latitude) ~ ("longitude" -> c.longitude) ~ ("altitude" -> c.altitude)
  }
))
