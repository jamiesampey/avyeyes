package com.avyeyes.data

import com.avyeyes.model.{User, Avalanche, AvalancheImage}

trait DiskDao extends AuthorizableDao {
  def getUser(email: String): Option[User]

  def isUserAuthorized(email: String): Boolean

  def getAllAvalanches(): List[Avalanche]

  def getAvalanche(extId: String): Option[Avalanche]
  
  def insertAvalanche(avalanche: Avalanche, submitterEmail: String): Unit

  def updateAvalanche(avalanche: Avalanche): Unit
  
  def deleteAvalanche(extId: String): Unit
  
  def insertAvalancheImage(img: AvalancheImage): Unit
  
  def getAvalancheImage(avyExtId: String, filename: String): Option[AvalancheImage]

  def countAvalancheImages(extId: String): Int

  def getAvalancheImagesMetadata(avyExtId: String): List[(String, String, Int)]
  
  def deleteAvalancheImage(avyExtId: String, fileBaseName: String): Unit

  def pruneImages(): Set[String]
}