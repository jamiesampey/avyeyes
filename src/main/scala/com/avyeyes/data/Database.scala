package com.avyeyes.data

import com.avyeyes.model.enums.Aspect._
import com.avyeyes.model.enums.ModeOfTravel._
import com.avyeyes.model.enums.Precipitation._
import com.avyeyes.model.enums.SkyCoverage._
import com.avyeyes.model._
import com.avyeyes.model.enums.ExperienceLevel._
import org.joda.time.DateTime
import com.avyeyes.data.SlickColumnMappers._
import slick.driver.PostgresDriver.api._

private[data] object Database {
  val Avalanches = TableQuery[Avalanches]
  val AvalancheImages = TableQuery[AvalancheImages]
  val Users = TableQuery[Users]
  val UserRoles = TableQuery[UserRoles]

  class Avalanches(tag: Tag) extends Table[Avalanche](tag, "avalanche") {
    def createTime = column[DateTime]("create_time")
    def updateTime = column[DateTime]("update_time")
    def extId = column[String]("external_id", O.PrimaryKey)
    def viewable = column[Boolean]("viewable")
    def submitterEmail = column[String]("submitter_email")
    def submitterExp = column[ExperienceLevel]("submitter_experience")
    def location = column[Coordinate]("location")
    def areaName = column[String]("area_name")
    def date = column[DateTime]("date")
    def sky = column[SkyCoverage]("sky")
    def precip = column[Precipitation]("precip")
    def aspect = column[Aspect]("aspect")
    def angle = column[Int]("angle")
    def classification = column[Classification]("classification")
    def humanNumbers = column[HumanNumbers]("human_numbers")
    def modeOfTravel = column[ModeOfTravel]("mode_of_travel")
    def comments = column[Option[String]]("comments")
    def perimeter = column[String]("perimeter")

    def * = (createTime, updateTime, extId, viewable, submitterEmail, submitterExp, location,
      areaName, date, sky, precip, aspect, angle, classification, humanNumbers, modeOfTravel,
      comments, perimeter) <> (modelApply.tupled, modelUnapply)

    private val modelApply = (createTime: DateTime, updateTime: DateTime, extId: String,
                              viewable: Boolean, submitterEmail: String, submitterExp: ExperienceLevel,
                              location: Coordinate, areaName: String, date: DateTime,
                              sky: SkyCoverage, precip: Precipitation, aspect: Aspect, angle: Int,
                              classification: Classification, humanNumbers: HumanNumbers,
                              modeOfTravel: ModeOfTravel, comments: Option[String], perimeter: String) =>
      Avalanche(
        createTime = createTime,
        updateTime = updateTime,
        extId = extId,
        viewable = viewable,
        submitterEmail = submitterEmail,
        submitterExp = submitterExp,
        location = location,
        areaName = areaName,
        date = date,
        scene = Scene(sky, precip),
        slope = Slope(aspect, angle, location.altitude),
        classification = classification,
        humanNumbers = humanNumbers,
        modeOfTravel = modeOfTravel,
        comments,
        perimeter.split(" ").toList.map(Coordinate.fromString)
      )

    private val modelUnapply = (a: Avalanche) => Some(
      (a.createTime, a.updateTime, a.extId, a.viewable, a.submitterEmail, a.submitterExp, a.location,
        a.areaName, a.date, a.scene.skyCoverage, a.scene.precipitation, a.slope.aspect, a.slope.angle,
        a.classification, a.humanNumbers, a.modeOfTravel, a.comments,
        a.perimeter.map(Coordinate.toString).mkString(" ").trim
        ))
  }

  class AvalancheImages(tag: Tag) extends Table[AvalancheImage](tag, "avalanche_image") {
    def createTime = column[DateTime]("create_time")
    def avyExtId = column[String]("avalanche_external_id")
    def filename = column[String]("filename")
    def origFilename = column[String]("original_filename")
    def mimeType = column[String]("mime_type")
    def size = column[Int]("size")
    def pk = primaryKey("pk", (avyExtId, filename))

    def * = (createTime, avyExtId, filename, origFilename, mimeType, size) <> (AvalancheImage.tupled, AvalancheImage.unapply)
  }

  class Users(tag: Tag) extends Table[User](tag, "app_user") {
    def createTime = column[DateTime]("create_time")
    def email = column[String]("email", O.PrimaryKey)

    def * = (createTime, email) <> (User.tupled, User.unapply)
  }

  class UserRoles(tag: Tag) extends Table[UserRole](tag, "app_role") {
    def name = column[String]("name", O.PrimaryKey)

    def * = (name) <> (UserRole.apply, UserRole.unapply)
  }
}
