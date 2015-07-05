package com.avyeyes.persist

import java.sql.Timestamp

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist.AvyEyesSchema._
import com.avyeyes.service.ExternalIdService
import com.avyeyes.util.{UnauthorizedException, UserSession}
import net.liftweb.common.Loggable
import org.joda.time.DateTime

class SquerylAvalancheDao(userSession: UserSession) extends AvalancheDao with ExternalIdService with Loggable {
  def isAuthorizedSession(): Boolean = userSession.isAuthorizedSession()

  def selectAvalanche(extId: String): Option[Avalanche] = {
    avalanches.where(a => a.extId === extId
      and (a.viewable === true).inhibitWhen(isAuthorizedSession)).headOption
  }

  def selectAvalanches(query: AvalancheQuery) = {
    val latMax = if (query.geo.isDefined) query.geo.get.latMax else 0
    val latMin = if (query.geo.isDefined) query.geo.get.latMin else 0
    val lngMax = if (query.geo.isDefined) query.geo.get.lngMax else 0
    val lngMin = if (query.geo.isDefined) query.geo.get.lngMin else 0
    
    val fromDate = query.fromDate match {case Some(dt) => dt; case None => new DateTime(0)}
    val toDate = query.toDate match {case Some(dt) => dt; case None => DateTime.now}

    val avyTypeQueryVal = query.avyType match {case Some(avyType) => avyType; case None => AvalancheType.U}
    val avyTriggerQueryVal = query.avyTrigger match {case Some(avyTrigger) => avyTrigger; case None => AvalancheTrigger.U}
    
    from(avalanches)(a => where(
        (a.viewable === getAvyViewableQueryVal(query.viewable).?)
        and (a.location.latitude.between(latMin, latMax)).inhibitWhen(query.geo.isEmpty)
        and (a.location.longitude.between(lngMin, lngMax)).inhibitWhen(query.geo.isEmpty)
        and a.date.between(fromDate, toDate)
        and (a.classification.avyType === avyTypeQueryVal).inhibitWhen(query.avyType.isEmpty)
        and (a.classification.trigger === avyTriggerQueryVal).inhibitWhen(query.avyTrigger.isEmpty)
        and (a.classification.rSize gte (query.rSize).?)
        and (a.classification.dSize gte (query.dSize).?)
        and (a.humanNumbers.caught gte (query.numCaught).?)
        and (a.humanNumbers.killed gte (query.numKilled).?)
    ) select(a) orderBy(query.orderBy map(orderTuple => buildOrderByArg(orderTuple, a))))
    .page(query.offset, query.limit).toList
  }

  def selectAvalanchesForAdminTable(query: AdminAvalancheQuery): (List[Avalanche], Int, Int) = {
    if (!isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized for admin select")
    }

    val queryResult = from(avalanches, users)((a,u) => where(
      (a.submitterId === u.id)
        and ((lower(a.extId) like lower(query.extId)).inhibitWhen(query.extId.isEmpty)
          or (lower(a.areaName) like lower(query.areaName)).inhibitWhen(query.areaName.isEmpty)
          or (lower(u.email) like lower(query.submitterEmail)).inhibitWhen(query.submitterEmail.isEmpty))
    ) select(a) orderBy(query.orderBy map(orderTuple => buildOrderByArg(orderTuple, a, Some(u)))))

    (queryResult.page(query.offset, query.limit).toList, queryResult.size, countAvalanches(None))
  }

  def countAvalanches(viewable: Option[Boolean]) = from(avalanches)(a =>
    where(a.viewable === viewable.?) compute (count)).toInt

  def insertAvalanche(avalanche: Avalanche, submitterEmail: String) = {
    if (avalanche.viewable && !isAuthorizedSession) {
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
    if (!isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to update avalanche")
    }

    update(avalanches)(a => where(a.extId === updated.extId)
      set (a.updateTime := new Timestamp(System.currentTimeMillis),
        a.viewable := updated.viewable, a.submitterExp := updated.submitterExp,
        a.areaName := updated.areaName, a.date := updated.date,
        a.scene := updated.scene, a.slope := updated.slope,
        a.classification := updated.classification,
        a.humanNumbers := updated.humanNumbers,
        a.comments := updated.comments))
  }

  def deleteAvalanche(extId: String) = {
    isAuthorizedSession match {
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
    if (reservationExists(avyExtId)) {
      from(avalancheImages)(img => where(
        img.avyExtId === avyExtId and img.filename === filename)
        select img).headOption
    } else {
      from(avalancheImages, avalanches)((img, a) => where(
        a.extId === avyExtId
          and (a.viewable === true).inhibitWhen(isAuthorizedSession)
          and img.avyExtId === avyExtId
          and img.filename === filename)
        select img).headOption
    }
  }

  def countAvalancheImages(extId: String) = from(avalancheImages)(img => where(img.avyExtId === extId) compute (count)).toInt

  def selectAvalancheImagesMetadata(avyExtId: String) = {
    from(avalancheImages, avalanches)((img, a) => where(
      a.extId === avyExtId
        and (a.viewable === true).inhibitWhen(isAuthorizedSession)
        and img.avyExtId === a.extId)
      select (img.filename, img.mimeType, img.size)).toList
  }

  def deleteAvalancheImage(avyExtId: String, fileBaseName: String) = {
    val deleteAllowed = isAuthorizedSession || reservationExists(avyExtId)

    deleteAllowed match {
      case false => throw new UnauthorizedException("Not authorized to delete image")
      case true => {
        avalancheImages deleteWhere (img => img.filename like s"$fileBaseName%" and img.avyExtId === avyExtId)
        setAvalancheUpdateTime(avyExtId)
      }
    }
  }

  def pruneImages(): Set[String] = {
    val orphanImageExtIds = from(avalancheImages)(img => where(
      img.avyExtId notIn(from(avalanches)(a => select(a.extId)))) select(img.avyExtId)).distinct.toSet

    val imageExtIdsForDelete = orphanImageExtIds filter(!reservationExists(_))

    if (imageExtIdsForDelete.size > 0) {
      val orphanImageCount = from(avalancheImages)(img => where(
        img.avyExtId in imageExtIdsForDelete) compute count).toInt

      logger.info(s"Pruning $orphanImageCount orphan images for ${imageExtIdsForDelete.size}"
        + " unfinished avalanche report(s)")
      avalancheImages.deleteWhere(img => img.avyExtId in imageExtIdsForDelete)
    } else {
      logger.info("No orphan images found for pruning")
    }

    imageExtIdsForDelete
  }

  private def setAvalancheUpdateTime(extId: String) = {
    update(avalanches)(a => where(a.extId === extId)
      set (a.updateTime := new Timestamp(System.currentTimeMillis)))
  }

  private def getAvyViewableQueryVal(viewable: Option[Boolean]): Option[Boolean] = viewable match {
    case None if isAuthorizedSession => None // viewable criteria will NOT apply (ADMIN ONLY)
    case Some(bool) if (!bool && isAuthorizedSession) => Some(false) // criteria: viewable == false (ADMIN ONLY)
    case _ => Some(true) // criteria: viewable == true
  }

  private def buildOrderByArg(orderTuple: (OrderField.Value, OrderDirection.Value),
                              a: Avalanche, userOpt: Option[User] = None): ExpressionNode = {
    orderTuple._2 match {
      case OrderDirection.asc => new OrderByArg(orderFieldToExpNode(orderTuple._1, a, userOpt)) asc
      case OrderDirection.desc => new OrderByArg(orderFieldToExpNode(orderTuple._1, a, userOpt)) desc
    }
  }

  private def orderFieldToExpNode(field: OrderField.Value, a: Avalanche, userOpt: Option[User]): ExpressionNode =
    field match {
      case OrderField.id => a.id
      case OrderField.createTime => a.createTime
      case OrderField.updateTime => a.updateTime
      case OrderField.extId => a.extId
      case OrderField.viewable => a.viewable
      case OrderField.lat => a.location.latitude
      case OrderField.lng => a.location.longitude
      case OrderField.areaName => a.areaName
      case OrderField.avyDate => a.date
      case OrderField.avyType => a.classification.avyType
      case OrderField.avyTrigger => a.classification.trigger
      case OrderField.avyInterface => a.classification.interface
      case OrderField.rSize => a.classification.rSize
      case OrderField.dSize => a.classification.dSize
      case OrderField.caught => a.humanNumbers.caught
      case OrderField.killed => a.humanNumbers.killed

      case OrderField.submitterEmail => userOpt match {
        case Some(u) => u.email
        case None => a.id
      }
    }
}