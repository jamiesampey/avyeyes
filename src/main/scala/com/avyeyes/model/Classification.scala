package com.avyeyes.model

import com.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import com.avyeyes.model.enums.{AvalancheInterface, AvalancheTrigger, AvalancheType}

case class Classification(avyType: AvalancheType = AvalancheType.U,
                          trigger: AvalancheTrigger = AvalancheTrigger.U,
                          interface: AvalancheInterface = AvalancheInterface.U,
                          rSize: Double = 0.0,
                          dSize: Double = 0.0)
