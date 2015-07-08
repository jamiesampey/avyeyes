package com.avyeyes.service

import java.util.concurrent.TimeUnit
import com.avyeyes.data.DiskDao
import com.avyeyes.util.Constants._
import com.avyeyes.util.Helpers._
import com.google.common.cache._
import net.liftweb.common.Loggable
import net.liftweb.util.Props
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.DateTime

trait ExternalIdService extends Loggable {
  
  def reserveNewExtId(implicit diskDao: DiskDao): String = {
    var extIdAttempt = ""
    var attemptCount = 0
    
    do {
      extIdAttempt = RandomStringUtils.random(ExtIdLength, ExtIdChars)
      attemptCount += 1
      if (attemptCount >= Props.getInt("extId.newIdAttemptLimit", 100)) {
        throw new RuntimeException("Could not find an available ID")
      }
    } while (ExtIdReservationCache.reservationExists(extIdAttempt)
        || diskDao.getAvalanche(extIdAttempt).isDefined
        || containsBadWord(extIdAttempt))
      
    ExtIdReservationCache.reserve(extIdAttempt, new DateTime)
    logger.info(s"Reserved new extId $extIdAttempt. Current extIds reserve cache size is ${ExtIdReservationCache.reservations}")
      
    extIdAttempt
  }
    
  def unreserveExtId(extId: String) {
    ExtIdReservationCache.unreserve(extId)
    logger.info(s"Unreserved extId $extId")
  }

  def reservationExists(extId: String) = ExtIdReservationCache.reservationExists(extId)
}

private object ExtIdReservationCache {
  private val cache: Cache[String, DateTime] =
    CacheBuilder.newBuilder().expireAfterWrite(Props.getInt("extId.cacheExpireHours", 4), TimeUnit.HOURS).build()
  
  def reserve(extId: String, dt: DateTime) = cache.put(extId, dt)

  def unreserve(extId: String) = cache.invalidate(extId)
    
  def reservationExists(extId: String): Boolean = cache.getIfPresent(extId) != null
  
  def reservations = cache.size
}