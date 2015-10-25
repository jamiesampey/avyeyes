package com.avyeyes.model

object StringSerializers {

  implicit def stringToCoordinate(str: String): Coordinate = {
    val arr = str.split(',')
    Coordinate(arr(0).toDouble, arr(1).toDouble, arr(2).toDouble.toInt)
  }

}
