package com.avyeyes.model

import com.avyeyes.model.JsonFormats.formats

import com.avyeyes.model.enums._
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._

import scala.util.{Failure, Success, Try}

object Converters {

  implicit def avalancheImageToJson(img: AvalancheImage): JValue =
    ("filename" -> img.filename) ~ ("mimeType" -> img.mimeType) ~ ("size" -> img.size)

  implicit def coordinateToJson(c: Coordinate): JValue =
    ("longitude" -> c.longitude) ~ ("latitude" -> c.latitude) ~ ("altitude" -> c.altitude)

  implicit def enumToJson(enumValue: AutocompleteEnum#Value): JValue =
    ("value" -> enumValue.toString) ~ ("label" -> enumValue.getEnumLabel)

  implicit def stringToCoordinate(str: String): Coordinate = {
    val arr = str.split(',')
    Try(Coordinate(arr(0).toDouble, arr(1).toDouble, arr(2).toDouble.toInt)) match {
      case Success(c) => c
      case Failure(ex) => Coordinate(0, 0, 0)
    }
  }

  implicit def stringToClassification(str: String): Classification = {
    val arr = str.split("-")
    Classification(
      avyType = AvalancheType.withName(arr(0)),
      trigger = AvalancheTrigger.withName(arr(1)),
      rSize = arr(2).toDouble,
      dSize = arr(3).toDouble,
      interface = AvalancheInterface.withName(arr(4))
    )
  }

  implicit def stringToHumanNumbers(str: String): HumanNumbers = {
    val arr = str.split(',')
    HumanNumbers(
      ModeOfTravel.withName(arr(0)),
      arr(1).toInt,
      arr(2).toInt,
      arr(3).toInt,
      arr(4).toInt,
      arr(5).toInt
    )
  }

  implicit def stringToScene(str: String): Scene = {
    val arr = str.split("-")
    Scene(
      skyCoverage = SkyCoverage.withName(arr(0)),
      precipitation = Precipitation.withName(arr(1))
    )
  }

  implicit def stringToSlope(str: String): Slope = {
    val arr = str.split("-")
    Slope(
      aspect = Aspect.withName(arr(0)),
      angle = arr(1).toInt,
      elevation = arr(2).toInt
    )
  }
}
