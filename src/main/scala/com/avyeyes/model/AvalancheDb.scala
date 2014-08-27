package com.avyeyes.model

import java.util.Date
import java.util.concurrent.TimeUnit

import org.apache.commons.lang3.RandomStringUtils
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

import com.avyeyes.util.AEConstants.ExtIdChars
import com.avyeyes.util.AEConstants.ExtIdLength
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder

import net.liftweb.common.Loggable

object AvalancheDb extends Schema with Loggable {
  private val reservedExtIds: Cache[String, Date] = 
    CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).build()
    
	val avalanches = table[Avalanche]("avalanche")
	val avalancheImages = table[AvalancheImg]("avalanche_img")
	val avalancheImageDropbox = table[AvalancheImg]("avalanche_img_dropbox")
	
	on(avalanches)(a => declare(
		a.id is(primaryKey, autoIncremented),
		a.extId is(unique, indexed)
	))
	
    on(avalancheImages)(img => declare(
        img.id is(primaryKey, autoIncremented),
        img.avyExtId is(indexed)
    ))
    
    on(avalancheImageDropbox)(img => declare(
        img.id is(primaryKey, autoIncremented)
    ))
    
	def getAvalancheByExtId(extId: Option[String]): Option[Avalanche] = {
        avalanches.where(a => a.viewable === true and a.extId === extId.get).headOption
    }
	
	def reserveNewExtId: String = {
	    var extIdAttempt = ""
        do {
            extIdAttempt = RandomStringUtils.random(ExtIdLength, ExtIdChars)
        } while (reservedExtIds.getIfPresent(extIdAttempt) != null
            || avalanches.where(a => a.extId === extIdAttempt).headOption.isDefined)
	    
        reservedExtIds.put(extIdAttempt, new Date())
        logger.info("Reserved new extId " + extIdAttempt + ". Current extIds reserve cache size is " 
          + reservedExtIds.size)
        extIdAttempt
	}
	
	def unreserveExtId(extId: String) {
	  reservedExtIds.invalidate(extId)
	  logger.info("Unreserved extId " + extId)
	}
}