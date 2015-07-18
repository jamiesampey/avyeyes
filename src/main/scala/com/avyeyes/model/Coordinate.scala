package com.avyeyes.model

case class Coordinate(longitude: Double,
                      latitude: Double,
                      altitude: Int)

object Coordinate {

  implicit def toString(c: Coordinate) = s"${c.longitude},${c.latitude},${c.altitude}"

  implicit def fromString(str: String) = {
    val arr = str.split(',')
    Coordinate(arr(0).toDouble, arr(1).toDouble, arr(2).toDouble.toInt)
  }
}