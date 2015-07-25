package com.avyeyes.model

import com.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import com.avyeyes.model.enums._
import net.liftweb.json.{Extraction, CustomSerializer}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._
import com.avyeyes.model.JsonConverters.formats

case class Classification(avyType: AvalancheType = AvalancheType.U,
                          trigger: AvalancheTrigger = AvalancheTrigger.U,
                          interface: AvalancheInterface = AvalancheInterface.U,
                          rSize: Double = 0.0,
                          dSize: Double = 0.0) {
  
  override def toString = s"$avyType-$trigger-$rSize-$dSize-$interface"
}
