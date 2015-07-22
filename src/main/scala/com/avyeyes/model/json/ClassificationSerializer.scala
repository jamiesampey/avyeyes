package com.avyeyes.model.json

import com.avyeyes.model.Classification
import com.avyeyes.model.enums.AvalancheInterface._
import com.avyeyes.model.enums.AvalancheTrigger._
import com.avyeyes.model.enums.AvalancheType._
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._
import net.liftweb.json.{CustomSerializer, Extraction}
import com.avyeyes.model.json.JsonFormats.formats


class ClassificationSerializer extends CustomSerializer[Classification](format => (
  {
    case json: JValue =>
      Classification(
        avyType = (json \ "avyType").extract[AvalancheType],
        trigger = (json \ "trigger").extract[AvalancheTrigger],
        interface = (json \ "interface").extract[AvalancheInterface],
        rSize = (json \ "rSize").extract[Double],
        dSize = (json \ "dSize").extract[Double]
      )
  },
  {
    case Classification(avyType, trigger, interface, rSize, dSize) =>
      (("avyType" -> Extraction.decompose(avyType)) ~
        ("trigger" -> Extraction.decompose(trigger)) ~
        ("interface" -> Extraction.decompose(interface)) ~
        ("rSize" -> rSize) ~
        ("dSize" -> dSize))
  }))

