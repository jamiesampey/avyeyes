package com.avyeyes.model.enums

object AvalancheTriggerCause extends AutocompleteEnum {
  override def default = U

  type AvalancheTriggerCause = Value

  val U = Value("AvalancheTriggerCause.U")
  val NE_R = Value("AvalancheTriggerCause.NaturalExplosive.R")
  val NE_Y = Value("AvalancheTriggerCause.NaturalExplosive.Y")
  val H_C = Value("AvalancheTriggerCause.Human.C")
  val H_U = Value("AvalancheTriggerCause.Human.U")
  val H_R = Value("AvalancheTriggerCause.Human.R")
  val H_Y = Value("AvalancheTriggerCause.Human.Y")
}