package com.avyeyes.model.enums

object AvalancheTriggerCause extends AutocompleteEnum {
  override def default = U

  type AvalancheTriggerCause = Value

  val U = Value("AvalancheTriggerCause.U")
  val r = Value("AvalancheTriggerCause.r")
  val y = Value("AvalancheTriggerCause.y")
  val c = Value("AvalancheTriggerCause.c")
  val u = Value("AvalancheTriggerCause.u")
}