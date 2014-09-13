package com.avyeyes.persist

import org.apache.commons.lang3.StringUtils.isNotBlank
import org.squeryl.PrimitiveTypeMode._

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist.AvalancheSchema._
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._

import net.liftweb.common.Loggable
import net.liftweb.util.Helpers.today

class SquerylAvalancheDao extends AvalancheDao with Loggable {
  def selectViewableAvalanche(extId: String): Option[Avalanche] = {
    avalanches.where(a => a.viewable === true and a.extId === extId).headOption
  }
  
  def selectAvalanche(extId: String): Option[Avalanche] = {
    avalanches.where(a => a.extId === extId).headOption
  }
    
  def selectAvalanches(criteria: AvalancheSearchCriteria) = {
    val fromDate = if (!criteria.fromDateStr.isEmpty) strToDate(criteria.fromDateStr) else EarliestAvyDate
    val toDate = if (!criteria.toDateStr.isEmpty) strToDate(criteria.toDateStr) else today.getTime

    val avyType = if (isNotBlank(criteria.avyTypeStr)) AvalancheType.withName(criteria.avyTypeStr) else AvalancheType.U
    val avyTrigger = if (isNotBlank(criteria.avyTriggerStr)) AvalancheTrigger.withName(criteria.avyTriggerStr) else AvalancheTrigger.U

    from(avalanches)(a => where(
      a.viewable === true
      and a.lat.between(strToDblOrZero(criteria.southLimit), strToDblOrZero(criteria.northLimit))
      and a.lng.between(strToDblOrZero(criteria.westLimit), strToDblOrZero(criteria.eastLimit))
      and a.avyDate.between(fromDate, toDate)
      and (a.avyType === avyType).inhibitWhen(criteria.avyTypeStr.isEmpty)
      and (a.trigger === avyTrigger).inhibitWhen(criteria.avyTriggerStr.isEmpty)
      and (a.rSize gte getAvySizeQueryVal(criteria.rSize).?)
      and (a.dSize gte getAvySizeQueryVal(criteria.dSize).?)
      and (a.caught gte getHumanNumberQueryVal(criteria.numCaught).?)
      and (a.killed gte getHumanNumberQueryVal(criteria.numKilled).?))
    select(a)).toList
  }  

  def insertAvalanche(avalanche: Avalanche) = avalanches insert avalanche

  def insertAvalancheImage(avalancheImg: AvalancheImg) = avalancheImageDropbox insert avalancheImg
  
  def selectAvalancheImage(avyExtId: String, filename: String) = {
    from(avalancheImageDropbox)(img => where(
      img.avyExtId === avyExtId 
      and img.filename === filename) 
    select(img)).headOption
  }
  
  def selectAvalancheImageFilenames(avyExtId: String) = {
    from(avalancheImageDropbox)(img => where(img.avyExtId === avyExtId) select(img.filename)).toList
  }
  
  private def getAvySizeQueryVal(sizeStr: String): Option[Double] = 
    if (strToDblOrZero(sizeStr) > 0) Some(strToDblOrZero(sizeStr)) else None
    
  private def getHumanNumberQueryVal(numStr: String): Option[Int] = 
    if (strToIntOrNegOne(numStr) >= 0) Some(strToIntOrNegOne(numStr)) else None
}