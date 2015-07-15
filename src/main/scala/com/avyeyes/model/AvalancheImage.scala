package com.avyeyes.model

import net.liftweb.json.JsonAST.{JInt, JString, JField, JObject}
import org.joda.time.DateTime

case class AvalancheImage(
  createTime: DateTime,
  avyExtId: String,
  filename: String,
  origFilename: String,
  mimeType: String,
  size: Int) {

  def toJson = JObject(List(
    JField("filename", JString(filename)),
    JField("mimeType", JString(mimeType)),
    JField("size", JInt(size))
  ))
}
