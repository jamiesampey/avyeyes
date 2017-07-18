package com.avyeyes.model.enums

object AvalancheTriggerModifier extends AutocompleteEnum {
  override def default = empty

  type AvalancheTriggerModifier = Value

  val empty = Value("AvalancheTriggerModifier.empty")

  val c = Value("AvalancheTriggerModifier.c")
  val u = Value("AvalancheTriggerModifier.u")
  val r = Value("AvalancheTriggerModifier.r")
  val y = Value("AvalancheTriggerModifier.y")
}