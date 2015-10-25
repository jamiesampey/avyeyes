package com.avyeyes.model

import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel
import com.avyeyes.model.StringSerializers.enumValueToCode

case class HumanNumbers(modeOfTravel: ModeOfTravel,
                        caught: Int,
                        partiallyBuried: Int,
                        fullyBuried: Int,
                        injured: Int,
                        killed: Int) {
  override def toString = s"${enumValueToCode(modeOfTravel)},$caught,$partiallyBuried,$fullyBuried,$injured,$killed"
}
