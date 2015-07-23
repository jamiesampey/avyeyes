package com.avyeyes.model

import com.avyeyes.model.JsonFormats.formats
import com.avyeyes.model.enums.ModeOfTravel
import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.JsonDSL._
import net.liftweb.json.{CustomSerializer, Extraction}

case class HumanNumbers(modeOfTravel: ModeOfTravel,
                        caught: Int = -1,
                        partiallyBuried: Int = -1,
                        fullyBuried: Int = -1,
                        injured: Int = -1,
                        killed: Int = -1) {
  override def toString = s"${modeOfTravel.toString},$caught,$partiallyBuried,$fullyBuried,$injured,$killed"
}

object HumanNumbers {
  def fromString(str: String) = {
    val arr = str.split(',')
    HumanNumbers(
      ModeOfTravel.withName(arr(0)),
      arr(1).toInt,
      arr(2).toInt,
      arr(3).toInt,
      arr(4).toInt,
      arr(5).toInt
    )
  }

  object JsonSerializer extends CustomSerializer[HumanNumbers](format => (
    {
      case json: JValue =>
        HumanNumbers(
          modeOfTravel = (json \ "modeOfTravel").extract[ModeOfTravel],
          caught = (json \ "caught").extract[Int],
          partiallyBuried = (json \ "partiallyBuried").extract[Int],
          fullyBuried = (json \ "fullyBuried").extract[Int],
          injured = (json \ "injured").extract[Int],
          killed = (json \ "killed").extract[Int]
        )
    },
    {
      case HumanNumbers(modeOfTravel, caught, partiallyBuried, fullyBuried, injured, killed) =>
        ("modeOfTravel" -> Extraction.decompose(modeOfTravel)) ~
        ("caught" -> caught) ~
        ("partiallyBuried" -> partiallyBuried) ~
        ("fullyBuried" -> fullyBuried) ~
        ("injured" -> injured) ~
        ("killed" -> killed)
    }))
}