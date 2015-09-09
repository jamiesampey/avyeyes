package com.avyeyes.model

import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.util.Helpers._
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
  perimeter: List[Coordinate],
  comments: Option[String]) {

  def getTitle() = s"${dateToStr(date)}: ${areaName}"

  def getExtUrl() = s"${getHttpsBaseUrl}/${extId}"
}
