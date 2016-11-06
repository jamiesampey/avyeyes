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
  slope: Slope,
  weather: Weather,
  classification: Classification,
  humanNumbers: HumanNumbers,
  perimeter: Seq[Coordinate],
  comments: Option[String]) {

  lazy val title: String = s"${dateToStr(date)}: $areaName"

  lazy val editKey: Long = createTime.getMillis / 1000
}
