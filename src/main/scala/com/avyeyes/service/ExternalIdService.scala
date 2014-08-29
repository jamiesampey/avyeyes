package com.avyeyes.service

import org.apache.commons.lang3.RandomStringUtils
import java.util.Date
import com.avyeyes.persist.SquerylPersistence
import com.avyeyes.util.AEConstants._
import com.google.common.cache._
import java.util.concurrent.TimeUnit
import net.liftweb.util.Props

trait ExternalIdService extends PersistenceService with SquerylPersistence {
  def reserveNewExtId: String = {
    var extIdAttempt = ""
    do {
      extIdAttempt = RandomStringUtils.random(ExtIdLength, ExtIdChars)
    } while (ExternalIdMaitreD.reservationExists(extIdAttempt) || findAvalanche(extIdAttempt).isDefined)
      
    ExternalIdMaitreD.reserve(extIdAttempt, new Date())
    logger.info(s"Reserved new extId $extIdAttempt. Current extIds reserve cache size is ${ExternalIdMaitreD.reservations}")
      
    extIdAttempt
  }
    
  def unreserveExtId(extId: String) {
    ExternalIdMaitreD.unreserve(extId)
    logger.info(s"Unreserved extId $extId")
  }
}

private object ExternalIdMaitreD {
  private val reservedExtIds: Cache[String, Date] = 
    CacheBuilder.newBuilder().expireAfterWrite(Props.getInt("extId.cacheExpireHours", 24), TimeUnit.HOURS).build()
  
  def reserve(extId: String, date: Date) = reservedExtIds.put(extId, date)

  def unreserve(extId: String) = reservedExtIds.invalidate(extId)
    
  def reservationExists(extId: String): Boolean = reservedExtIds.getIfPresent(extId) != null
  
  def reservations = reservedExtIds.size
}