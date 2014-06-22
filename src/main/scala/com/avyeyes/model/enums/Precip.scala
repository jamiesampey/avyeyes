package com.avyeyes.model.enums

object Precip extends DataCodeEnum {
  val NO_PRECIP = new DataCodeVal(1, "NO", "No precipitation") 
  val RAIN = new DataCodeVal(2, "RA", "Rain")
  val SNOW = new DataCodeVal(3, "SN", "Snow")
  val MIXED = new DataCodeVal(4, "RS", "Mixed rain and snow")
  val GRAUPEL = new DataCodeVal(5, "GR", "Graupel and hail")
  val FREEZING_RAIN = new DataCodeVal(6, "ZR", "Freezing rain")
}