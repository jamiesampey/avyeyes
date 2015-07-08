package com.avyeyes.data

import com.avyeyes.model._
import com.avyeyes.model.enums.ExperienceLevel._
import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel
import org.joda.time.DateTime

object DbObjects {
  case class DbAvalanche(
    createTime: DateTime,
    updateTime: DateTime,
    extId: String,
    viewable: Boolean,
    submitterEmail: String,
    submitterExp: ExperienceLevel,
    location: Coordinate,
    areaName: String,
    date: DateTime,
    scene: Scene,
    slope: Slope,
    classification: Classification,
    humanNumbers: HumanNumbers,
    modeOfTravel: ModeOfTravel,
    comments: String,
    perimeter: List[Coordinate]
  ) extends Avalanche

  case class DbAvalancheImage(
    createTime: DateTime,
    avyExtId: String,
    filename: String,
    origFilename: String,
    mimeType: String,
    size: Int
  ) extends AvalancheImage

  case class DbAppUser(
    createTime: DateTime,
    email: String
  ) extends User

  case class DbAppUserRole(
    name: String
  ) extends UserRole
}
