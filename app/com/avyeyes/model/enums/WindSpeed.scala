package com.avyeyes.model.enums

object WindSpeed extends AutocompleteEnum {
  override def default = empty

  type WindSpeed = Value

  val empty = Value("WindSpeed.empty")

  val Calm = Value("WindSpeed.Calm")
  val LightBreeze = Value("WindSpeed.LightBreeze")
  val ModerateBreeze = Value("WindSpeed.ModerateBreeze")
  val StrongBreeze = Value("WindSpeed.StrongBreeze")
  val Gale = Value("WindSpeed.Gale")
  val Storm = Value("WindSpeed.Storm")
}