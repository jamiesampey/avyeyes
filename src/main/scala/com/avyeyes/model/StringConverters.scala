package com.avyeyes.model

import com.avyeyes.model.enums._

object StringConverters {

  implicit def stringToCoordinate(str: String): Coordinate = {
    val arr = str.split(',')
    Coordinate(arr(0).toDouble, arr(1).toDouble, arr(2).toDouble.toInt)
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
