package com.avyeyes.service

import java.util.Date

import org.apache.commons.lang3.RandomStringUtils

import com.avyeyes.model._
import com.avyeyes.persist.PersistenceContext
import com.avyeyes.util.AEConstants._

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
  
  def reserveNewExtId: String = {
    var extIdAttempt = ""
    do {
      extIdAttempt = RandomStringUtils.random(ExtIdLength, ExtIdChars)
    } while (ExternalIdMaitreD.reservationExists(extIdAttempt) || findAvalanche(extIdAttempt).isDefined)
      
    ExternalIdMaitreD.reserve(extIdAttempt, new Date())
    logger.info("Reserved new extId " + extIdAttempt + ". Current extIds reserve cache size is " 
      + ExternalIdMaitreD.reservations)
      
    extIdAttempt
  }
    
  def unreserveExtId(extId: String) {
    ExternalIdMaitreD.unreserve(extId)
    logger.info("Unreserved extId " + extId)
  }
}