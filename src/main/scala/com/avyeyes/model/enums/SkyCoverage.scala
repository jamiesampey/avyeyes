package com.avyeyes.model.enums

object SkyCoverage extends Enumeration with UISelectableEnum {
  type SkyCoverage = Value
  
  override def isCompositeLabel = true
  
  val U = Value(0, "U")
  val CLR = Value(1, "CLR")
  val FEW = Value(2, "FEW")
  val SCT = Value(3, "SCT")
  val BKN = Value(4, "BKN")
  val OVC = Value(5, "OVC")
  val OBSCD = Value(6, "OBSCD")
}