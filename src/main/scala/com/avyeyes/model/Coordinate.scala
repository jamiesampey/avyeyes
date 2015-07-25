package com.avyeyes.model

case class Coordinate(longitude: Double,
                      latitude: Double,
                      altitude: Int) {

  override def toString = s"$longitude,$latitude,$altitude"
}
