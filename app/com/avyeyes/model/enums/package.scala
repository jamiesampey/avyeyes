package com.avyeyes.model

package object enums {
  val CompositeLabelEnums = Seq(
    enumSimpleName(AvalancheInterface),
    enumSimpleName(AvalancheTrigger),
    enumSimpleName(AvalancheTriggerModifier),
    enumSimpleName(AvalancheType)
  )

  def enumSimpleName(enum: AutocompleteEnum) = enum.getClass.getSimpleName.replace("$", "")
}
