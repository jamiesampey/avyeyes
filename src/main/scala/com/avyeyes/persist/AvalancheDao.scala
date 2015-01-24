package com.avyeyes.persist

import com.avyeyes.model._

trait AvalancheDao {
  def selectAvalanche(extId: String): Option[Avalanche]
  
  def selectAvalanches(query: AvalancheQuery): List[Avalanche]

  def selectAvalanchesForAdminTable(query: AdminAvalancheQuery): (List[Avalanche], Int, Int)

  def countAvalanches(viewable: Option[Boolean]): Int
  
  def insertAvalanche(avalanche: Avalanche, submitterEmail: String): Unit

  def updateAvalanche(avalanche: Avalanche): Unit
  
  def deleteAvalanche(extId: String): Unit
  
  def insertAvalancheImage(img: AvalancheImage): Unit
  
  def selectAvalancheImage(avyExtId: String, filename: String): Option[AvalancheImage]

  def countAvalancheImages(extId: String): Int

  def selectAvalancheImagesMetadata(avyExtId: String): List[(String, String, Int)]
  
  def deleteAvalancheImage(avyExtId: String, filename: String): Unit

  def performMaintenance(): Unit
}