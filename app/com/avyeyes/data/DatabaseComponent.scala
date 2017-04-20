package com.avyeyes.data

import com.avyeyes.model._
import com.avyeyes.model.enums.Direction.Direction
import com.avyeyes.model.enums.AvalancheInterface.AvalancheInterface
import com.avyeyes.model.enums.AvalancheTrigger.AvalancheTrigger
import com.avyeyes.model.enums.AvalancheTriggerModifier.AvalancheTriggerModifier
import com.avyeyes.model.enums.AvalancheType.AvalancheType
import com.avyeyes.model.enums.ExperienceLevel.ExperienceLevel
import com.avyeyes.model.enums.ModeOfTravel.ModeOfTravel
import com.avyeyes.model.enums.WindSpeed.WindSpeed
import org.joda.time.DateTime
import slick.jdbc.JdbcProfile

private[data] case class AvalancheTableRow(createTime: DateTime, updateTime: DateTime, extId: String, viewable: Boolean, submitterEmail: String, submitterExp: ExperienceLevel, areaName: String, date: DateTime, longitude: Double, latitude: Double, elevation: Int, aspect: Direction, angle: Int, perimeter: Seq[Coordinate], comments: Option[String])
private[data] case class AvalancheWeatherTableRow(avalanche: String, recentSnow: Int, recentWindSpeed: WindSpeed, recentWindDirection: Direction)
private[data] case class AvalancheClassificationTableRow(avalanche: String, avalancheType: AvalancheType, trigger: AvalancheTrigger, triggerModifier: AvalancheTriggerModifier, interface: AvalancheInterface, rSize: Double, dSize: Double)
private[data] case class AvalancheHumanTableRow(avalanche: String, modeOfTravel: ModeOfTravel, caught: Int, partiallyBuried: Int, fullyBuried: Int, injured: Int, killed: Int)
private[data] case class AppUserTableRow(createTime: DateTime, lastActivityTime: DateTime, email: String, passwordHash: Option[String])

private[data] trait DatabaseComponent {this: SlickColumnMappers =>
  protected val jdbcProfile: JdbcProfile
  import jdbcProfile.api._

  protected val AvalancheRows = TableQuery[AvalancheTable]
  protected val AvalancheWeatherRows = TableQuery[AvalancheWeatherTable]
  protected val AvalancheClassificationRows = TableQuery[AvalancheClassificationTable]
  protected val AvalancheHumanRows = TableQuery[AvalancheHumanTable]

  protected val AvalancheImageRows = TableQuery[AvalancheImageTable]

  protected val AppUserRows = TableQuery[AppUserTable]
  protected val AppUserRoleRows = TableQuery[AppUserRoleAssignmentTable]

  private[data] def createSchema = (
    AvalancheRows.schema ++
    AvalancheWeatherRows.schema ++
    AvalancheClassificationRows.schema ++
    AvalancheHumanRows.schema ++
    AvalancheImageRows.schema ++
    AppUserRows.schema ++
    AppUserRoleRows.schema
  ).create

  class AvalancheTable(tag: Tag) extends Table[AvalancheTableRow](tag, "avalanche") {
    def createTime = column[DateTime]("create_time")
    def updateTime = column[DateTime]("update_time")
    def extId = column[String]("external_id", O.PrimaryKey)
    def viewable = column[Boolean]("viewable")
    def submitterEmail = column[String]("submitter_email")
    def submitterExp = column[ExperienceLevel]("submitter_experience")
    def areaName = column[String]("area_name")
    def date = column[DateTime]("date")
    def longitude = column[Double]("longitude")
    def latitude = column[Double]("latitude")
    def elevation = column[Int]("elevation")
    def aspect = column[Direction]("aspect")
    def angle = column[Int]("angle")
    def perimeter = column[Seq[Coordinate]]("perimeter")
    def comments = column[Option[String]]("comments")

    def * = (createTime, updateTime, extId, viewable, submitterEmail, submitterExp, areaName, date,
      longitude, latitude, elevation, aspect, angle, perimeter, comments) <> (AvalancheTableRow.tupled, AvalancheTableRow.unapply)

  }

  class AvalancheWeatherTable(tag: Tag) extends Table[AvalancheWeatherTableRow](tag, "avalanche_weather") {
    def avalanche = column[String]("avalanche")
    def recentSnow = column[Int]("recent_snow")
    def recentWindSpeed = column[WindSpeed]("recent_wind_speed")
    def recentWindDirection = column[Direction]("recent_wind_direction")

    def * = (avalanche, recentSnow, recentWindSpeed, recentWindDirection) <> (AvalancheWeatherTableRow.tupled, AvalancheWeatherTableRow.unapply)

    def idx = index("avalanche_weather_extid_idx", avalanche, unique = true)
    def avalancheFk = foreignKey("avalanche_weather_extid_fk", avalanche, AvalancheRows)(a =>
      a.extId, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  class AvalancheClassificationTable(tag: Tag) extends Table[AvalancheClassificationTableRow](tag, "avalanche_classification") {
    def avalanche = column[String]("avalanche")
    def avalancheType = column[AvalancheType]("avalanche_type")
    def trigger = column[AvalancheTrigger]("trigger")
    def triggerModifier = column[AvalancheTriggerModifier]("trigger_modifier")
    def interface = column[AvalancheInterface]("interface")
    def rSize = column[Double]("r_size")
    def dSize = column[Double]("d_size")

    def * = (avalanche, avalancheType, trigger, triggerModifier, interface, rSize, dSize) <> (AvalancheClassificationTableRow.tupled, AvalancheClassificationTableRow.unapply)

    def idx = index("avalanche_classification_extid_idx", avalanche, unique = true)
    def avalancheFk = foreignKey("avalanche_classification_extid_fk", avalanche, AvalancheRows)(a =>
      a.extId, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  class AvalancheHumanTable(tag: Tag) extends Table[AvalancheHumanTableRow](tag, "avalanche_human") {
    def avalanche = column[String]("avalanche")
    def modeOfTravel = column[ModeOfTravel]("mode_of_travel")
    def caught = column[Int]("caught")
    def partiallyBuried = column[Int]("partially_buried")
    def fullyBuried = column[Int]("fully_buried")
    def injured = column[Int]("injured")
    def killed = column[Int]("killed")

    def * = (avalanche, modeOfTravel, caught, partiallyBuried, fullyBuried, injured, killed) <> (AvalancheHumanTableRow.tupled, AvalancheHumanTableRow.unapply)

    def idx = index("avalanche_humans_extid_idx", avalanche, unique = true)
    def avalancheFk = foreignKey("avalanche_human_extid_fk", avalanche, AvalancheRows)(a =>
      a.extId, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  class AvalancheImageTable(tag: Tag) extends Table[AvalancheImage](tag, "avalanche_image") {
    def createTime = column[DateTime]("create_time")
    def avalanche = column[String]("avalanche")
    def filename = column[String]("filename")
    def origFilename = column[String]("original_filename")
    def mimeType = column[String]("mime_type")
    def size = column[Int]("size")
    def sortOrder = column[Int]("sort_order")
    def caption = column[Option[String]]("caption")

    def * = (createTime, avalanche, filename, origFilename, mimeType, size, sortOrder, caption) <> (AvalancheImage.tupled, AvalancheImage.unapply)

    def pk = primaryKey("avalanche_image_pk", (avalanche, filename))
  }

  class AppUserTable(tag: Tag) extends Table[AppUserTableRow](tag, "app_user") {
    def createTime = column[DateTime]("create_time")
    def lastActivityTime = column[DateTime]("last_activity_time")
    def email = column[String]("email", O.PrimaryKey)
    def passwordHash = column[Option[String]]("password_hash")

    def * = (createTime, lastActivityTime, email, passwordHash) <> (AppUserTableRow.tupled, AppUserTableRow.unapply)
  }

  class AppUserRoleAssignmentTable(tag: Tag) extends Table[AvyEyesUserRole](tag, "app_user_role_assignment") {
    def email = column[String]("app_user")
    def role = column[String]("app_role")
    def pk = primaryKey("app_user_role_assignment_pk", (email, role))

    def * = (email, role) <> (AvyEyesUserRole.tupled, AvyEyesUserRole.unapply)
  }
}
