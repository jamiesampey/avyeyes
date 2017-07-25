package com.jamiesampey.avyeyes.model.serializers

import com.jamiesampey.avyeyes.model.HumanNumbers
import com.jamiesampey.avyeyes.model.enums._
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

object HumanNumbersSerializer extends CustomSerializer[HumanNumbers]( implicit formats => (
  {
    case json: JValue => HumanNumbers(
      caught = (json \ "caught").extract[Int],
      partiallyBuried = (json \ "partiallyBuried").extract[Int],
      fullyBuried = (json \ "fullyBuried").extract[Int],
      injured = (json \ "injured").extract[Int],
      killed = (json \ "killed").extract[Int],
      modeOfTravel = ModeOfTravel.fromCode((json \ "modeOfTravel").extract[String])
    )
  },{
  case hn: HumanNumbers =>
    ("modeOfTravel" -> ModeOfTravel.toCode(hn.modeOfTravel)) ~
    ("caught" -> hn.caught) ~
    ("partiallyBuried" -> hn.partiallyBuried) ~
    ("fullyBuried" -> hn.fullyBuried) ~
    ("injured" -> hn.injured) ~
    ("killed" -> hn.killed)
  }
))
