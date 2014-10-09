package com.avyeyes.model

case class AvalancheImage(avyExtId: String, filename: String, 
  mimeType: String, size: Int, bytes: Array[Byte]) extends SquerylDbObj {

  def this() = this("", "", "", 0, Array.emptyByteArray)
}