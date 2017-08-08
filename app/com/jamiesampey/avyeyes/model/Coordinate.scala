package com.jamiesampey.avyeyes.model

import scala.math._

case class Coordinate(longitude: Double, latitude: Double, altitude: Double) {
  override def toString = s"$longitude,$latitude,$altitude"

  def ecefDistanceTo(other: Coordinate): Double = {
    val p = toEcef
    val q = other.toEcef

    sqrt(pow(p._1 - q._1, 2) + pow(p._2 - q._2, 2) + pow(p._3 - q._3, 2))
  }

  private lazy val toEcef: (Double, Double, Double) = {
    val lambda = toRadians(latitude)
    val phi = toRadians(longitude)
    val s = sin(lambda)
    val N = Coordinate.a / sqrt(1 - Coordinate.e_squared * s * s)

    val x = (altitude + N) * cos(lambda) * cos(phi)
    val y = (altitude + N) * cos(lambda) * sin(phi)
    val z = (altitude + (1 - Coordinate.e_squared) * N) * sin(lambda)

    (x, y, z)
  }
}

object Coordinate {
  // WGS-84 geodetic constants
  val a = 6378137.0           // WGS-84 Earth semimajor axis (meters)
  val b = 6356752.3142        // WGS-84 Earth semiminor axis (meters)
  val f = (a - b) / a         // Ellipsoid Flatness
  val e_squared = f * (2 - f) // Square of Eccentricity

  def apply(str: String): Coordinate = {
    val arr = str.split(',')
    Coordinate(arr(0).toDouble, arr(1).toDouble, arr(2).toDouble)
  }
}