package com.jamiesampey.avyeyes.model

import com.jamiesampey.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

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

  private val titleDTF = DateTimeFormat.forPattern("MMM dd, yyyy")

  lazy val title: String = s"${date.toString(titleDTF)}: $areaName"

  lazy val editKey: Long = createTime.getMillis / 1000
}
