package com.avyeyes.model

import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel

case class HumanNumbers(modeOfTravel: ModeOfTravel,
                        caught: Int = -1,
                        partiallyBuried: Int = -1,
                        fullyBuried: Int = -1,
                        injured: Int = -1,
                        killed: Int = -1) {

  override def toString = s"${modeOfTravel.toString},$caught,$partiallyBuried,$fullyBuried,$injured,$killed"
}
