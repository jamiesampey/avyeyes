package com.avyeyes.model

import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

case class AvalancheImage(avyExtId: String,
                          filename: String,
                          origFilename: String,
                          mimeType: String,
                          size: Int
                          ) extends BaseDbObject

class AvalancheImages(tag: Tag) extends Table[AvalancheImage](tag, "avalanche_image") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def createTime = column[DateTime]("create_time")
  def avyExtId = column[String]("avalanche_external_id")
  def filename = column[String]("filename")
  def origFilename = column[String]("original_filename")
  def mimeType = column[String]("mime_type")
  def size = column[Int]("size")

  def * = (id, createTime, avyExtId, filename, origFilename, mimeType, size).<> (AvalancheImage.tupled, AvalancheImage.unapply _)
}