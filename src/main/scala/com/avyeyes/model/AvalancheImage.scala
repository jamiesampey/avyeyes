package com.avyeyes.model

case class AvalancheImage(avyExtId: String, filename: String, origFilename: String,
  mimeType: String, size: Int) extends SquerylDbObj {

  def this() = this("", "", "", "", 0)
}