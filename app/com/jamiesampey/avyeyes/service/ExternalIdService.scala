package com.jamiesampey.avyeyes.service

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.jamiesampey.avyeyes.data.CachedDao
import com.jamiesampey.avyeyes.util.Constants._
import com.google.common.cache._
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.DateTime
import play.api.Logger

class ExternalIdService @Inject()(logger: Logger) {

  val NewExternalIdAttemptLimit = 100

  def reserveNewExtId(implicit dao: CachedDao): String = {
    var extIdAttempt = ""
    var attemptCount = 0

    do {
      extIdAttempt = RandomStringUtils.random(ExtIdLength, ExtIdChars)
      attemptCount += 1
      if (attemptCount >= NewExternalIdAttemptLimit) {
        throw new RuntimeException("Could not find an available ID")
      }
    } while (reservationExists(extIdAttempt) || dao.getAvalanche(extIdAttempt).isDefined)


    ExternalIdService.cache.put(extIdAttempt, new DateTime)
    logger.info(s"Reserved new extId $extIdAttempt. Current extIds reserve cache size is ${ExternalIdService.cache.size}")
      
    extIdAttempt
  }
    
  def unreserveExtId(extId: String) {
    ExternalIdService.cache.invalidate(extId)
    logger.info(s"Unreserved extId $extId")
  }

  def reservationExists(extId: String): Boolean = ExternalIdService.cache.getIfPresent(extId) != null
}

private object ExternalIdService {
  private val cache: Cache[String, DateTime] = CacheBuilder.newBuilder().expireAfterWrite(4, TimeUnit.HOURS).build()
}
