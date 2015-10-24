package com.avyeyes.data

import javax.sql.DataSource

import com.avyeyes.data.SlickRowMappers._
import com.avyeyes.model._
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
    UserRoleRows.filter(_.email === email).result
  }.flatMap { userRolesResult =>
    val roles = userRolesResult.map(_.role)
    Future { roles.contains(SiteOwnerRole) || roles.contains(AdminRole) }
  }

  def countAvalanches(viewableOpt: Option[Boolean]): Int = viewableOpt match {
    case Some(viewable) => avalancheMap.count(_._2.viewable == viewable)
    case None => avalancheMap.size
  }

  def getAvalanche(extId: String): Option[Avalanche] = avalancheIfAllowed(avalancheMap.get(extId))

  def getAvalanches(query: AvalancheQuery): List[Avalanche] = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    matches.sortWith(query.sortFunction).slice(query.offset, query.offset + query.limit)
  }

  def getAvalanchesAdmin(query: AdminAvalancheQuery): (List[Avalanche], Int, Int) = {
    if (!user.isAuthorizedSession()) {
      throw new UnauthorizedException("Not authorized to view avalanches")
    }

    val matches = avalancheMap.values.filter(query.toPredicate).toList
    (matches.sortWith(query.sortFunction).slice(query.offset, query.offset + query.limit), matches.size, avalancheMap.size)
  }

  def getAvalanchesFromDisk: Seq[Avalanche] = {
    val query = for {
      avalanche <- AvalancheRows
      scene <- AvalancheSceneRows if scene.avalanche === avalanche.extId
      classification <- AvalancheClassificationRows if classification.avalanche === avalanche.extId
      human <- AvalancheHumanRows if human.avalanche === avalanche.extId
    } yield (avalanche, scene, classification, human)

    Await.result(db.run(query.result), Duration.Inf).map(avalancheFromData)
  }

  def getAvalancheFromDisk(extId: String): Option[Avalanche] = {
    val query = for {
      avalanche <- AvalancheRows.filter(_.extId === extId)
      scene <- AvalancheSceneRows if scene.avalanche === avalanche.extId
      classification <- AvalancheClassificationRows if classification.avalanche === avalanche.extId
      human <- AvalancheHumanRows if human.avalanche === avalanche.extId
    } yield (avalanche, scene, classification, human)

    val avalancheOpt = Await.result(db.run(query.result.headOption), Duration.Inf).map(avalancheFromData)
    avalancheIfAllowed(avalancheOpt)
  }

  def insertAvalanche(avalanche: Avalanche) = {
    if (avalanche.viewable && !user.isAuthorizedSession()) {
      throw new UnauthorizedException("Not authorized to insert a viewable avalanche")
    }

    val avalancheInserts = (AvalancheRows += avalanche) >> (AvalancheSceneRows += avalanche) >> (AvalancheClassificationRows += avalanche) >> (AvalancheHumanRows += avalanche)

    val insertAction = db.run {
      UserRows.filter(_.email === avalanche.submitterEmail).exists.result
    }.flatMap { userExists =>
      db.run(if (!userExists) {
        (UserRows += User(DateTime.now, avalanche.submitterEmail)) >> avalancheInserts
      } else {
        avalancheInserts
      })
    }

    Await.result(insertAction, Duration.Inf)
    avalancheMap += (avalanche.extId -> avalanche.copy(comments = None))
  }

  def updateAvalanche(update: Avalanche) = {
    if (!user.isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to update avalanche")
    }

    val avalancheUpdateQuery = AvalancheRows.filter(_.extId === update.extId).map(a => (a.updateTime, a.viewable, a.submitterEmail, a.submitterExp, a.areaName, a.date, a.aspect, a.angle, a.comments))
    val sceneUpdateQuery = AvalancheSceneRows.filter(_.avalanche === update.extId).map(s => (s.skyCoverage, s.precipitation))
    val classificationUpdateQuery = AvalancheClassificationRows.filter(_.avalanche === update.extId).map(c => (c.avalancheType, c.trigger, c.interface, c.rSize, c.dSize))
    val humanUpdateQuery = AvalancheHumanRows.filter(_.avalanche === update.extId).map(h => (h.modeOfTravel, h.caught, h.partiallyBuried, h.fullyBuried, h.injured, h.killed))

    Await.result(db.run {
      avalancheUpdateQuery.update((DateTime.now, update.viewable, update.submitterEmail, update.submitterExp, update.areaName, update.date, update.slope.aspect, update.slope.angle, update.comments)) >>
      sceneUpdateQuery.update((update.scene.skyCoverage, update.scene.precipitation)) >>
      classificationUpdateQuery.update((update.classification.avyType, update.classification.trigger, update.classification.interface, update.classification.rSize, update.classification.dSize)) >>
      humanUpdateQuery.update((update.humanNumbers.modeOfTravel, update.humanNumbers.caught, update.humanNumbers.partiallyBuried, update.humanNumbers.fullyBuried, update.humanNumbers.injured, update.humanNumbers.killed))
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
      AvalancheImageRows.filter(_.avyExtId === extId).delete >>
      AvalancheRows.filter(_.extId === extId).delete
    ), Duration.Inf)
    avalancheMap -= extId
  }

  def insertAvalancheImage(img: AvalancheImage) = {
    Await.result(db.run(
      (AvalancheImageRows += img) >>
      setAvalancheUpdateTimeAction(img.avyExtId)
    ), Duration.Inf)
  }

  def countAvalancheImages(extId: String): Int = Await.result(db.run(
    AvalancheImageRows.filter(_.avyExtId === extId).length.result), Duration.Inf)

  def getAvalancheImage(avyExtId: String, baseFilename: String): Option[AvalancheImage] = {
    Await.result(db.run(imageQuery(avyExtId, Some(baseFilename)).result.headOption), Duration.Inf)
  }

  def getAvalancheImages(avyExtId: String): List[AvalancheImage] = Await.result(db.run(
    imageQuery(avyExtId, None).result), Duration.Inf).toList

  private def imageQuery(avyExtId: String, baseFilename: Option[String]) = {
    val queryByExtId = reservationExists(avyExtId) || user.isAuthorizedSession() match {
      case true => AvalancheImageRows.filter(_.avyExtId === avyExtId)
      case false => for {
        img <- AvalancheImageRows if img.avyExtId === avyExtId
        a <- AvalancheRows if a.extId === img.avyExtId && a.viewable === true
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
      AvalancheImageRows.filter(img => img.avyExtId === avyExtId && img.filename === filename).delete >>
      setAvalancheUpdateTimeAction(avyExtId)
    ), Duration.Inf)
  }

  def getOrphanAvalancheImages = Await.result(db.run(
      AvalancheImageRows.filter(img => !AvalancheRows.filter(_.extId === img.avyExtId).exists).result
    ), Duration.Inf)

  private def avalancheIfAllowed(opt: Option[Avalanche]): Option[Avalanche] = opt match {
    case Some(avalanche) => if (avalanche.viewable || user.isAuthorizedSession()) Some(avalanche) else None
    case None => None
  }

  private def setAvalancheUpdateTimeAction(extId: String) = AvalancheRows.filter(
    _.extId === extId).map(_.updateTime).update(DateTime.now)

  def createSchema = (
    AvalancheRows.schema ++
    AvalancheSceneRows.schema ++
    AvalancheClassificationRows.schema ++
    AvalancheHumanRows.schema ++
    AvalancheImageRows.schema ++
    UserRows.schema ++
    UserRoleRows.schema
  ).create
}
