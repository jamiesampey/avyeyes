package avyeyes.model.enums

object ModeOfTravel extends DataCodeEnum {
  val SKIER = new DataCodeVal(1, "Skier")
  val SNOWBOARDER = new DataCodeVal(2, "Snowboarder")
  val SNOWMOBILER = new DataCodeVal(3, "Snowmobiler")
  val SNOWSHOER = new DataCodeVal(4, "Snowshoer")
  val HIKER = new DataCodeVal(5, "Hiker")
  val CLIMBER = new DataCodeVal(6, "Climber")
  val MOTORIST = new DataCodeVal(7, "Motorist")
  val OTHER = new DataCodeVal(8, "Other")
}