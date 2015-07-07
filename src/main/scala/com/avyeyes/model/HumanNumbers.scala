package com.avyeyes.model

import com.avyeyes.model.enums.ModeOfTravel
import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel

case class HumanNumbers(caught: Int = -1,
                        partiallyBuried: Int = -1,
                        fullyBuried: Int = -1,
                        injured: Int = -1,
                        killed: Int = -1,
                        modeOfTravel: ModeOfTravel = ModeOfTravel.U)
