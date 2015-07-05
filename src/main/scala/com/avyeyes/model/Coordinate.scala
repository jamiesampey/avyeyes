package com.avyeyes.model

import slick.driver.PostgresDriver.api._

case class Coordinate(longitude: Double,
                      latitude: Double,
                      altitude: Double)

case class LiftedCoordinate(longitude: Rep[Double],
                            latitude: Rep[Double],
                            altitude: Rep[Double])

object LiftedCoordinate {

  implicit def toString(c: Coordinate) = s"${c.longitude},${c.latitude},${c.altitude}"

  implicit def fromString(str: String) = {
    val arr = str.split(',')
    Coordinate(arr(0).toDouble, arr(1).toDouble, arr(2).toDouble)
  }
}