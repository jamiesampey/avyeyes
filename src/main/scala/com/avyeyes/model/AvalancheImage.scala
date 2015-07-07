package com.avyeyes.model

import org.joda.time.DateTime

trait AvalancheImage {
  def createTime: DateTime
  def avyExtId: String
  def filename: String
  def origFilename: String
  def mimeType: String
  def size: Int
}
