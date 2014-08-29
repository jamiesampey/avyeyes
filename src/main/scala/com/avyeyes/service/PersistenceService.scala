package com.avyeyes.service

import com.avyeyes.model._
import com.avyeyes.persist.PersistenceContext

import net.liftweb.common.Loggable
import net.liftweb.http.FileParamHolder

trait PersistenceService extends Loggable {
  this: PersistenceContext =>
  
  def findAvalanche(extId: String): Option[Avalanche] = selectAvalanche(extId)
  
  def findViewableAvalanche(extId: String): Option[Avalanche] = selectViewableAvalanche(extId)
  
  def findAvalanches(criteria: AvalancheSearchCriteria): List[Avalanche] = selectAvalanches(criteria) 

  def saveAvalanche(avalanche: Avalanche): Unit = insertAvalanche(avalanche)

  def saveAvalancheImage(avyExtId: String, fph: FileParamHolder): Unit = insertAvalancheImage(avyExtId, fph)
  
  def findAvalancheImage(avyExtId: String, filename: String): Option[AvalancheImg] = selectAvalancheImage(avyExtId, filename)
  
  def findAvalancheImageFilenames(avyExtId: String): List[String] = selectAvalancheImageFilenames(avyExtId)
}