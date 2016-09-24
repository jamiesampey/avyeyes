package com.avyeyes.data

import com.avyeyes.model.{Avalanche, AvalancheImage}

import scala.concurrent.Future

trait CachedDAL {
  def isUserAuthorized(email: String): Future[Boolean]

  def countAvalanches(viewable: Option[Boolean]): Int

  def getAvalanche(extId: String): Option[Avalanche]

  def getAvalanchesFromDisk: Future[Seq[Avalanche]]

  def getAvalancheFromDisk(extId: String): Future[Option[Avalanche]]

  def getAvalanches(query: AvalancheQuery): List[Avalanche]

  def getAvalanchesAdmin(query: AdminAvalancheQuery): (List[Avalanche], Int, Int)

  def insertAvalanche(avalanche: Avalanche): Future[Unit]

  def updateAvalanche(avalanche: Avalanche): Future[Unit]
  
  def deleteAvalanche(extId: String): Future[Int]
  
  def insertAvalancheImage(img: AvalancheImage): Unit
  
  def countAvalancheImages(extId: String): Future[Int]

  def getAvalancheImage(avyExtId: String, baseFilename: String): Future[Option[AvalancheImage]]

  def getAvalancheImages(avyExtId: String): Future[List[AvalancheImage]]

  def updateAvalancheImageCaption(avyExtId: String, baseFilename: String, caption: Option[String]): Future[Int]

  def updateAvalancheImageOrder(avyExtId: String, filenameOrder: List[String]): Unit

  def deleteAvalancheImage(avyExtId: String, filename: String): Unit

  def deleteOrphanAvalancheImages: Seq[String]
}