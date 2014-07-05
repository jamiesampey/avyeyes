package com.avyeyes.model

class AvalancheImg(val avyExtId: String, val filename: String, 
    val mimeType: String, val bytes: Array[Byte]) extends AvalancheObj {

  def this() = this("", "", "", Array.emptyByteArray)
}