package com.avyeyes.model

import com.avyeyes.model.enums.ModeOfTravel
import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel

case class HumanNumbers(modeOfTravel: ModeOfTravel,
                        caught: Int = -1,
                        partiallyBuried: Int = -1,
                        fullyBuried: Int = -1,
                        injured: Int = -1,
                        killed: Int = -1)

object HumanNumbers {
  implicit def toString(hn: HumanNumbers) =
    s"${hn.modeOfTravel.toString},${hn.caught},${hn.partiallyBuried},${hn.fullyBuried},${hn.injured},${hn.killed}"

  implicit def fromString(str: String) = {
    val arr = str.split(',')
    HumanNumbers(
      ModeOfTravel.withName(arr(0)),
      arr(1).toInt,
      arr(2).toInt,
      arr(3).toInt,
      arr(4).toInt,
      arr(5).toInt
    )
  }
}