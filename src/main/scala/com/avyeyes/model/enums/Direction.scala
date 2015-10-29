package com.avyeyes.model.enums

object Direction extends AutocompleteEnum {
  override def default = U

  type Direction = Value

  val U = Value("Direction.U")
	val N = Value("Direction.N")
	val NE = Value("Direction.NE")
	val E = Value("Direction.E")
	val SE = Value("Direction.SE")
	val S = Value("Direction.S")
	val SW = Value("Direction.SW")
	val W = Value("Direction.W")
	val NW = Value("Direction.NW")
}