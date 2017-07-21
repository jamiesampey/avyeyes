package com.jamiesampey.avyeyes.model.enums

object ModeOfTravel extends AutocompleteEnum {
  override def default = empty

  type ModeOfTravel = Value
  
  val empty = Value("ModeOfTravel.empty")

  val Skier = Value("ModeOfTravel.Skier")
  val Snowboarder = Value("ModeOfTravel.Snowboarder")
  val Snowmobiler = Value("ModeOfTravel.Snowmobiler")
  val Snowshoer = Value("ModeOfTravel.Snowshoer")
  val Hiker = Value("ModeOfTravel.Hiker")
  val Climber = Value("ModeOfTravel.Climber")
  val Motorist = Value("ModeOfTravel.Motorist")
  val Other = Value("ModeOfTravel.Other")
}