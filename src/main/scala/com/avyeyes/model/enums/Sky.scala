package com.avyeyes.model.enums

object Sky extends DataCodeEnum {
  val CLEAR = new DataCodeVal(1, "Clear", "No clouds")
  val FEW_CLOUDS = new DataCodeVal(2, "Few", "Few clouds")
  val SCATTERED = new DataCodeVal(3, "Scattered", "Partially cloudy")
  val BROKEN = new DataCodeVal(4, "Broken", "Cloudy")
  val OVERCAST = new DataCodeVal(5, "Overcast")
  val OBSCURED = new DataCodeVal(6, "Obscured", "Surface clouds/fog")
}