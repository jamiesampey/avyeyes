package com.avyeyes.data

import javax.inject._

import com.avyeyes.data.SlickRowMappers._
import com.avyeyes.model._
import com.avyeyes.service.ExternalIdService
import com.google.inject.assistedinject.Assisted
import org.joda.time.DateTime
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MemoryMapCachedDAL @Inject()(@NamedDatabase("postgres") dbConfigProvider: DatabaseConfigProvider, val logger: Logger)(@Assisted avalancheMap: AvalancheMap)
  extends CachedDAL with DatabaseComponent with SlickColumnMappers with ExternalIdService {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db
  protected val jdbcProfile: JdbcProfile = dbConfig.profile
  import jdbcProfile.api._

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  def userRoles(email: String): Future[Seq[UserRole]] = db.run(UserRoleRows.filter(_.email === email).result)

  def countAvalanches(viewableOpt: Option[Boolean]): Int = viewableOpt match {
    case Some(viewable) => avalancheMap.count(_._2.viewable == viewable)
    case None => avalancheMap.size
  }

  def getAvalanche(extId: String): Option[Avalanche] = avalancheMap.get(extId)

  def getAvalanches(query: AvalancheQuery): List[Avalanche] = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    matches.sortWith(query.sortFunction).slice(query.offset, query.offset + query.limit)
  }

  def getAvalanchesAdmin(query: AdminAvalancheQuery): (List[Avalanche], Int, Int) = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    (matches.sortWith(query.sortFunction).slice(query.offset, query.offset + query.limit), matches.size, avalancheMap.size)
  }

  def getAvalanchesFromDisk = {
    val query = for {
      avalanche <- AvalancheRows
      weather <- AvalancheWeatherRows if weather.avalanche === avalanche.extId
      classification <- AvalancheClassificationRows if classification.avalanche === avalanche.extId
      human <- AvalancheHumanRows if human.avalanche === avalanche.extId
    } yield (avalanche, weather, classification, human)

    db.run(query.result).map(_.map(avalancheFromData))
  }

  def getAvalancheFromDisk(extId: String) = {
    val query = for {
      avalanche <- AvalancheRows.filter(_.extId === extId)
      weather <- AvalancheWeatherRows if weather.avalanche === avalanche.extId
      classification <- AvalancheClassificationRows if classification.avalanche === avalanche.extId
      human <- AvalancheHumanRows if human.avalanche === avalanche.extId
    } yield (avalanche, weather, classification, human)

    db.run(query.result.headOption).map(_.map(avalancheFromData))
  }

  def insertAvalanche(avalanche: Avalanche) = {
    val avalancheInserts = (AvalancheRows += avalanche) >> (AvalancheWeatherRows += avalanche) >> (AvalancheClassificationRows += avalanche) >> (AvalancheHumanRows += avalanche)

    db.run(
      UserRows.filter(_.email === avalanche.submitterEmail).exists.result
    ).flatMap {
      case false => db.run((UserRows += User(DateTime.now, avalanche.submitterEmail)) >> avalancheInserts)
      case true => db.run(avalancheInserts)
    }
    .map(_ => avalancheMap += (avalanche.extId -> avalanche.copy(comments = None)))
  }

  def updateAvalanche(update: Avalanche) = {
    val avalancheUpdateQuery = AvalancheRows.filter(_.extId === update.extId).map(a => (a.updateTime, a.viewable, a.submitterEmail, a.submitterExp, a.areaName, a.date, a.aspect, a.angle, a.comments))
    val sceneUpdateQuery = AvalancheWeatherRows.filter(_.avalanche === update.extId).map(w => (w.recentSnow, w.recentWindDirection, w.recentWindSpeed))
    val classificationUpdateQuery = AvalancheClassificationRows.filter(_.avalanche === update.extId).map(c => (c.avalancheType, c.trigger, c.triggerModifier, c.interface, c.rSize, c.dSize))
    val humanUpdateQuery = AvalancheHumanRows.filter(_.avalanche === update.extId).map(h => (h.modeOfTravel, h.caught, h.partiallyBuried, h.fullyBuried, h.injured, h.killed))

    db.run {
      avalancheUpdateQuery.update((DateTime.now, update.viewable, update.submitterEmail, update.submitterExp, update.areaName, update.date, update.slope.aspect, update.slope.angle, update.comments)) >>
      sceneUpdateQuery.update((update.weather.recentSnow, update.weather.recentWindDirection, update.weather.recentWindSpeed)) >>
      classificationUpdateQuery.update((update.classification.avyType, update.classification.trigger, update.classification.triggerModifier, update.classification.interface, update.classification.rSize, update.classification.dSize)) >>
      humanUpdateQuery.update((update.humanNumbers.modeOfTravel, update.humanNumbers.caught, update.humanNumbers.partiallyBuried, update.humanNumbers.fullyBuried, update.humanNumbers.injured, update.humanNumbers.killed))
    }
    .map(_ => getAvalancheFromDisk(update.extId).map {
      case Some(a) => avalancheMap += (a.extId -> a.copy(comments = None))
      case None => logger.error("Unable to pull updated avalanche back out of database")
    })
  }

  def deleteAvalanche(extId: String) = {
    avalancheMap -= extId

    db.run(
      AvalancheImageRows.filter(_.avalanche === extId).delete >>
      AvalancheWeatherRows.filter(_.avalanche === extId).delete >>
      AvalancheClassificationRows.filter(_.avalanche === extId).delete >>
      AvalancheHumanRows.filter(_.avalanche === extId).delete >>
      AvalancheRows.filter(_.extId === extId).delete
    )
  }

  def insertAvalancheImage(img: AvalancheImage) = db.run(
    (AvalancheImageRows += img) >> setAvalancheUpdateTimeAction(img.avalanche)
  )

  def countAvalancheImages(extId: String) = db.run(
    AvalancheImageRows.filter(_.avalanche === extId).length.result
  )

  def getAvalancheImage(avyExtId: String, baseFilename: String) = db.run(
    imageQuery(avyExtId, Some(baseFilename)).result.headOption
  )

  def getAvalancheImages(avyExtId: String) = db.run(
    imageQuery(avyExtId, None).result).map(_.toList.sortBy(_.sortOrder)
  )

  private def imageQuery(avyExtId: String, baseFilename: Option[String]) = {
    val imageQuery = AvalancheImageRows.filter(_.avalanche === avyExtId)

    baseFilename match {
      case Some(name) => imageQuery.filter(_.filename.startsWith(name))
      case None => imageQuery
    }
  }

  def updateAvalancheImageCaption(avyExtId: String, baseFilename: String, caption: Option[String]) = {
    val imageUpdateQuery = AvalancheImageRows.filter(img => img.avalanche === avyExtId && img.filename.startsWith(baseFilename)).map(_.caption)
    db.run(imageUpdateQuery.update(caption))
  }

  def updateAvalancheImageOrder(avyExtId: String, filenameOrder: List[String]) = Future.sequence(filenameOrder.zipWithIndex.map { case (baseFilename, order) =>
    val imageOrderUpdateQuery = AvalancheImageRows.filter(img => img.avalanche === avyExtId && img.filename.startsWith(baseFilename)).map(_.sortOrder)
    db.run(imageOrderUpdateQuery.update(order))
  })

  def deleteAvalancheImage(avyExtId: String, filename: String) = db.run(
    AvalancheImageRows.filter(img => img.avalanche === avyExtId && img.filename === filename).delete >>
    setAvalancheUpdateTimeAction(avyExtId)
  ).map(_ => getAvalancheImages(avyExtId).map(images => updateAvalancheImageOrder(avyExtId, images.map(_.filename))))

  def deleteOrphanAvalancheImages = db.run(
    AvalancheImageRows.filter(img => !AvalancheRows.filter(_.extId === img.avalanche).exists).result
  ).flatMap { orphanImages =>
    val unfinishedReports = orphanImages.filterNot(img => reservationExists(img.avalanche)).map(_.avalanche).distinct
    logger.info(s"Deleting ${orphanImages.size} orphan images from ${unfinishedReports.size} unfinished avalanche reports")
    db.run(AvalancheImageRows.filter(img => img.avalanche inSetBind unfinishedReports).delete)
  }

  private def setAvalancheUpdateTimeAction(extId: String) = AvalancheRows.filter(_.extId === extId).map(_.updateTime).update(DateTime.now)
}
