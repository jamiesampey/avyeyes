package com.avyeyes.model.enums

object ModeOfTravel extends DataCodeEnum {
  type ModeOfTravel = Value
  
  val U = Value(0)
  val SKIER = Value(1)
  val SNOWBOARDER = Value(2)
  val SNOWMOBILER = Value(3)
  val SNOWSHOER = Value(4)
  val HIKER = Value(5)
  val CLIMBER = Value(6)
  val MOTORIST = Value(7)
  val OTHER = Value(8)
}