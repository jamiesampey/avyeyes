package com.avyeyes.model.enums

object Aspect extends Enumeration with UISelectableEnum {
    type Aspect = Value
    
	val N = Value(0)
	val NE = Value(1)
	val E = Value(2)
	val SE = Value(3)
	val S = Value(4)
	val SW = Value(5)
	val W = Value(6)
	val NW = Value(7)
}