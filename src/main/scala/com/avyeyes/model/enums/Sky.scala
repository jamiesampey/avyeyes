package com.avyeyes.model.enums

object Sky extends Enumeration with UISelectableEnum {
  type Sky = Value
  
  override def isCompositeLabel = true
  
  val U = Value(0)
  val Clear = Value(1)
  val Few = Value(2)
  val Scattered = Value(3)
  val Broken = Value(4)
  val Overcast = Value(5)
  val Obscured = Value(6)
}