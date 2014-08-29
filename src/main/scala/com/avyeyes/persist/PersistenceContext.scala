package com.avyeyes.persist

import com.avyeyes.model._
import net.liftweb.http.FileParamHolder
import com.avyeyes.service.AvalancheSearchCriteria

trait PersistenceContext {
  def dao: AvalancheDao
  
  def selectAvalanche(extId: String) = dao.selectAvalanche(extId)
  
  def selectViewableAvalanche(extId: String) = dao.selectViewableAvalanche(extId)
  
  def selectAvalanches(criteria: AvalancheSearchCriteria) = dao.selectAvalanches(criteria) 

  def insertAvalanche(avalanche: Avalanche) = dao.insertAvalanche(avalanche)

  def insertAvalancheImage(avyExtId: String, fph: FileParamHolder) = dao.insertAvalancheImage(avyExtId, fph)
  
  def selectAvalancheImage(avyExtId: String, filename: String) = dao.selectAvalancheImage(avyExtId, filename)
  
  def selectAvalancheImageFilenames(avyExtId: String) = dao.selectAvalancheImageFilenames(avyExtId)
}