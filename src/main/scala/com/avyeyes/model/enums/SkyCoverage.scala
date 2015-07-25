package com.avyeyes.model.enums

object SkyCoverage extends Enumeration {
  type SkyCoverage = Value
  
  val U = Value("SkyCoverage.U")
  val CLR = Value("SkyCoverage.CLR")
  val FEW = Value("SkyCoverage.FEW")
  val SCT = Value("SkyCoverage.SCT")
  val BKN = Value("SkyCoverage.BKN")
  val OVC = Value("SkyCoverage.OVC")
  val OBSCD = Value("SkyCoverage.OBSCD")
}