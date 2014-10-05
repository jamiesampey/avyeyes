package com.avyeyes.persist

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
    val northLimit = if (query.geo.isDefined) query.geo.get.northLimit else 0
    val eastLimit = if (query.geo.isDefined) query.geo.get.eastLimit else 0
    val southLimit = if (query.geo.isDefined) query.geo.get.southLimit else 0
    val westLimit = if (query.geo.isDefined) query.geo.get.westLimit else 0
    
    val fromDate = query.fromDate match {case Some(date) => date; case None => EarliestAvyDate}
    val toDate = query.toDate match {case Some(date) => date; case None => today.getTime}

    val avyTypeQueryVal = query.avyType match {case Some(avyType) => avyType; case None => AvalancheType.U}
    val avyTriggerQueryVal = query.avyTrigger match {case Some(avyTrigger) => avyTrigger; case None => AvalancheTrigger.U}
    
    from(avalanches)(a => where(
      (a.viewable === getAvyViewableQueryVal(query.viewable).?)
        and (a.lat.between(southLimit, northLimit)).inhibitWhen(query.geo.isEmpty)
        and (a.lng.between(westLimit, eastLimit)).inhibitWhen(query.geo.isEmpty)
        and a.avyDate.between(fromDate, toDate)
        and (a.avyType === avyTypeQueryVal).inhibitWhen(query.avyType.isEmpty)
        and (a.avyTrigger === avyTriggerQueryVal).inhibitWhen(query.avyTrigger.isEmpty)
        and (a.rSize gte (query.rSize).?)
        and (a.dSize gte (query.dSize).?)
        and (a.caught gte (query.numCaught).?)
        and (a.killed gte (query.numKilled).?))
      select (a) orderBy (buildAvalancheOrderBy(a, query.orderBy, query.order)))
      .page(query.offset, query.limit).toList
  }

  def countAvalanches(viewable: Boolean) = from(avalanches)(a => where(a.viewable === viewable) compute (count)).toInt

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
          set (a.updateTime := new Timestamp(System.currentTimeMillis),
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
      select (img)).headOption
  }

  def selectAvalancheImagesMetadata(avyExtId: String) = {
    from(avalancheImages, avalanches)((img, a) => where(
      a.extId === avyExtId
        and (a.viewable === true).inhibitWhen(isAuthorizedSession())
        and img.avyExtId === a.extId)
      select (img.filename, img.mimeType, img.size)).toList
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
      set (a.updateTime := new Timestamp(System.currentTimeMillis)))
  }

  private def getAvyViewableQueryVal(viewable: Option[Boolean]): Option[Boolean] = viewable match {
    case None if isAuthorizedSession() => None // viewable criteria will NOT apply (ADMIN ONLY)
    case Some(bool) if (!bool && isAuthorizedSession()) => Some(false) // criteria: viewable == false (ADMIN ONLY)
    case _ => Some(true) // criteria: viewable == true
  }

  private def buildAvalancheOrderBy(a: Avalanche, orderBy: OrderBy.Value,
    order: Order.Value): OrderByArg = order match {
    case Order.Asc => new OrderByArg(orderByToExpNode(a, orderBy)) asc
    case Order.Desc => new OrderByArg(orderByToExpNode(a, orderBy)) desc
  }

  private def orderByToExpNode(a: Avalanche, orderBy: OrderBy.Value): ExpressionNode = orderBy match {
    case OrderBy.Id => a.id
    case OrderBy.CreateTime => a.createTime
    case OrderBy.UpdateTime => a.updateTime
    case OrderBy.Lat => a.lat
    case OrderBy.Lng => a.lng
    case OrderBy.AreaName => a.areaName
    case OrderBy.AvyDate => a.avyDate
    case OrderBy.AvyType => a.avyType
    case OrderBy.AvyTrigger => a.avyTrigger
    case OrderBy.AvyInterface => a.avyInterface
    case OrderBy.RSize => a.rSize
    case OrderBy.DSize => a.dSize
    case OrderBy.Caught => a.caught
    case OrderBy.Killed => a.killed
  }
}