package com.avyeyes.model.enums

object AvalancheTrigger extends UISelectableEnum {
  type AvalancheTrigger = Value
  
  override def isCompositeLabel = true
  
  val U = Value(0, "U")
  val N = Value(1, "N")
  val NC = Value(2, "NC")
  val NE = Value(3, "NE")
  val NI = Value(4, "NI")
  val NL = Value(5, "NL")
  val NS = Value(6, "NS")
  val NR = Value(7, "NR")
  val NO = Value(8, "NO")
  val AA = Value(9, "AA")
  val AE = Value(10, "AE")
  val AL = Value(11, "AL")
  val AB = Value(12, "AB")
  val AC = Value(13, "AC")
  val AX = Value(14, "AX")
  val AH = Value(15, "AH")
  val AP = Value(16, "AP")
  val AW = Value(17, "AW")
  val AU = Value(18, "AU")
  val AO = Value(19, "AO")
  val AM = Value(20, "AM")
  val AK = Value(21, "AK")
  val AV = Value(22, "AV")
  val AS = Value(23, "AS")
  val AR = Value(24, "AR")
  val AI = Value(25, "AI")
  val AF = Value(26, "AF")
}