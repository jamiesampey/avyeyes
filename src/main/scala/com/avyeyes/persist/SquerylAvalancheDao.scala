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
import org.squeryl.dsl.ast.ExpressionNode
import org.squeryl.dsl.ast.OrderByArg

class SquerylAvalancheDao(isAuthorizedSession: () => Boolean) extends AvalancheDao {
  def selectAvalanche(extId: String): Option[Avalanche] = {
    avalanches.where(a => a.extId === extId 
      and (a.viewable === true).inhibitWhen(isAuthorizedSession())).headOption
  }
   
  def selectAvalanches(query: AvalancheQuery) = {
    val fromDate = if (!query.fromDateStr.isEmpty) strToDate(query.fromDateStr) else EarliestAvyDate
    val toDate = if (!query.toDateStr.isEmpty) strToDate(query.toDateStr) else today.getTime

    val avyType = if (isNotBlank(query.avyTypeStr)) AvalancheType.withName(query.avyTypeStr) else AvalancheType.U
    val avyTrigger = if (isNotBlank(query.avyTriggerStr)) AvalancheTrigger.withName(query.avyTriggerStr) else AvalancheTrigger.U

    val northLimit = if (query.geo.isDefined) query.geo.get.northLimit else 0
    val eastLimit = if (query.geo.isDefined) query.geo.get.eastLimit else 0
    val southLimit = if (query.geo.isDefined) query.geo.get.southLimit else 0
    val westLimit = if (query.geo.isDefined) query.geo.get.westLimit else 0

    from(avalanches)(a => where(
      (a.viewable === getAvyViewableQueryVal(query.viewable).?)
      and (a.lat.between(southLimit, northLimit)).inhibitWhen(query.geo.isEmpty)
      and (a.lng.between(westLimit, eastLimit)).inhibitWhen(query.geo.isEmpty)
      and a.avyDate.between(fromDate, toDate)
      and (a.avyType === avyType).inhibitWhen(query.avyTypeStr.isEmpty)
      and (a.avyTrigger === avyTrigger).inhibitWhen(query.avyTriggerStr.isEmpty)
      and (a.rSize gte getAvySizeQueryVal(query.rSize).?)
      and (a.dSize gte getAvySizeQueryVal(query.dSize).?)
      and (a.caught gte getHumanNumberQueryVal(query.numCaught).?)
      and (a.killed gte getHumanNumberQueryVal(query.numKilled).?))
    select(a) orderBy(buildAvalancheOrderBy(a, query.orderBy, query.orderDirection)))
    .page(query.page, query.pageLimit).toList
  }  

  def countAvalanches(viewable: Boolean) = from(avalanches)(a => where(a.viewable === viewable) compute(count)).toInt
  
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
            a.avyType := updated.avyType, a.avyTrigger := updated.avyTrigger, a.avyInterface := updated.avyInterface,
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
  
  private def getAvyViewableQueryVal(viewable: Option[Boolean]): Option[Boolean] = viewable match {
    case None if isAuthorizedSession() => None // viewable criteria will NOT apply (ADMIN ONLY)
    case Some(bool) if (!bool && isAuthorizedSession()) => Some(false) // criteria: viewable == false (ADMIN ONLY)
    case _ => Some(true) // criteria: viewable == true
  }
  
  private def getAvySizeQueryVal(sizeStr: String): Option[Double] = 
    if (strToDblOrZero(sizeStr) > 0) Some(strToDblOrZero(sizeStr)) else None
    
  private def getHumanNumberQueryVal(numStr: String): Option[Int] = 
    if (strToIntOrNegOne(numStr) >= 0) Some(strToIntOrNegOne(numStr)) else None
    
  private def buildAvalancheOrderBy(a: Avalanche, field: String, dir: OrderDirection.Value): OrderByArg = dir match {
    case OrderDirection.ASC => new OrderByArg(fieldToExpNode(a, field)) asc
    case OrderDirection.DESC => new OrderByArg(fieldToExpNode(a, field)) desc
  }
    
  private def fieldToExpNode(a: Avalanche, field: String): ExpressionNode = field match {
    case "createTime" => a.createTime
    case "updateTime" => a.updateTime
    case "lat" => a.lat
    case "lng" => a.lng
    case "areaName" => a.areaName
    case "avyDate" => a.avyDate
    case "avyType" => a.avyType
    case "avyTrigger" => a.avyTrigger
    case "rSize" => a.rSize
    case "dSize" => a.dSize
    case "caught" => a.caught
    case "killed" => a.killed
    case _ => a.id
  }
}