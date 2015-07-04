package com.avyeyes.model.enums

object Aspect extends Enumeration with UISelectableEnum {
    type Aspect = Value
    
	val N = Value(0, "N")
	val NE = Value(1, "NE")
	val E = Value(2, "E")
	val SE = Value(3, "SE")
	val S = Value(4, "S")
	val SW = Value(5, "SW")
	val W = Value(6, "W")
	val NW = Value(7, "NW")
}