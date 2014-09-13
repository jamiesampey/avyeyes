package com.avyeyes.persist

import com.avyeyes.model._
import net.liftweb.http.FileParamHolder

trait AvalancheDao {
  def selectAvalanche(extId: String): Option[Avalanche]
  
  def selectViewableAvalanche(extId: String): Option[Avalanche]
  
  def selectAvalanches(criteria: AvalancheSearchCriteria): List[Avalanche]  

  def insertAvalanche(avalanche: Avalanche): Unit

  def insertAvalancheImage(img: AvalancheImg): Unit
  
  def selectAvalancheImage(avyExtId: String, filename: String): Option[AvalancheImg]
  
  def selectAvalancheImageFilenames(avyExtId: String): List[String]
}