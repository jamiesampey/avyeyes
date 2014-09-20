package com.avyeyes.model

case class AvalancheImg(avyExtId: String, filename: String, 
    mimeType: String, bytes: Array[Byte]) extends SquerylDbObj {

  def this() = this("", "", "", Array.emptyByteArray)
}