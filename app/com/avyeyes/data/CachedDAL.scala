package com.avyeyes.data

import com.avyeyes.model.{Avalanche, AvalancheImage, UserRole}
import com.google.inject.ImplementedBy

import scala.concurrent.Future

@ImplementedBy(classOf[MemoryMapCachedDAL])
trait CachedDAL {
  def userRoles(email: String): Future[Seq[UserRole]]

  def countAvalanches(viewable: Option[Boolean]): Int

  def getAvalanche(extId: String): Option[Avalanche]

  def getAvalanchesFromDisk: Future[Seq[Avalanche]]

  def getAvalancheFromDisk(extId: String): Future[Option[Avalanche]]

  def getAvalanches(query: AvalancheQuery): List[Avalanche]

  def getAvalanchesAdmin(query: AdminAvalancheQuery): (List[Avalanche], Int, Int)

  def insertAvalanche(avalanche: Avalanche): Future[Unit]

  def updateAvalanche(avalanche: Avalanche): Future[Unit]
  
  def deleteAvalanche(extId: String): Future[Int]
  
  def insertAvalancheImage(img: AvalancheImage): Future[Int]
  
  def countAvalancheImages(extId: String): Future[Int]

  def getAvalancheImage(avyExtId: String, baseFilename: String): Future[Option[AvalancheImage]]

  def getAvalancheImages(avyExtId: String): Future[List[AvalancheImage]]

  def updateAvalancheImageCaption(avyExtId: String, baseFilename: String, caption: Option[String]): Future[Int]

  def updateAvalancheImageOrder(avyExtId: String, filenameOrder: List[String]): Future[List[Int]]

  def deleteAvalancheImage(avyExtId: String, filename: String): Future[Unit]

  def deleteOrphanAvalancheImages: Future[Int]
}
