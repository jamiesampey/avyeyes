package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.model.{Avalanche, AvalancheImage}
import com.google.inject.ImplementedBy

import scala.concurrent.Future

@ImplementedBy(classOf[AvalancheDao])
trait CachedDao {
  def countAvalanches(viewable: Option[Boolean]): Int
  def getAvalanche(extId: String): Option[Avalanche]
  def getAvalanches(query: AvalancheSpatialQuery): List[Avalanche]
  def getAvalanchesAdmin(query: AvalancheTableQuery): (List[Avalanche], Int, Int)
  private[data] def getAvalanchesFromDisk: Future[Seq[Avalanche]]

  def insertAvalanche(avalanche: Avalanche): Future[Unit]
  def updateAvalanche(avalanche: Avalanche): Future[Unit]
  def deleteAvalanche(extId: String): Future[Int]

  def getAvalancheImage(avyExtId: String, baseFilename: String): Future[Option[AvalancheImage]]
  def getAvalancheImages(avyExtId: String): Future[List[AvalancheImage]]

  def insertAvalancheImage(img: AvalancheImage): Future[Int]
  def countAvalancheImages(extId: String): Future[Int]
  def updateAvalancheImageCaption(avyExtId: String, baseFilename: String, caption: Option[String]): Future[Int]
  def updateAvalancheImageOrder(avyExtId: String, filenameOrder: List[String]): Future[List[Int]]
  def deleteAvalancheImage(avyExtId: String, filename: String): Future[Unit]
  private[data] def deleteOrphanAvalancheImages(): Future[Int]
}
