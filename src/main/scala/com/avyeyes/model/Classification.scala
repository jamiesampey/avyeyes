package com.avyeyes.model

import com.avyeyes.model.enums._
import com.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheTriggerCause.AvalancheTriggerCause
import com.avyeyes.model.enums.AvalancheType.AvalancheType

case class Classification(avyType: AvalancheType,
                          trigger: AvalancheTrigger,
                          triggerCause: AvalancheTriggerCause,
                          interface: AvalancheInterface,
                          rSize: Double,
                          dSize: Double) {

  override def toString = s"${AvalancheType.toCode(avyType)}-${AvalancheTrigger.toCode(trigger)}${AvalancheTriggerCause.toCode(triggerCause)}-$rSize-$dSize-${AvalancheInterface.toCode(interface)}"
}
