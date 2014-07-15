package com.avyeyes.model.enums

object Sky extends DataCodeEnum {
  type Sky = Value
  
  val U = Value(0)
  val CLEAR = Value(1)
  val FEW = Value(2)
  val SCATTERED = Value(3)
  val BROKEN = Value(4)
  val OVERCAST = Value(5)
  val OBSCURED = Value(6)
}