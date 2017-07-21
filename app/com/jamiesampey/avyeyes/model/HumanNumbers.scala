package com.jamiesampey.avyeyes.model

import com.jamiesampey.avyeyes.model.enums.ModeOfTravel
import com.jamiesampey.avyeyes.model.enums.ModeOfTravel.ModeOfTravel

case class HumanNumbers(modeOfTravel: ModeOfTravel,
                        caught: Int,
                        partiallyBuried: Int,
                        fullyBuried: Int,
                        injured: Int,
                        killed: Int) {
  override def toString = s"${ModeOfTravel.toCode(modeOfTravel)},$caught,$partiallyBuried,$fullyBuried,$injured,$killed"
}
