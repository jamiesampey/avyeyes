package com.avyeyes.model

import net.liftweb.json.JsonAST._

case class AvalancheImage(avyExtId: String, filename: String, 
    mimeType: String, bytes: Array[Byte]) extends SquerylDbObj {

  def this() = this("", "", "", Array.emptyByteArray)
  
  def toJObject(): JObject = JObject(List(
    JField("filename", JString(filename)),
    JField("mimeType", JString(mimeType)),
    JField("size", JDouble(bytes.length)) 
  ))
}