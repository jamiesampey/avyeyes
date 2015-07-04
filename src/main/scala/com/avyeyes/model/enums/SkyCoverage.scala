package com.avyeyes.model.enums

object SkyCoverage extends Enumeration with UISelectableEnum {
  type SkyCoverage = Value
  
  override def isCompositeLabel = true
  
  val U = Value(0, "U")
  val Clear = Value(1, "clear")
  val Few = Value(2, "few")
  val Scattered = Value(3, "scattered")
  val Broken = Value(4, "broken")
  val Overcast = Value(5, "overcast")
  val Obscured = Value(6, "obscured")
}