package avyeyes.model.enums

object AvalancheInterface extends DataCodeEnum {
  val STORM_SNOW = new DataCodeVal(1, "S", "Layer of recent storm snow") 
  val OLD_NEW_INTERFACE = new DataCodeVal(2, "I", "New/old snow interface")
  val OLD_SNOW = new DataCodeVal(3, "O", "Within old snow")
  val GROUND = new DataCodeVal(4, "G", "Ground, glacial ice, or firm")
}