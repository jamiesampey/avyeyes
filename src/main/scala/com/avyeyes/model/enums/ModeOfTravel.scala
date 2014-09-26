package com.avyeyes.model.enums

object ModeOfTravel extends Enumeration with UISelectableEnum {
  type ModeOfTravel = Value
  
  val U = Value(0)
  val Skier = Value(1)
  val Snowboarder = Value(2)
  val Snowmobiler = Value(3)
  val Snowshoer = Value(4)
  val Hiker = Value(5)
  val Climber = Value(6)
  val Motorist = Value(7)
  val Other = Value(8)
}