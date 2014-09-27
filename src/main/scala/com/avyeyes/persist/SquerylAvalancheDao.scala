package com.avyeyes.persist

import org.apache.commons.lang3.StringUtils.isNotBlank
import org.squeryl.PrimitiveTypeMode._

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist.AvyEyesSchema._
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._

import net.liftweb.util.Helpers.today

class SquerylAvalancheDao extends AvalancheDao {
  def selectViewableAvalanche(extId: String): Option[Avalanche] = {
    avalanches.where(a => a.viewable === true and a.extId === extId).headOption
  }
  
  def selectAvalanche(extId: String): Option[Avalanche] = {
    avalanches.where(a => a.extId === extId).headOption
  }
   
  def selectUnviewableAvalanches = { from(avalanches)(a => 
    where(a.viewable === false) select(a) orderBy(a.createTime asc)).toList
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

  def updateAvalanche(updated: Avalanche) = {
    update(avalanches)(a => where(a.extId === updated.extId)
      set(a.viewable := updated.viewable,
        a.submitterEmail := updated.submitterEmail,
        a.submitterExp := updated.submitterExp,
        a.areaName := updated.areaName,
        a.avyDate := updated.avyDate,
        a.sky := updated.sky,
        a.precip := updated.precip,
        a.aspect := updated.aspect,
        a.angle := updated.angle,
        a.avyType := updated.avyType,
        a.trigger := updated.trigger,
        a.bedSurface := updated.bedSurface,
        a.rSize := updated.rSize,
        a.dSize := updated.dSize,
        a.caught := updated.caught,
        a.partiallyBuried := updated.partiallyBuried,
        a.fullyBuried := updated.fullyBuried,
        a.injured := updated.injured,
        a.killed := updated.killed,
        a.modeOfTravel := updated.modeOfTravel,
        a.comments := updated.comments)) 
  }
  
  def insertAvalancheImage(avalancheImg: AvalancheImg) = avalancheImages insert avalancheImg
  
  def selectAvalancheImage(avyExtId: String, filename: String) = {
    from(avalancheImages)(img => where(
      img.avyExtId === avyExtId 
      and img.filename === filename) 
    select(img)).headOption
  }
  
  def selectAvalancheImageFilenames(avyExtId: String) = {
    from(avalancheImages)(img => where(img.avyExtId === avyExtId) select(img.filename)).toList
  }
  
  private def getAvySizeQueryVal(sizeStr: String): Option[Double] = 
    if (strToDblOrZero(sizeStr) > 0) Some(strToDblOrZero(sizeStr)) else None
    
  private def getHumanNumberQueryVal(numStr: String): Option[Int] = 
    if (strToIntOrNegOne(numStr) >= 0) Some(strToIntOrNegOne(numStr)) else None
}