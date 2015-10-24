package com.avyeyes.model

import com.avyeyes.model.enums._

object StringSerializers {

  implicit def enumValueToCode(enumValue: AutocompleteEnum#Value): String = {
    enumValue.toString.split('.')(1)
  }

  implicit def stringToCoordinate(str: String): Coordinate = {
    val arr = str.split(',')
    Coordinate(arr(0).toDouble, arr(1).toDouble, arr(2).toDouble.toInt)
  }

}
