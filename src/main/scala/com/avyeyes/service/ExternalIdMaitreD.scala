package com.avyeyes.service

import java.util.Date
import java.util.concurrent.TimeUnit

import com.google.common.cache._

object ExternalIdMaitreD {
  private val reservedExtIds: Cache[String, Date] = 
    CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build()
  
  def reserve(extId: String, date: Date) = reservedExtIds.put(extId, date)

  def unreserve(extId: String) = reservedExtIds.invalidate(extId)
    
  def reservationExists(extId: String): Boolean = reservedExtIds.getIfPresent(extId) != null
  
  def reservations = reservedExtIds.size
}