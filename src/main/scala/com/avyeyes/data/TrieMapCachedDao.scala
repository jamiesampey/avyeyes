package com.avyeyes.data

import com.avyeyes.model.{AvalancheImage, User, Avalanche}
import com.avyeyes.service.ExternalIdService
import com.avyeyes.data.Database
import com.avyeyes.util.{UnauthorizedException, UserSession}
import net.liftweb.common.Loggable

import scala.collection.concurrent.TrieMap

class TrieMapCachedDao(db: Database, user: UserSession) extends CachedDao with ExternalIdService with Loggable {
  implicit val userSession: UserSession = user

  val avalancheMap: TrieMap[String, Avalanche] = new TrieMap
  avalancheMap ++= diskDao.getAllAvalanches.map(a => (a.extId, a.copy(comments = None)))


  def countAvalanches(viewableOpt: Option[Boolean]): Int = viewableOpt match {
    case Some(viewable) => avalancheMap.filter(_._2.viewable == viewable).size
    case None => avalancheMap.size
  }

  def getAvalanche(extId: String) = avalancheMap.get(extId)

  def getAvalanches(query: OrderedAvalancheQuery) = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    query.sortAndPaginate(matches)
  }

  def getAvalanchesAdmin(query: OrderedAvalancheQuery) = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    (query.sortAndPaginate(matches), matches.size, avalancheMap.size)
  }

  def getUser(email: String): Option[User] = ???

  def isUserAuthorized(email: String): Boolean = ???

  def getAllAvalanches(): List[Avalanche] = ???

  def getAvalancheFromDisk(extId: String): Option[Avalanche] = {
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

  def getAvalancheImage(avyExtId: String, filename: String) = {
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

  def getAvalancheImagesMetadata(avyExtId: String) = {
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
}
