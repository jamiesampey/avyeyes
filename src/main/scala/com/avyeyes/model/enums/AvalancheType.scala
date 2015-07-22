package com.avyeyes.model.enums

object AvalancheType extends AutocompleteEnum {
  type AvalancheType = Value
  
  override def isCompositeLabel = true
  
  val U = Value(0, "U")
  val L = Value(1, "L")
  val WL = Value(2, "WL")
  val SS = Value(3, "SS")
  val HS = Value(4, "HS")
  val WS = Value(5, "WS")
  val I = Value(6, "I")
  val SF = Value(7, "SF")
  val C = Value(8, "C")
  val R = Value(9, "R")
}