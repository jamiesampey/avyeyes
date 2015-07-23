package com.avyeyes.model

import net.liftweb.json.CustomSerializer
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import org.joda.time.DateTime

case class AvalancheImage(
  createTime: DateTime = DateTime.now,
  avyExtId: String = "",
  filename: String = "",
  origFilename: String = "",
  mimeType: String = "",
  size: Int = -1)

object AvalancheImageJsonSerializer extends CustomSerializer[AvalancheImage](format => (
  {
    case json: JValue => AvalancheImage() // images are never deserialized
  },
  {
    case AvalancheImage(createTime, avyExtId, filename, origFilename, mimeType, size) =>
      ("filename" -> filename) ~
      ("mimeType" -> mimeType) ~
      ("size" -> size)
  }))
