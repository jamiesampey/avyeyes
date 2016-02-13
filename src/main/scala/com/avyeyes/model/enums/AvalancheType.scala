package com.avyeyes.model.enums

object AvalancheType extends AutocompleteEnum {
  override def default = empty

  type AvalancheType = Value

  val empty = Value("AvalancheType.empty")

  val L = Value("AvalancheType.L")
  val WL = Value("AvalancheType.WL")
  val SS = Value("AvalancheType.SS")
  val HS = Value("AvalancheType.HS")
  val WS = Value("AvalancheType.WS")
  val I = Value("AvalancheType.I")
  val SF = Value("AvalancheType.SF")
  val C = Value("AvalancheType.C")
  val R = Value("AvalancheType.R")
}