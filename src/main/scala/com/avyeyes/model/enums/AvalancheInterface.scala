package com.avyeyes.model.enums

object AvalancheInterface extends AutocompleteEnum {
  override def default = U

  type AvalancheInterface = Value

  val U = Value("AvalancheInterface.U")
  val S = Value("AvalancheInterface.S")
  val I = Value("AvalancheInterface.I")
  val O = Value("AvalancheInterface.O")
  val G = Value("AvalancheInterface.G")
}