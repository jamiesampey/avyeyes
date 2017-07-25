package com.jamiesampey.avyeyes.model

import org.joda.time.DateTime

case class AvalancheImage(
  createTime: DateTime,
  avalanche: String,
  filename: String,
  origFilename: String,
  mimeType: String,
  size: Int,
  sortOrder: Int,
  caption: Option[String] = None
)

