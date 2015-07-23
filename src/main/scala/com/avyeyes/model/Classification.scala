package com.avyeyes.model

import com.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import com.avyeyes.model.enums._
import net.liftweb.json.{Extraction, CustomSerializer}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._
import com.avyeyes.model.JsonFormats.formats

case class Classification(avyType: AvalancheType = AvalancheType.U,
                          trigger: AvalancheTrigger = AvalancheTrigger.U,
                          interface: AvalancheInterface = AvalancheInterface.U,
                          rSize: Double = 0.0,
                          dSize: Double = 0.0) {
  override def toString = s"$avyType-$trigger-$rSize-$dSize-$interface"
}

object Classification {
  def fromString(str: String) = {
    val arr = str.split("-")
    Classification(
      avyType = AvalancheType.withName(arr(0)),
      trigger = AvalancheTrigger.withName(arr(1)),
      rSize = arr(2).toDouble,
      dSize = arr(3).toDouble,
      interface = AvalancheInterface.withName(arr(4))
    )
  }

  object JsonSerializer extends CustomSerializer[Classification](format => (
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
        ("avyType" -> Extraction.decompose(avyType)) ~
        ("trigger" -> Extraction.decompose(trigger)) ~
        ("interface" -> Extraction.decompose(interface)) ~
        ("rSize" -> rSize) ~
        ("dSize" -> dSize)
    }))
}

