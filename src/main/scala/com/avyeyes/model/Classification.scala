package com.avyeyes.model

import com.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import com.avyeyes.model.enums.{AvalancheInterface, AvalancheTrigger, AvalancheType}
import slick.driver.PostgresDriver.api._

case class Classification(avyType: AvalancheType = AvalancheType.U,
                          trigger: AvalancheTrigger = AvalancheTrigger.U,
                          interface: AvalancheInterface = AvalancheInterface.U,
                          rSize: Double = 0.0, dSize: Double = 0.0)

case class LiftedClassification(avyType: Rep[AvalancheType],
                                trigger: Rep[AvalancheTrigger],
                                interface: Rep[AvalancheInterface],
                                rSize: Rep[Double],
                                dSize: Rep[Double])