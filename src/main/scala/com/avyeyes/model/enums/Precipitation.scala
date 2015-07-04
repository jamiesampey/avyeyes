package com.avyeyes.model.enums

object Precipitation extends Enumeration with UISelectableEnum {
  type Precipitation = Value
  
  override def isCompositeLabel = true
  
  val U = Value(0, "U")
  val NO = Value(1, "NO")
  val RA = Value(2, "RA")
  val SN = Value(3, "SN")
  val RS = Value(4, "RS")
  val GR = Value(5, "GR")
  val ZR = Value(6, "ZR")
}