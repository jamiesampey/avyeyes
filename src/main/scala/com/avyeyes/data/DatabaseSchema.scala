package com.avyeyes.data

import com.avyeyes.data.SlickColumnMappers._
import com.avyeyes.model._
import com.avyeyes.model.enums.ExperienceLevel._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

private[data] object DatabaseSchema {
  val Avalanches = TableQuery[AvalanchesTable]
  val AvalancheImages = TableQuery[AvalancheImagesTable]
  val Users = TableQuery[UsersTable]
  val UserRoles = TableQuery[UserRoleAssignmentTable]

  class AvalanchesTable(tag: Tag) extends Table[Avalanche](tag, "avalanche") {
    def createTime = column[DateTime]("create_time")
    def updateTime = column[DateTime]("update_time")
    def extId = column[String]("external_id", O.PrimaryKey)
    def viewable = column[Boolean]("viewable")
    def submitterEmail = column[String]("submitter_email")
    def submitterExp = column[ExperienceLevel]("submitter_experience")
    def location = column[Coordinate]("location")
    def areaName = column[String]("area_name")
    def date = column[DateTime]("date")
    def scene = column[Scene]("scene")
    def slope = column[Slope]("slope")
    def classification = column[Classification]("classification")
    def humanNumbers = column[HumanNumbers]("human_numbers")
    def perimeter = column[String]("perimeter")
    def comments = column[Option[String]]("comments")

    def * = (createTime, updateTime, extId, viewable, submitterEmail, submitterExp, location,
      areaName, date, scene, slope, classification, humanNumbers, perimeter, comments) <> (modelApply.tupled, modelUnapply)

    private val modelApply = (createTime: DateTime, updateTime: DateTime, extId: String,
                              viewable: Boolean, submitterEmail: String, submitterExp: ExperienceLevel,
                              location: Coordinate, areaName: String, date: DateTime,
                              scene: Scene, slope: Slope, classification: Classification,
                              humanNumbers: HumanNumbers, perimeter: String, comments: Option[String]) =>
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
        scene = scene,
        slope = slope,
        classification = classification,
        humanNumbers = humanNumbers,
        perimeter.split(" ").toList.map(Coordinate.fromString),
        comments
      )

    private val modelUnapply = (a: Avalanche) => Some(
      (a.createTime, a.updateTime, a.extId, a.viewable, a.submitterEmail, a.submitterExp, a.location,
        a.areaName, a.date, a.scene, a.slope, a.classification, a.humanNumbers,
        a.perimeter.map(Coordinate.toString).mkString(" ").trim, a.comments))
  }

  class AvalancheImagesTable(tag: Tag) extends Table[AvalancheImage](tag, "avalanche_image") {
    def createTime = column[DateTime]("create_time")
    def avyExtId = column[String]("avalanche_external_id")
    def filename = column[String]("filename")
    def origFilename = column[String]("original_filename")
    def mimeType = column[String]("mime_type")
    def size = column[Int]("size")
    def pk = primaryKey("pk_a", (avyExtId, filename))

    def * = (createTime, avyExtId, filename, origFilename, mimeType, size) <> (AvalancheImage.tupled, AvalancheImage.unapply)
  }

  class UsersTable(tag: Tag) extends Table[User](tag, "app_user") {
    def createTime = column[DateTime]("create_time")
    def email = column[String]("email", O.PrimaryKey)

    def * = (createTime, email) <> (User.tupled, User.unapply)
  }

  class UserRoleAssignmentTable(tag: Tag) extends Table[UserRole](tag, "app_user_role_assignment") {
    def email = column[String]("app_user")
    def role = column[String]("app_role")
    def pk = primaryKey("pk_a", (email, role))

    def * = (email, role) <> (UserRole.tupled, UserRole.unapply)
  }
}
