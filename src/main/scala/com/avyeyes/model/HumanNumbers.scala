package com.avyeyes.model

import com.avyeyes.model.enums.ModeOfTravel
import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel
import slick.driver.PostgresDriver.api._

case class HumanNumbers(caught: Int = -1,
                        partiallyBuried: Int = -1,
                        fullyBuried: Int = -1,
                        injured: Int = -1,
                        killed: Int = -1,
                        modeOfTravel: ModeOfTravel = ModeOfTravel.U)

case class LiftedHumanNumbers(caught: Rep[Int],
                               partiallyBuried: Rep[Int],
                               fullyBuried: Rep[Int],
                               injured: Rep[Int],
                               killed: Rep[Int],
                               modeOfTravel: Rep[ModeOfTravel])