package com.avyeyes.model.enums

object AvalancheType extends DataCodeEnum {
  val LOOSE_SNOW = new DataCodeVal(1, "L", "Loose-snow avalanche")
  val WET_LOOSE_SNOW = new DataCodeVal(2, "WL", "Wet loose-snow avalanche")
  val SOFT_SLAB = new DataCodeVal(3, "SS", "Soft slab avalanche")
  val HARD_SLAB = new DataCodeVal(4, "HS", "Hard slab avalanche")
  val WET_SLAB = new DataCodeVal(5, "WS", "Wet slab avalanche")
  val ICE_FALL = new DataCodeVal(6, "I", "Ice fall or avalanche")
  val SLUSH_FLOW = new DataCodeVal(7, "SF", "Slush flow")
  val CORNICE_FALL = new DataCodeVal(8, "C", "Cornice fall (w/o avalanche)")
  val ROOF_AVALANCHE = new DataCodeVal(9, "R", "Roof avalanche")
}