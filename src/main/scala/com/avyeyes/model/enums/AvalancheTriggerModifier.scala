package com.avyeyes.model.enums

object AvalancheTriggerModifier extends AutocompleteEnum {
  override def default = U

  type AvalancheTriggerModifier = Value

  val U = Value("AvalancheTriggerModifier.U")
  val r = Value("AvalancheTriggerModifier.r")
  val y = Value("AvalancheTriggerModifier.y")
  val c = Value("AvalancheTriggerModifier.c")
  val u = Value("AvalancheTriggerModifier.u")
}