package com.jamiesampey.avyeyes.model.enums

object AvalancheTrigger extends AutocompleteEnum {
  override def default = empty

  type AvalancheTrigger = Value

  val empty = Value("AvalancheTrigger.empty")

  val N = Value("AvalancheTrigger.Natural.N")
  val NC = Value("AvalancheTrigger.Natural.NC")
  val NE = Value("AvalancheTrigger.Natural.NE")
  val NI = Value("AvalancheTrigger.Natural.NI")
  val NL = Value("AvalancheTrigger.Natural.NL")
  val NS = Value("AvalancheTrigger.Natural.NS")
  val NR = Value("AvalancheTrigger.Natural.NR")
  val NO = Value("AvalancheTrigger.Natural.NO")
  val AA = Value("AvalancheTrigger.Explosive.AA")
  val AE = Value("AvalancheTrigger.Explosive.AE")
  val AL = Value("AvalancheTrigger.Explosive.AL")
  val AB = Value("AvalancheTrigger.Explosive.AB")
  val AC = Value("AvalancheTrigger.Explosive.AC")
  val AX = Value("AvalancheTrigger.Explosive.AX")
  val AH = Value("AvalancheTrigger.Explosive.AH")
  val AP = Value("AvalancheTrigger.Explosive.AP")
  val AW = Value("AvalancheTrigger.Miscellaneous.AW")
  val AU = Value("AvalancheTrigger.Miscellaneous.AU")
  val AO = Value("AvalancheTrigger.Miscellaneous.AO")
  val AM = Value("AvalancheTrigger.Vehicle.AM")
  val AK = Value("AvalancheTrigger.Vehicle.AK")
  val AV = Value("AvalancheTrigger.Vehicle.AV")
  val AS = Value("AvalancheTrigger.Human.AS")
  val AR = Value("AvalancheTrigger.Human.AR")
  val AI = Value("AvalancheTrigger.Human.AI")
  val AF = Value("AvalancheTrigger.Human.AF")
}