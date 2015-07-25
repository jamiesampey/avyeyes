package com.avyeyes.model.enums

object ModeOfTravel extends AutocompleteEnum {
  override def default = U

  type ModeOfTravel = Value
  
  val U = Value("ModeOfTravel.U")
  val Skier = Value("ModeOfTravel.Skier")
  val Snowboarder = Value("ModeOfTravel.Snowboarder")
  val Snowmobiler = Value("ModeOfTravel.Snowmobiler")
  val Snowshoer = Value("ModeOfTravel.Snowshoer")
  val Hiker = Value("ModeOfTravel.Hiker")
  val Climber = Value("ModeOfTravel.Climber")
  val Motorist = Value("ModeOfTravel.Motorist")
  val Other = Value("ModeOfTravel.Other")
}