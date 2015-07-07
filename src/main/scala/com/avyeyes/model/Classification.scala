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

object Classification {
  implicit def toString(c: Classification) = s"${c.avyType}-${c.trigger}-${c.rSize}-${c.dSize}-${c.interface}"

  implicit def fromString(str: String) = {
    val arr = str.split("-")
    Classification(
      avyType = AvalancheType.withName(arr(0)),
      trigger = AvalancheTrigger.withName(arr(1)),
      rSize = arr(2).toDouble,
      dSize = arr(3).toDouble,
      interface = AvalancheInterface.withName(arr(4))
    )
  }
}