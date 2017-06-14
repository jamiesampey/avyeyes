package com.avyeyes.model.serializers

import com.avyeyes.model._
import com.avyeyes.model.enums._
import org.joda.time.DateTime
import org.json4s.{CustomSerializer, Extraction}
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._
import org.apache.commons.lang3.StringEscapeUtils._


object AvalancheSerializer extends CustomSerializer[Avalanche]( implicit formats => (
  {
    case json: JValue => Avalanche(
      createTime = (json \ "createTime").extractOpt[DateTime].getOrElse(DateTime.now),
      updateTime = (json \ "updateTime").extractOpt[DateTime].getOrElse(DateTime.now),
      viewable = (json \ "viewable").extractOpt[Boolean].getOrElse(true),
      extId = (json \ "extId").extract[String],
      submitterEmail = (json \ "submitterEmail").extract[String],
      submitterExp = ExperienceLevel.fromCode((json \ "submitterExp").extract[String]),
      location = (json \ "location").extract[Coordinate],
      date = (json \ "date").extract[DateTime],
      areaName = (json \ "areaName").extract[String],
      slope = (json \ "slope").extract[Slope],
      weather = (json \ "weather").extract[Weather],
      classification = (json \ "classification").extract[Classification],
      humanNumbers = (json \ "humanNumbers").extract[HumanNumbers],
      perimeter = {
        // perimeter is set when SUBMITTING but not when UPDATING
        val perimeter = (json \ "perimeter").extract[String]
        if (perimeter.nonEmpty) perimeter.trim.split(" ").toSeq.map(Coordinate(_)) else Seq.empty
      },
      comments = (json \ "comments").extractOpt[String] match {
        case Some(text) if text.nonEmpty => Some(escapeJava(text))
        case _ => None
      }
    )
  },{
  case a: Avalanche =>
    ("createTime" -> Extraction.decompose(a.createTime)) ~
    ("updateTime" -> Extraction.decompose(a.updateTime)) ~
    ("viewable" -> a.viewable) ~
    ("extId" -> a.extId) ~
    ("submitterEmail" -> a.submitterEmail) ~
    ("submitterExp" -> ExperienceLevel.toCode(a.submitterExp)) ~
    ("location" -> Extraction.decompose(a.location)) ~
    ("date" -> Extraction.decompose(a.date)) ~
    ("areaName" -> a.areaName) ~
    ("slope" -> Extraction.decompose(a.slope)) ~
    ("weather" -> Extraction.decompose(a.weather)) ~
    ("classification" -> Extraction.decompose(a.classification)) ~
    ("humanNumbers" -> Extraction.decompose(a.humanNumbers)) ~
    ("perimeter" -> Extraction.decompose(a.perimeter)) ~
    ("comments" -> Extraction.decompose(a.comments))
  }
))
