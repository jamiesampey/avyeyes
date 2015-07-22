package com.avyeyes.model.enums

object AvalancheInterface extends UISelectableEnum {
  type AvalancheInterface = Value
  
  override def isCompositeLabel = true
  
  val U = Value(0, "U")
  val S = Value(1, "S")
  val I = Value(2, "I")
  val O = Value(3, "O")
  val G = Value(4, "G")
}