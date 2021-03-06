package com.jamiesampey.avyeyes.model.enums

object ExperienceLevel extends AutocompleteEnum {
  override def default = A0

  type ExperienceLevel = Value
  
  val A0 = Value("ExperienceLevel.A0")
  val A1 = Value("ExperienceLevel.A1")
  val A2 = Value("ExperienceLevel.A2")
  val P0 = Value("ExperienceLevel.P0")
  val P1 = Value("ExperienceLevel.P1")
  val P2 = Value("ExperienceLevel.P2")
}