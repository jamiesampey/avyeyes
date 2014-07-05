package com.avyeyes.model

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._
import scala.collection.mutable.HashSet
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.AEConstants._
import org.apache.commons.lang3.RandomStringUtils

object AvalancheDb extends Schema {
    private val reservedExtIds: HashSet[String] = HashSet.empty
    
	val avalanches = table[Avalanche]("avalanche")
	val avalancheImages = table[AvalancheImg]("avalanche_img")
	
	on(avalanches)(a => declare(
		a.id is(primaryKey, autoIncremented),
		a.extId is(unique, indexed)
	))
	
    on(avalancheImages)(img => declare(
        img.id is(primaryKey, autoIncremented)
    ))
    
	def getAvalancheByExtId(extId: Option[String]): Option[Avalanche] = {
      if (isValidExtId(extId)) {
          transaction {
            avalanches.where(a => a.viewable === true and a.extId === extId.get).headOption
          }
      } else
        None
    }
	
	def reserveNewExtId: String = {
	    var extIdAttempt = ""
	    transaction {
            do {
                extIdAttempt = RandomStringUtils.random(EXT_ID_LENGTH, EXT_ID_CHARS)
            } while (reservedExtIds.contains(extIdAttempt) 
                || avalanches.where(a => a.extId === extIdAttempt).headOption.isDefined)
	    }
	    
        reservedExtIds += extIdAttempt
        extIdAttempt
	}
	
	def unreserveExtId(extId: String) {
	  reservedExtIds -= extId
	}
}