package com.avyeyes.model.enums

object AvalancheInterface extends Enumeration with UISelectableEnum {
  type AvalancheInterface = Value
  
  override def isCompositeLabel = true
  
  val U = Value(0)
  val S = Value(1) 
  val I = Value(2)
  val O = Value(3)
  val G = Value(4)
}