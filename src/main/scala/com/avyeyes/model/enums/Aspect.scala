package com.avyeyes.model.enums

object Aspect extends AutocompleteEnum {
  override def default = N

  type Aspect = Value

	val N = Value("Aspect.N")
	val NE = Value("Aspect.NE")
	val E = Value("Aspect.E")
	val SE = Value("Aspect.SE")
	val S = Value("Aspect.S")
	val SW = Value("Aspect.SW")
	val W = Value("Aspect.W")
	val NW = Value("Aspect.NW")
}