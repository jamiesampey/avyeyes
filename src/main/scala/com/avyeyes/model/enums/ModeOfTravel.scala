package com.avyeyes.model.enums

object ModeOfTravel extends Enumeration with UISelectableEnum {
  type ModeOfTravel = Value
  
  val U = Value(0, "U")
  val Skier = Value(1, "skier")
  val Snowboarder = Value(2, "snowboarder")
  val Snowmobiler = Value(3, "snowmobiler")
  val Snowshoer = Value(4, "snowshoer")
  val Hiker = Value(5, "hiker")
  val Climber = Value(6, "climber")
  val Motorist = Value(7, "motorist")
  val Other = Value(8, "other")
}