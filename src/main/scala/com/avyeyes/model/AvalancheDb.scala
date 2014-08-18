package com.avyeyes.model

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._
import scala.collection.mutable.HashMap
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.AEConstants._
import org.apache.commons.lang3.RandomStringUtils
import java.util.Date
import net.liftweb.common.Loggable

object AvalancheDb extends Schema with Loggable {
    private val reservedExtIds: HashMap[String, Date] = HashMap.empty
    
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
        } while (reservedExtIds.contains(extIdAttempt) 
            || avalanches.where(a => a.extId === extIdAttempt).headOption.isDefined)
	    
        reservedExtIds += (extIdAttempt -> new Date())
        logger.info("Reserved new extId " + extIdAttempt + ". Current reserved extIds: " + reservedExtIds.keySet)
        extIdAttempt
	}
	
	def unreserveExtId(extId: String) {
	  reservedExtIds -= extId
	  logger.info("Unreserved extId " + extId)
	}
}