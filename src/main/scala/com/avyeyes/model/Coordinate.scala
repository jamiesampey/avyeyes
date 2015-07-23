package com.avyeyes.model

import com.avyeyes.model.JsonFormats.formats
import net.liftweb.json.CustomSerializer
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._

import scala.util.{Failure, Success, Try}

case class Coordinate(longitude: Double,
                      latitude: Double,
                      altitude: Int) {
  override def toString = s"$longitude,$latitude,$altitude"
}

object Coordinate {
  def fromString(str: String) = {
    val arr = str.split(',')
    Try(Coordinate(arr(0).toDouble, arr(1).toDouble, arr(2).toDouble.toInt)) match {
      case Success(c) => c
      case Failure(ex) => Coordinate(0, 0, 0)
    }
  }

  object JsonSerializer extends CustomSerializer[Coordinate](format => (
    {
      case json: JValue =>
        Coordinate(
          longitude = (json \ "longitude").extract[Double],
          latitude = (json \ "latitude").extract[Double],
          altitude = (json \ "interface").extract[Int]
        )
    },
    {
      case Coordinate(lng, lat, alt) => ("longitude" -> lng) ~ ("latitude" -> lat) ~ ("altitude" -> alt)
    }))
}