package com.avyeyes.model

import com.avyeyes.model.Queries._
import com.avyeyes.model.enums.{AvalancheTrigger, AvalancheType}
import com.avyeyes.service.ExternalIdService
import com.avyeyes.util.{UnauthorizedException, UserSession}
import net.liftweb.common.Loggable
import org.joda.time.DateTime

import slick.driver.PostgresDriver.api._
import slick.lifted.CanBeQueryCondition

class SlickAvalancheDao(userSession: UserSession) extends AvalancheDao with ExternalIdService with Loggable {
  def isAuthorizedSession(): Boolean = userSession.isAuthorizedSession()

  implicit class QueryHelper[T, E](query: Query[T, E, Seq]) {
    def optionFilter[X, R: CanBeQueryCondition](name: Option[X])(f: (T, X) => R) =
      name.map(v => query.withFilter(f(_, v))).getOrElse(query)
  }

  def selectAvalanche(extId: String): Option[Avalanche] = {
    avalanches.filter(_.extId === extId).result.headOption
  }

  def selectAvalanches(q: AvalancheQuery) = {

    val latMax = if (q.geo.isDefined) q.geo.get.latMax else 0
    val latMin = if (q.geo.isDefined) q.geo.get.latMin else 0
    val lngMax = if (q.geo.isDefined) q.geo.get.lngMax else 0
    val lngMin = if (q.geo.isDefined) q.geo.get.lngMin else 0

    val fromDate = q.fromDate match {case Some(dt) => dt; case None => new DateTime(0)}
    val toDate = q.toDate match {case Some(dt) => dt; case None => DateTime.now}

    val avyTypeQueryVal = q.avyType match {case Some(avyType) => avyType; case None => AvalancheType.U}
    val avyTriggerQueryVal = q.avyTrigger match {case Some(avyTrigger) => avyTrigger; case None => AvalancheTrigger.U}

    avalanches.filter(_.longitude >= 5)
      .optionFilter(q.geo)(_.longitude >= lngMin && _.longitude <= lngMax && _.latitude >= latMin && _.latitude <= latMax)
      .optionFilter(q.fromDate)(_.date >= q.fromDate).list
  }

  def selectAvalanchesForAdminTable(query: AdminAvalancheQuery): (List[Avalanche], Int, Int) = {
    if (!isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized for admin select")
    }

    // TODO: select matching avalanches
  }

  def countAvalanches(viewable: Option[Boolean]) = {}

  def insertAvalanche(avalanche: Avalanche, submitterEmail: String) = {
    if (avalanche.viewable && !isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to insert a viewable avalanche")
    }

    // TODO: insert submitting user

    // TODO: insert avalanche
  }

  def updateAvalanche(updated: Avalanche) = {
    if (!isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to update avalanche")
    }

    // TODO: update existing avalanche
  }

  def deleteAvalanche(extId: String) = {
    isAuthorizedSession match {
      case false => throw new UnauthorizedException("Not authorized to delete avalanches")
      case true => {
        // TODO delete avalanche
      }
    }
  }

  def insertAvalancheImage(img: AvalancheImage) = {
    //TODO: insert image

    //TODO: set avlanche update time
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
