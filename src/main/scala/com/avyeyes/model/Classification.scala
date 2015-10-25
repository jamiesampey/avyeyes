package com.avyeyes.model

import com.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import com.avyeyes.model.StringSerializers.enumValueToCode

case class Classification(avyType: AvalancheType,
                          trigger: AvalancheTrigger,
                          interface: AvalancheInterface,
                          rSize: Double,
                          dSize: Double) {
  override def toString = s"${enumValueToCode(avyType)}-${enumValueToCode(trigger)}-$rSize-$dSize-${enumValueToCode(interface)}"
}
