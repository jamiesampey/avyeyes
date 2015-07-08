package com.avyeyes.data

import com.avyeyes.model._
import com.avyeyes.service.ExternalIdService
import com.avyeyes.util.{UnauthorizedException, UserSession}
import net.liftweb.common.Loggable

class SlickRelationalDao(user: UserSession) extends DiskDao with ExternalIdService with Loggable {
  implicit val userSession: UserSession = user

  def selectUser(email: String): Option[User] = ???

  def isUserAuthorized(email: String): Boolean = ???

  def selectAvalanche(extId: String): Option[Avalanche] = {
    //avalanches.filter(_.extId === extId).headOption
    ???
  }

  def insertAvalanche(avalanche: Avalanche, submitterEmail: String) = {
    if (avalanche.viewable && !isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to insert a viewable avalanche")
    }

    ???
  }

  def updateAvalanche(updated: Avalanche) = {
    if (!isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to update avalanche")
    }

    ???
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
//      from(avalancheImages)(img => where(
//        img.avyExtId === avyExtId and img.filename === filename)
//        select img).headOption
    } else {
//      from(avalancheImages, avalanches)((img, a) => where(
//        a.extId === avyExtId
//          and (a.viewable === true).inhibitWhen(isAuthorizedSession)
//          and img.avyExtId === avyExtId
//          and img.filename === filename)
//        select img).headOption
    }

    ???
  }

  def countAvalancheImages(extId: String) = ???

  def selectAvalancheImagesMetadata(avyExtId: String) = {
//    from(avalancheImages, avalanches)((img, a) => where(
//      a.extId === avyExtId
//        and (a.viewable === true).inhibitWhen(isAuthorizedSession)
//        and img.avyExtId === a.extId)
//      select (img.filename, img.mimeType, img.size)).toList

    ???
  }

  def deleteAvalancheImage(avyExtId: String, fileBaseName: String) = {
    val deleteAllowed = isAuthorizedSession || reservationExists(avyExtId)

    deleteAllowed match {
      case false => throw new UnauthorizedException("Not authorized to delete image")
      case true => {
//        avalancheImages deleteWhere (img => img.filename like s"$fileBaseName%" and img.avyExtId === avyExtId)
//        setAvalancheUpdateTime(avyExtId)
      }
    }
  }

  def pruneImages(): Set[String] = {
//    val orphanImageExtIds = from(avalancheImages)(img => where(
//      img.avyExtId notIn(from(avalanches)(a => select(a.extId)))) select(img.avyExtId)).distinct.toSet
//
//    val imageExtIdsForDelete = orphanImageExtIds filter(!reservationExists(_))
//
//    if (imageExtIdsForDelete.size > 0) {
//      val orphanImageCount = from(avalancheImages)(img => where(
//        img.avyExtId in imageExtIdsForDelete) compute count).toInt
//
//      logger.info(s"Pruning $orphanImageCount orphan images for ${imageExtIdsForDelete.size}"
//        + " unfinished avalanche report(s)")
//      avalancheImages.deleteWhere(img => img.avyExtId in imageExtIdsForDelete)
//    } else {
//      logger.info("No orphan images found for pruning")
//    }
//
//    imageExtIdsForDelete

    ???
  }

//  private def setAvalancheUpdateTime(extId: String) = {
//    update(avalanches)(a => where(a.extId === extId)
//      set (a.updateTime := new Timestamp(System.currentTimeMillis)))
//  }
//
//  private def getAvyViewableQueryVal(viewable: Option[Boolean]): Option[Boolean] = viewable match {
//    case None if isAuthorizedSession => None // viewable criteria will NOT apply (ADMIN ONLY)
//    case Some(bool) if (!bool && isAuthorizedSession) => Some(false) // criteria: viewable == false (ADMIN ONLY)
//    case _ => Some(true) // criteria: viewable == true
//  }

//  private def buildOrderByArg(orderTuple: (OrderField.Value, OrderDirection.Value),
//                              a: Avalanche, userOpt: Option[User] = None): ExpressionNode = {
//    orderTuple._2 match {
//      case OrderDirection.asc => new OrderByArg(orderFieldToExpNode(orderTuple._1, a, userOpt)) asc
//      case OrderDirection.desc => new OrderByArg(orderFieldToExpNode(orderTuple._1, a, userOpt)) desc
//    }
//  }

//  private def orderFieldToExpNode(field: OrderField.Value, a: Avalanche, userOpt: Option[User]): ExpressionNode =
//    field match {
//      case OrderField.createTime => a.createTime
//      case OrderField.updateTime => a.updateTime
//      case OrderField.extId => a.extId
//      case OrderField.viewable => a.viewable
//      case OrderField.lat => a.location.latitude
//      case OrderField.lng => a.location.longitude
//      case OrderField.areaName => a.areaName
//      case OrderField.avyDate => a.date
//      case OrderField.avyType => a.classification.avyType
//      case OrderField.avyTrigger => a.classification.trigger
//      case OrderField.avyInterface => a.classification.interface
//      case OrderField.rSize => a.classification.rSize
//      case OrderField.dSize => a.classification.dSize
//      case OrderField.caught => a.humanNumbers.caught
//      case OrderField.killed => a.humanNumbers.killed
//
//      case OrderField.submitterEmail => userOpt match {
//        case Some(u) => u.email
//        case None => a.createTime
//      }
//    }
}
