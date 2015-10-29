package com.avyeyes.model.enums

object WindSpeed extends AutocompleteEnum {
  override def default = U

  type WindSpeed = Value

  val U = Value("WindSpeed.U")
  val Calm = Value("WindSpeed.Calm")
  val LightBreeze = Value("WindSpeed.LightBreeze")
  val ModerateBreeze = Value("WindSpeed.ModerateBreeze")
  val StrongBreeze = Value("WindSpeed.StrongBreeze")
  val Gale = Value("WindSpeed.Gale")
  val Storm = Value("WindSpeed.Storm")
}