package com.avyeyes.data

import javax.sql.DataSource

import com.avyeyes.model.{Avalanche, AvalancheImage, User}
import com.avyeyes.service.{UnauthorizedException, Injectors, ExternalIdService}
import com.avyeyes.util.Constants._
import net.liftweb.common.Loggable
import org.joda.time.DateTime
import slick.driver.JdbcProfile

import scala.collection.concurrent.{Map => CMap}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class MemoryMapCachedDAL(val driver: JdbcProfile, ds: DataSource,
  avalancheMap: CMap[String, Avalanche]) extends CachedDAL
  with DatabaseComponent with SlickColumnMappers with DriverComponent
  with ExternalIdService with Loggable {

  import driver.api._

  private val user = Injectors.user.vend
  private val db = Database.forDataSource(ds)

  def isUserAuthorized(email: String): Future[Boolean] = db.run {
    UserRoles.filter(_.email === email).result
  }.flatMap { userRolesResult =>
    val roles = userRolesResult.map(_.role)
    Future { roles.contains(SiteOwnerRole) || roles.contains(AdminRole) }
  }

  def countAvalanches(viewableOpt: Option[Boolean]): Int = viewableOpt match {
    case Some(viewable) => avalancheMap.filter(_._2.viewable == viewable).size
    case None => avalancheMap.size
  }

  def getAvalanche(extId: String): Option[Avalanche] = avalancheIfAllowed(avalancheMap.get(extId))

  def getAvalanches(query: AvalancheQuery): List[Avalanche] = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    matches.sortWith(query.sortFunction).drop(query.offset).take(query.limit)
  }

  def getAvalanchesAdmin(query: AdminAvalancheQuery): (List[Avalanche], Int, Int) = {
    if (!user.isAuthorizedSession()) {
      throw new UnauthorizedException("Not authorized to view avalanches")
    }

    val matches = avalancheMap.values.filter(query.toPredicate).toList
    (matches.sortWith(query.sortFunction).drop(query.offset).take(query.limit), matches.size, avalancheMap.size)
  }

  def getAvalancheFromDisk(extId: String): Option[Avalanche] = {
    val avalancheOpt = Await.result(db.run(Avalanches.filter(_.extId === extId).result.headOption), Duration.Inf)
    avalancheIfAllowed(avalancheOpt)
  }

  private def avalancheIfAllowed(opt: Option[Avalanche]): Option[Avalanche] = opt match {
    case Some(avalanche) => if (avalanche.viewable || user.isAuthorizedSession()) Some(avalanche) else None
    case None => None
  }

  def insertAvalanche(avalanche: Avalanche) = {
    if (avalanche.viewable && !user.isAuthorizedSession()) {
      throw new UnauthorizedException("Not authorized to insert a viewable avalanche")
    }

    val insertAction = db.run {
      Users.filter(_.email === avalanche.submitterEmail).exists.result
    }.flatMap { userExists =>
      db.run(if (!userExists) {
        (Users += User(DateTime.now, avalanche.submitterEmail)) >>
        (Avalanches += avalanche)
      } else {
        Avalanches += avalanche
      })
    }

    Await.result(insertAction, Duration.Inf)
    avalancheMap += (avalanche.extId -> avalanche.copy(comments = None))
  }

  def updateAvalanche(update: Avalanche) = {
    if (!user.isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to update avalanche")
    }

    Await.result(db.run {
      val updateQuery = Avalanches.filter(_.extId === update.extId).map(a =>
        (a.updateTime, a.viewable, a.submitterEmail, a.submitterExp,
          a.areaName, a.date, a.scene, a.slope, a.classification,
          a.humanNumbers, a.comments))
      updateQuery.update(
        (DateTime.now, update.viewable, update.submitterEmail, update.submitterExp,
          update.areaName, update.date, update.scene, update.slope, update.classification,
          update.humanNumbers, update.comments))
    }, Duration.Inf)

    getAvalancheFromDisk(update.extId) match {
      case Some(a) => avalancheMap += (a.extId -> a.copy(comments = None))
      case None => logger.error("Unable to pull updated avalanche back out of database")
    }
  }

  def deleteAvalanche(extId: String) = {
    if (!user.isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to delete avalanches")
    }

    Await.result(db.run(
      AvalancheImages.filter(_.avyExtId === extId).delete >>
      Avalanches.filter(_.extId === extId).delete
    ), Duration.Inf)
    avalancheMap -= extId
  }

  def insertAvalancheImage(img: AvalancheImage) = {
    Await.result(db.run(
      (AvalancheImages += img) >>
      setAvalancheUpdateTimeAction(img.avyExtId)
    ), Duration.Inf)
  }

  def countAvalancheImages(extId: String): Int = Await.result(db.run(
    AvalancheImages.filter(_.avyExtId === extId).length.result), Duration.Inf)

  def getAvalancheImage(avyExtId: String, baseFilename: String): Option[AvalancheImage] = {
    Await.result(db.run(imageQuery(avyExtId, Some(baseFilename)).result.headOption), Duration.Inf)
  }

  def getAvalancheImages(avyExtId: String): List[AvalancheImage] = Await.result(db.run(
    imageQuery(avyExtId, None).result), Duration.Inf).toList

  private def imageQuery(avyExtId: String, baseFilename: Option[String]) = {
    val queryByExtId = reservationExists(avyExtId) || user.isAuthorizedSession() match {
      case true => AvalancheImages.filter(_.avyExtId === avyExtId)
      case false => for {
        img <- AvalancheImages if img.avyExtId === avyExtId
        a <- Avalanches if a.extId === img.avyExtId && a.viewable === true
      } yield img
    }

    baseFilename match {
      case Some(fn) => queryByExtId.filter(_.filename.startsWith(fn))
      case None => queryByExtId
    }
  }

  def deleteAvalancheImage(avyExtId: String, filename: String) = {
    if (!user.isAuthorizedSession && !reservationExists(avyExtId)) {
      throw new UnauthorizedException("Not authorized to delete image")
    }
      
    Await.result(db.run(
      AvalancheImages.filter(img => img.avyExtId === avyExtId && img.filename === filename).delete >>
      setAvalancheUpdateTimeAction(avyExtId)
    ), Duration.Inf)
  }

  private def setAvalancheUpdateTimeAction(extId: String) = Avalanches.filter(
    _.extId === extId).map(_.updateTime).update(DateTime.now)

  def createSchema = (Avalanches.schema ++ AvalancheImages.schema ++ Users.schema ++ UserRoles.schema).create
}
