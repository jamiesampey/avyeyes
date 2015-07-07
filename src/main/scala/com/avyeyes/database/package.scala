package com.avyeyes

import com.avyeyes.model._
import com.avyeyes.model.enums.Aspect.Aspect
import com.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel
import com.avyeyes.model.enums.Precipitation.Precipitation
import com.avyeyes.model.enums.SkyCoverage.SkyCoverage
import com.avyeyes.database.DbObjects._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import com.avyeyes.model.Coordinate._

package object database {

  val avalanches = TableQuery[Avalanches]
  val avalancheImages = TableQuery[AvalancheImages]
  val users = TableQuery[Users]
  val userRoles = TableQuery[UserRoles]

  class Avalanches(tag: Tag) extends Table[DbAvalanche](tag, "avalanche") {
    def createTime = column[DateTime]("create_time")
    def updateTime = column[DateTime]("update_time")
    def extId = column[String]("external_id", O.PrimaryKey)
    def viewable = column[Boolean]("viewable")
    def submitterEmail = column[String]("submitter_email")
    def submitterExp = column[ExperienceLevel]("submitter_experience")
    def longitude = column[Double]("longitude")
    def latitude = column[Double]("latitude")
    def areaName = column[String]("area_name")
    def date = column[DateTime]("date")
    def sky = column[SkyCoverage]("sky")
    def precip = column[Precipitation]("precip")
    def elevation = column[Double]("elevation")
    def aspect = column[Aspect]("aspect")
    def angle = column[Int]("angle")
    def avyType = column[AvalancheType]("avalanche_type")
    def trigger = column[AvalancheTrigger]("avalanche_trigger")
    def interface = column[AvalancheInterface]("avalanche_interface")
    def rSize = column[Double]("r_size")
    def dSize = column[Double]("d_size")
    def caught = column[Int]("caught")
    def partiallyBuried = column[Int]("partially_buried")
    def fullyBuried = column[Int]("fully_buried")
    def injured = column[Int]("injured")
    def killed = column[Int]("killed")
    def modeOfTravel = column[ModeOfTravel]("mode_of_travel")
    def comments = column[String]("comments")
    def perimeter = column[String]("perimeter")

    def * = (createTime, updateTime, extId, viewable, submitterEmail, submitterExp, longitude,
      latitude, areaName, date, sky, precip, elevation, aspect, angle, avyType, trigger, interface,
      rSize, dSize, caught, partiallyBuried, fullyBuried, injured, killed, modeOfTravel, comments,
      perimeter) <> (modelApply.tupled, modelUnapply)

    private val modelApply = (createTime: DateTime, updateTime: DateTime, extId: String,
                              viewable: Boolean, submitterEmail: String, submitterExp: ExperienceLevel, longitude: Double,
                              latitude: Double, areaName: String, date: DateTime, sky: SkyCoverage, precip: Precipitation,
                              elevation: Int, aspect: Aspect, angle: Int, avyType: AvalancheType, trigger: AvalancheTrigger,
                              interface: AvalancheInterface, rSize: Double, dSize: Double, caught: Int, partiallyBuried: Int,
                              fullyBuried: Int, injured: Int, killed: Int, modeOfTravel: ModeOfTravel, comments: String,
                              perimeter: String) =>
      DbAvalanche(
        createTime = createTime,
        updateTime = updateTime,
        extId = extId,
        viewable = viewable,
        submitterEmail = submitterEmail,
        submitterExp = submitterExp,
        location = Coordinate(longitude, latitude, elevation),
        areaName = areaName,
        date = date,
        scene = Scene(sky, precip),
        slope = Slope(aspect, angle, elevation),
        classification = Classification(avyType, trigger, interface, rSize, dSize),
        humanNumbers = HumanNumbers(caught, partiallyBuried, fullyBuried, injured, killed, modeOfTravel),
        comments,
        perimeter.split(" ").toList.map(fromString)
      )

    private val modelUnapply = (a: DbAvalanche) => Some(
      (DateTime.now, DateTime.now, a.extId, a.viewable, a.submitterEmail, a.submitterExp, a.location.longitude,
        a.location.latitude, a.areaName, a.date, a.scene.skyCoverage, a.scene.precipitation, a.slope.elevation,
        a.slope.aspect, a.slope.angle, a.classification.avyType, a.classification.trigger, a.classification.interface,
        a.classification.rSize, a.classification.dSize, a.humanNumbers.caught, a.humanNumbers.partiallyBuried,
        a.humanNumbers.fullyBuried, a.humanNumbers.injured, a.humanNumbers.killed, a.humanNumbers.modeOfTravel,
        a.comments, a.perimeter.map(_.toString).mkString(" ").trim
      ))
  }

  class AvalancheImages(tag: Tag) extends Table[DbAvalancheImage](tag, "avalanche_image") {
    def createTime = column[DateTime]("create_time")
    def avyExtId = column[String]("avalanche_external_id")
    def filename = column[String]("filename")
    def origFilename = column[String]("original_filename")
    def mimeType = column[String]("mime_type")
    def size = column[Int]("size")
    def pk = primaryKey("pk", (avyExtId, filename))

    def * = (createTime, avyExtId, filename, origFilename, mimeType, size) <> (DbAvalancheImage.tupled, DbAvalancheImage.unapply)
  }

  class Users(tag: Tag) extends Table[DbAppUser](tag, "app_user") {
    def createTime = column[DateTime]("create_time")
    def email = column[String]("email", O.PrimaryKey)

    def * = (createTime, email) <> (DbAppUser.tupled, DbAppUser.unapply)
  }

  class UserRoles(tag: Tag) extends Table[DbAppUserRole](tag, "app_role") {
    def name = column[String]("name", O.PrimaryKey)

    def * = (name) <> (DbAppUserRole.apply, DbAppUserRole.unapply)
  }
}
