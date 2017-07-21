package com.jamiesampey.avyeyes.model.enums

object AvalancheInterface extends AutocompleteEnum {
  override def default = empty

  type AvalancheInterface = Value

  val empty = Value("AvalancheInterface.empty")

  val S = Value("AvalancheInterface.S")
  val I = Value("AvalancheInterface.I")
  val O = Value("AvalancheInterface.O")
  val G = Value("AvalancheInterface.G")
  val U = Value("AvalancheInterface.U")
}