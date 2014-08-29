package com.avyeyes.persist

import com.avyeyes.model._
import net.liftweb.http.FileParamHolder
import com.avyeyes.service.AvalancheSearchCriteria

trait AvalancheDao {
  def initSession(): Unit
  
  def selectAvalanche(extId: String): Option[Avalanche]
  
  def selectViewableAvalanche(extId: String): Option[Avalanche]
  
  def selectAvalanches(criteria: AvalancheSearchCriteria): List[Avalanche]  

  def insertAvalanche(avalanche: Avalanche): Unit

  def insertAvalancheImage(avyExtId: String, fph: FileParamHolder): Unit
  
  def selectAvalancheImage(avyExtId: String, filename: String): Option[AvalancheImg]
  
  def selectAvalancheImageFilenames(avyExtId: String): List[String]
}