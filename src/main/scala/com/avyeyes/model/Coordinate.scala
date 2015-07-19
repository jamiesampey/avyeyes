package com.avyeyes.model

import scala.util.{Failure, Success, Try}

case class Coordinate(longitude: Double,
                      latitude: Double,
                      altitude: Int)

object Coordinate {

  implicit def toString(c: Coordinate) = s"${c.longitude},${c.latitude},${c.altitude}"

  implicit def fromString(str: String) = {
    val arr = str.split(',')
    Try(Coordinate(arr(0).toDouble, arr(1).toDouble, arr(2).toDouble.toInt)) match {
      case Success(c) => c
      case Failure(ex) => Coordinate(0, 0, 0)
    }
  }
}