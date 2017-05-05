package com.avyeyes.model.serializers

import com.avyeyes.util.Converters.{dateToStr, strToDate}
import org.joda.time.DateTime
import org.json4s.{CustomSerializer, JNull}
import org.json4s.JsonAST.JString

object DateTimeSerializer extends CustomSerializer[DateTime]( implicit formats => (
  {
    case JString(s) => strToDate(s)
    case JNull => null
  },
  {
    case d: DateTime => JString(dateToStr(d))
  }
))
