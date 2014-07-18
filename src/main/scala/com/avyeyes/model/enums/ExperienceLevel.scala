package com.avyeyes.model.enums

object ExperienceLevel extends Enumeration with UISelectableEnum {
  type ExperienceLevel = Value
  
  val A0 = Value(0)
  val A1 = Value(1)
  val A2 = Value(2)
  val P1 = Value(3)
  val P2 = Value(4)
  val PE = Value(5)
}