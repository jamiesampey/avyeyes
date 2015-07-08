package com.avyeyes.model

import org.joda.time.DateTime

case class AvalancheImage(
  createTime: DateTime,
  avyExtId: String,
  filename: String,
  origFilename: String,
  mimeType: String,
  size: Int)
