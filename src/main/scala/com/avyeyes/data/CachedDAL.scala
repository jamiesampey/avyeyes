package com.avyeyes.data

import com.avyeyes.model.{Avalanche, AvalancheImage}

import scala.concurrent.Future

trait CachedDAL {
  def isUserAuthorized(email: String): Future[Boolean]

  def countAvalanches(viewable: Option[Boolean]): Int

  def getAvalanche(extId: String): Option[Avalanche]

  def getAvalanchesFromDisk: Seq[Avalanche]

  def getAvalancheFromDisk(extId: String): Option[Avalanche]

  def getAvalanches(query: AvalancheQuery): List[Avalanche]

  def getAvalanchesAdmin(query: AdminAvalancheQuery): (List[Avalanche], Int, Int)

  def insertAvalanche(avalanche: Avalanche): Unit

  def updateAvalanche(avalanche: Avalanche): Unit
  
  def deleteAvalanche(extId: String): Unit
  
  def insertAvalancheImage(img: AvalancheImage): Unit
  
  def getAvalancheImage(avyExtId: String, baseFilename: String): Option[AvalancheImage]

  def countAvalancheImages(extId: String): Int

  def getAvalancheImages(avyExtId: String): List[AvalancheImage]
  
  def deleteAvalancheImage(avyExtId: String, filename: String): Unit

  def deleteOrphanAvalancheImages: Seq[String]
}