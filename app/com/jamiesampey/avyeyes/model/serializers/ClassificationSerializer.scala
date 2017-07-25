package com.jamiesampey.avyeyes.model.serializers

import com.jamiesampey.avyeyes.model.Classification
import com.jamiesampey.avyeyes.model.enums.{AvalancheInterface, AvalancheTrigger, AvalancheTriggerModifier, AvalancheType}
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

object ClassificationSerializer extends CustomSerializer[Classification]( implicit formats => (
  {
    case json: JValue => Classification(
      avyType = AvalancheType.fromCode((json \ "avyType").extract[String]),
      trigger = AvalancheTrigger.fromCode((json \ "trigger").extract[String]),
      triggerModifier = AvalancheTriggerModifier.fromCode((json \ "triggerModifier").extract[String]),
      interface = AvalancheInterface.fromCode((json \ "interface").extract[String]),
      rSize = (json \ "rSize").extract[Double],
      dSize = (json \ "dSize").extract[Double]
    )
  },{
  case cls: Classification =>
    ("avyType" -> AvalancheType.toCode(cls.avyType)) ~
    ("trigger" -> AvalancheTrigger.toCode(cls.trigger)) ~
    ("triggerModifier" -> AvalancheTriggerModifier.toCode(cls.triggerModifier)) ~
    ("interface" -> AvalancheInterface.toCode(cls.interface)) ~
    ("rSize" -> cls.rSize) ~
    ("dSize" -> cls.dSize)
  }
))
