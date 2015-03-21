package com.avyeyes.service

import java.util.concurrent.TimeUnit

import com.avyeyes.persist.AvalancheDao
import com.avyeyes.util.Constants._
import com.avyeyes.util.Helpers._
import com.google.common.cache._
import net.liftweb.common.Loggable
import net.liftweb.util.Props
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.DateTime

trait ExternalIdService extends Loggable {
  
  def reserveNewExtId(implicit dao: AvalancheDao): String = {
    var extIdAttempt = ""
    var attemptCount = 0
    
    do {
      extIdAttempt = RandomStringUtils.random(ExtIdLength, ExtIdChars)
      attemptCount += 1
      if (attemptCount >= Props.getInt("extId.newIdAttemptLimit", 100)) {
        throw new RuntimeException("Could not find an available ID")
      }
    } while (ExternalIdMaitreD.reservationExists(extIdAttempt) 
        || dao.selectAvalanche(extIdAttempt).isDefined
        || containsBadWord(extIdAttempt))
      
    ExternalIdMaitreD.reserve(extIdAttempt, new DateTime)
    logger.info(s"Reserved new extId $extIdAttempt. Current extIds reserve cache size is ${ExternalIdMaitreD.reservations}")
      
    extIdAttempt
  }
    
  def unreserveExtId(extId: String) {
    ExternalIdMaitreD.unreserve(extId)
    logger.info(s"Unreserved extId $extId")
  }
}

object ExternalIdMaitreD {
  private val reservedExtIds: Cache[String, DateTime] =
    CacheBuilder.newBuilder().expireAfterWrite(Props.getInt("extId.cacheExpireHours", 24), TimeUnit.HOURS).build()
  
  def reserve(extId: String, dt: DateTime) = reservedExtIds.put(extId, dt)

  def unreserve(extId: String) = reservedExtIds.invalidate(extId)
    
  def reservationExists(extId: String): Boolean = reservedExtIds.getIfPresent(extId) != null
  
  def reservations = reservedExtIds.size
}