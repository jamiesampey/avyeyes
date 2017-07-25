package com.jamiesampey.avyeyes.model

import com.jamiesampey.avyeyes.model.enums._
import com.jamiesampey.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.jamiesampey.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.jamiesampey.avyeyes.model.enums.AvalancheTriggerModifier.AvalancheTriggerModifier
import com.jamiesampey.avyeyes.model.enums.AvalancheType.AvalancheType

case class Classification(avyType: AvalancheType,
                          trigger: AvalancheTrigger,
                          triggerModifier: AvalancheTriggerModifier,
                          interface: AvalancheInterface,
                          rSize: Double,
                          dSize: Double) {

  override def toString = s"${AvalancheType.toCode(avyType)}-${AvalancheTrigger.toCode(trigger)}${AvalancheTriggerModifier.toCode(triggerModifier)}-$rSize-$dSize-${AvalancheInterface.toCode(interface)}"
}
