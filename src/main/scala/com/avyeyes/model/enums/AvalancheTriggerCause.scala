package com.avyeyes.model.enums

object AvalancheTriggerCause extends AutocompleteEnum {
  override def default = U

  type AvalancheTriggerCause = Value

  val U = Value("AvalancheTriggerCause.U")
  val Natural_r = Value("AvalancheTriggerCause.Natural.r")
  val Natural_y = Value("AvalancheTriggerCause.Natural.y")
  val Explosive_r = Value("AvalancheTriggerCause.Explosive.r")
  val Explosive_y = Value("AvalancheTriggerCause.Explosive.y")
  val Human_c = Value("AvalancheTriggerCause.Human.c")
  val Human_u = Value("AvalancheTriggerCause.Human.u")
  val Human_r = Value("AvalancheTriggerCause.Human.r")
  val Human_y = Value("AvalancheTriggerCause.Human.y")
}