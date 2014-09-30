package com.avyeyes.persist

import org.apache.commons.lang3.StringUtils.isNotBlank
import org.squeryl.PrimitiveTypeMode._
import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist.AvyEyesSchema._
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._
import net.liftweb.util.Helpers.today
import com.avyeyes.util.UnauthorizedException
import java.sql.Timestamp

class SquerylAvalancheDao(isAuthorizedSession: () => Boolean) extends AvalancheDao {
  def selectAvalanche(extId: String): Option[Avalanche] = {
    avalanches.where(a => a.extId === extId 
      and (a.viewable === true).inhibitWhen(isAuthorizedSession())).headOption
  }
   
  def selectUnviewableAvalanches() = { 
    isAuthorizedSession() match {
      case true => from(avalanches)(a => where(a.viewable === false) select(a) orderBy(a.createTime asc)).toList
      case false => throw new UnauthorizedException("Not authorized to access avalanche list")
    }
  }
  
  def selectRecentlyUpdatedAvalanches(limit: Int) = {
    isAuthorizedSession() match {
      case true => from(avalanches)(a => select(a) orderBy(a.updateTime desc)).page(0, limit).toList
      case false => throw new UnauthorizedException("Not authorized to access avalanche list")
    }
  }
    
  def selectAvalanches(criteria: AvalancheSearchCriteria) = {
    val fromDate = if (!criteria.fromDateStr.isEmpty) strToDate(criteria.fromDateStr) else EarliestAvyDate
    val toDate = if (!criteria.toDateStr.isEmpty) strToDate(criteria.toDateStr) else today.getTime

    val avyType = if (isNotBlank(criteria.avyTypeStr)) AvalancheType.withName(criteria.avyTypeStr) else AvalancheType.U
    val avyTrigger = if (isNotBlank(criteria.avyTriggerStr)) AvalancheTrigger.withName(criteria.avyTriggerStr) else AvalancheTrigger.U

    from(avalanches)(a => where(
      (a.viewable === true).inhibitWhen(isAuthorizedSession())
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

  def insertAvalanche(avalanche: Avalanche) = {
    (avalanche.viewable && !isAuthorizedSession()) match {
      case false => avalanches insert avalanche
      case true => throw new UnauthorizedException("Not authorized to insert a viewable avalanche")
    }
  }
  
  def updateAvalanche(updated: Avalanche) = {
    isAuthorizedSession() match {
      case true => {
        update(avalanches)(a => where(a.extId === updated.extId)
          set(a.updateTime := new Timestamp(System.currentTimeMillis),
            a.viewable := updated.viewable, 
            a.submitterEmail := updated.submitterEmail, a.submitterExp := updated.submitterExp,
            a.areaName := updated.areaName, a.avyDate := updated.avyDate,
            a.sky := updated.sky, a.precip := updated.precip,
            a.aspect := updated.aspect, a.angle := updated.angle,
            a.avyType := updated.avyType, a.trigger := updated.trigger, a.bedSurface := updated.bedSurface,
            a.rSize := updated.rSize, a.dSize := updated.dSize,
            a.caught := updated.caught, a.partiallyBuried := updated.partiallyBuried,
            a.fullyBuried := updated.fullyBuried, a.injured := updated.injured, a.killed := updated.killed,
            a.modeOfTravel := updated.modeOfTravel, a.comments := updated.comments)) 
      }
      case false => throw new UnauthorizedException("Not authorized to update avalanches")
    }
  }
  
  def deleteAvalanche(extId: String) = {
    isAuthorizedSession() match {
      case true => {
        avalancheImages deleteWhere (img => img.avyExtId === extId)
        avalanches deleteWhere (a => a.extId === extId)
      }
      case false => throw new UnauthorizedException("Not authorized to delete avalanches")
    }
  }
  
  def insertAvalancheImage(img: AvalancheImage) = {
    avalancheImages insert img
    setAvalancheUpdateTime(img.avyExtId)
  }
  
  def selectAvalancheImage(avyExtId: String, filename: String) = {
    from(avalancheImages, avalanches)((img, a) => where(
      a.extId === avyExtId
      and (a.viewable === true).inhibitWhen(isAuthorizedSession())
      and img.avyExtId === a.extId 
      and img.filename === filename) 
    select(img)).headOption
  }
  
  def selectAvalancheImagesMetadata(avyExtId: String) = {
    from(avalancheImages, avalanches)((img, a) => where(
      a.extId === avyExtId
      and (a.viewable === true).inhibitWhen(isAuthorizedSession())
      and img.avyExtId === a.extId)
    select(img.filename, img.mimeType, img.size)).toList
  }
  
  def deleteAvalancheImage(avyExtId: String, filename: String) = {
    isAuthorizedSession() match {
      case true => {
        avalancheImages deleteWhere (img => img.avyExtId === avyExtId and img.filename === filename)
        setAvalancheUpdateTime(avyExtId)
      }
      case false => throw new UnauthorizedException("Not authorized to delete image")
    }
  }
  
  private def setAvalancheUpdateTime(extId: String) = {
    update(avalanches)(a => where(a.extId === extId)
      set(a.updateTime := new Timestamp(System.currentTimeMillis)))
  }
  
  private def getAvySizeQueryVal(sizeStr: String): Option[Double] = 
    if (strToDblOrZero(sizeStr) > 0) Some(strToDblOrZero(sizeStr)) else None
    
  private def getHumanNumberQueryVal(numStr: String): Option[Int] = 
    if (strToIntOrNegOne(numStr) >= 0) Some(strToIntOrNegOne(numStr)) else None
}