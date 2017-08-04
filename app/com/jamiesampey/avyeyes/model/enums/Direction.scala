package com.jamiesampey.avyeyes.model.enums

object Direction extends AutocompleteEnum {
  override def default = empty

  type Direction = Value

  val empty = Value("Direction.empty")

	val N = Value("Direction.N")
	val NE = Value("Direction.NE")
	val E = Value("Direction.E")
	val SE = Value("Direction.SE")
	val S = Value("Direction.S")
	val SW = Value("Direction.SW")
	val W = Value("Direction.W")
	val NW = Value("Direction.NW")
}