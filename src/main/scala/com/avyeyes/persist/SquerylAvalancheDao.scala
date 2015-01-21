package com.avyeyes.persist

import java.sql.Timestamp

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist.AvyEyesSchema._
import com.avyeyes.persist.OrderDirection._
import com.avyeyes.service.ExternalIdMaitreD
import com.avyeyes.util.Constants._
import com.avyeyes.util.UnauthorizedException
import net.liftweb.common.Loggable
import net.liftweb.util.Helpers.today
import com.avyeyes.persist.AvyEyesSqueryl._
import org.squeryl.dsl.ast.{ExpressionNode, OrderByArg}

class SquerylAvalancheDao(isAuthorizedSession: () => Boolean) extends AvalancheDao with Loggable {
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
      select (a) orderBy(query.orderBy map(orderTuple => buildOrderByArg(a, orderTuple))))
      .page(query.offset, query.limit).toList
  }

  def countAvalanches(viewable: Option[Boolean]) = from(avalanches)(a =>
    where(a.viewable === viewable.?) compute (count)).toInt

  def insertAvalanche(avalanche: Avalanche, submitterEmail: String) = {
    if (avalanche.viewable && !isAuthorizedSession()) {
      throw new UnauthorizedException("Not authorized to insert a viewable avalanche")
    }

    users.where(u => u.email === submitterEmail).headOption match {
      case Some(existingUser) => avalanche.submitterId = existingUser.id
      case None => {
        val newUser = users insert User(submitterEmail)
        avalanche.submitterId = newUser.id
      }
    }

    avalanches insert avalanche
  }

  def updateAvalanche(updated: Avalanche) = {
    if (!isAuthorizedSession()) {
      throw new UnauthorizedException("Not authorized to update avalanche")
    }

    update(avalanches)(a => where(a.extId === updated.extId)
      set (a.updateTime := new Timestamp(System.currentTimeMillis),
        a.viewable := updated.viewable, a.submitterExp := updated.submitterExp,
        a.areaName := updated.areaName, a.avyDate := updated.avyDate,
        a.sky := updated.sky, a.precip := updated.precip,
        a.aspect := updated.aspect, a.angle := updated.angle,
        a.avyType := updated.avyType, a.avyTrigger := updated.avyTrigger, a.avyInterface := updated.avyInterface,
        a.rSize := updated.rSize, a.dSize := updated.dSize,
        a.caught := updated.caught, a.partiallyBuried := updated.partiallyBuried,
        a.fullyBuried := updated.fullyBuried, a.injured := updated.injured, a.killed := updated.killed,
        a.modeOfTravel := updated.modeOfTravel, a.comments := updated.comments))
  }

  def deleteAvalanche(extId: String) = {
    isAuthorizedSession() match {
      case false => throw new UnauthorizedException("Not authorized to delete avalanches")
      case true => {
        avalancheImages deleteWhere (img => img.avyExtId === extId)
        avalanches deleteWhere (a => a.extId === extId)
      }
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

  def countAvalancheImages(extId: String) = from(avalancheImages)(img => where(img.avyExtId === extId) compute (count)).toInt

  def selectAvalancheImagesMetadata(avyExtId: String) = {
    from(avalancheImages, avalanches)((img, a) => where(
      a.extId === avyExtId
        and (a.viewable === true).inhibitWhen(isAuthorizedSession())
        and img.avyExtId === a.extId)
      select (img.filename, img.mimeType, img.size)).toList
  }

  def deleteAvalancheImage(avyExtId: String, filename: String) = {
    isAuthorizedSession() match {
      case false => throw new UnauthorizedException("Not authorized to delete image")
      case true => {
        avalancheImages deleteWhere (img => img.avyExtId === avyExtId and img.filename === filename)
        setAvalancheUpdateTime(avyExtId)
      }
    }
  }

  def performMaintenance() = {
    val orphanImageAvyExtIdsFromDb = from(avalancheImages)(img => where(
      img.avyExtId notIn(from(avalanches)(a => select(a.extId)))) select(img.avyExtId)).distinct.toList
    val orphanImageAvyExtIds = orphanImageAvyExtIdsFromDb filter(extId => !ExternalIdMaitreD.reservationExists(extId))

    logger.info(s"Deleting orphan images for ${orphanImageAvyExtIds.size} unfinished avalanche reports")
    avalancheImages.deleteWhere(img => img.avyExtId in orphanImageAvyExtIds)
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

  private def buildOrderByArg(a: Avalanche, orderTuple: (OrderField.Value, OrderDirection.Value)): ExpressionNode = {
    orderTuple._2 match {
      case `asc` => new OrderByArg(orderFieldToExpNode(a, orderTuple._1)) asc
      case `desc` => new OrderByArg(orderFieldToExpNode(a, orderTuple._1)) desc
    }
  }

  private def orderFieldToExpNode(a: Avalanche, field: OrderField.Value): ExpressionNode = field match {
    case OrderField.Id => a.id
    case OrderField.CreateTime => a.createTime
    case OrderField.UpdateTime => a.updateTime
    case OrderField.ExternalId => a.extId
    case OrderField.Viewable => a.viewable
    case OrderField.Lat => a.lat
    case OrderField.Lng => a.lng
    case OrderField.AreaName => a.areaName
    case OrderField.AvyDate => a.avyDate
    case OrderField.AvyType => a.avyType
    case OrderField.AvyTrigger => a.avyTrigger
    case OrderField.AvyInterface => a.avyInterface
    case OrderField.RSize => a.rSize
    case OrderField.DSize => a.dSize
    case OrderField.Caught => a.caught
    case OrderField.Killed => a.killed
    case OrderField.SubmitterEmail => a.submitter.single.email
  }
}