package com.avyeyes.model

import net.liftweb.json.ext.DateTimeSerializer
import net.liftweb.json.{DefaultFormats, Formats}

object JsonFormats {
  implicit val formats: Formats = DefaultFormats + DateTimeSerializer
}
