package com.avyeyes.model

import scala.math._

case class Coordinate(longitude: Double,
                      latitude: Double,
                      altitude: Int) {
  override def toString = s"$longitude,$latitude,$altitude"

  def distanceTo(other: Coordinate) = {
    val dLat = (other.latitude - latitude).toRadians
    val dLon = (other.longitude - longitude).toRadians
    val ax = pow(sin(dLat/2),2) + pow(sin(dLon/2),2) * cos(latitude.toRadians) *
      cos(other.latitude.toRadians)
    val c = 2 * asin(sqrt(ax))
    3959.0 * c    // earth radius in miles * c
  }
}
