package com.avyeyes.model.enums

object ModeOfTravel extends AutocompleteEnum {
  type ModeOfTravel = Value
  
  val U = Value(0, "U")
  val Skier = Value(1, "Skier")
  val Snowboarder = Value(2, "Snowboarder")
  val Snowmobiler = Value(3, "Snowmobiler")
  val Snowshoer = Value(4, "Snowshoer")
  val Hiker = Value(5, "Hiker")
  val Climber = Value(6, "Climber")
  val Motorist = Value(7, "Motorist")
  val Other = Value(8, "Other")
}