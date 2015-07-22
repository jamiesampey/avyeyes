package com.avyeyes.model.enums

object ExperienceLevel extends AutocompleteEnum {
  type ExperienceLevel = Value
  
  val A0 = Value(0, "A0")
  val A1 = Value(1, "A1")
  val A2 = Value(2, "A2")
  val P1 = Value(3, "P1")
  val P2 = Value(4, "P2")
  val PE = Value(5, "PE")
}