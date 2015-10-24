package com.avyeyes.model

import com.avyeyes.util.Converters._
import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import org.joda.time.DateTime

case class Avalanche(
  createTime: DateTime,
  updateTime: DateTime,
  extId: String,
  viewable: Boolean,
  submitterEmail: String,
  submitterExp: ExperienceLevel,
  location: Coordinate,
  date: DateTime,
  areaName: String,
  scene: Scene,
  slope: Slope,
  classification: Classification,
  humanNumbers: HumanNumbers,
  perimeter: Seq[Coordinate],
  comments: Option[String]) {

  def getTitle() = s"${dateToStr(date)}: ${areaName}"
}
