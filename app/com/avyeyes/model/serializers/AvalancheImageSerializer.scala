package com.avyeyes.model.serializers

import com.avyeyes.model.AvalancheImage
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

object AvalancheImageSerializer extends CustomSerializer[AvalancheImage]( implicit formats => (
  {
    case _: JValue => ???
  },
  {
    case image: AvalancheImage =>
      ("filename" -> image.filename) ~
      ("mimeType" -> image.mimeType) ~
      ("size" -> image.size) ~
      ("caption" -> image.caption)
  }
))
