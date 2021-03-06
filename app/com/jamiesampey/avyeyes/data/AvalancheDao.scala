package com.jamiesampey.avyeyes.data

import javax.inject._

import com.jamiesampey.avyeyes.model._
import com.jamiesampey.avyeyes.service.ExternalIdService
import org.joda.time.DateTime
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AvalancheDao @Inject()(val dbConfigProvider: DatabaseConfigProvider, avalancheCache: AvalancheCache,
                             idService: ExternalIdService, val logger: Logger, val appLifecycle: ApplicationLifecycle)
  extends CachedDao with AvyEyesDatabase {

  private val avalancheMap = avalancheCache.avalancheMap

  import dbConfig.profile.api._

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)

  def countAvalanches(viewableOpt: Option[Boolean]): Int = viewableOpt match {
    case Some(viewable) => avalancheMap.count(_._2.viewable == viewable)
    case None => avalancheMap.size
  }

  def getAvalanche(extId: String): Option[Avalanche] = avalancheMap.get(extId)

  def getAvalanches(query: AvalancheSpatialQuery): List[Avalanche] = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    matches.sortWith(query.sortFunction).slice(query.offset, query.offset + query.limit)
  }

  def getAvalanchesAdmin(query: AvalancheTableQuery): (List[Avalanche], Int, Int) = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    (matches.sortWith(query.sortFunction).slice(query.offset, query.offset + query.limit), matches.size, avalancheMap.size)
  }

  private[data] def getAvalanchesFromDisk = {
    val query = for {
      avalanche <- AvalancheRows
      weather <- AvalancheWeatherRows if weather.avalanche === avalanche.extId
      classification <- AvalancheClassificationRows if classification.avalanche === avalanche.extId
    } yield (avalanche, weather, classification)

    db.run(query.result).map(_.map(avalancheFromData))
  }

  private[data] def getAvalancheFromDisk(extId: String) = {
    val query = for {
      avalanche <- AvalancheRows.filter(_.extId === extId)
      weather <- AvalancheWeatherRows if weather.avalanche === avalanche.extId
      classification <- AvalancheClassificationRows if classification.avalanche === avalanche.extId
    } yield (avalanche, weather, classification)

    db.run(query.result.headOption).map(_.map(avalancheFromData))
  }

  def insertAvalanche(avalanche: Avalanche) = {
    val avalancheInserts = (AvalancheRows += avalanche) >> (AvalancheWeatherRows += avalanche) >> (AvalancheClassificationRows += avalanche)

    db.run(
      AppUserRows.filter(_.email === avalanche.submitterEmail).exists.result
    ).flatMap {
      case false =>
        val now = DateTime.now
        db.run((AppUserRows += AppUserTableRow(now, now, avalanche.submitterEmail, None, None)) >> avalancheInserts)
      case true => db.run(avalancheInserts)
    }
    .map(_ => avalancheMap += (avalanche.extId -> avalanche))
  }

  def updateAvalanche(update: Avalanche) = {
    val avalancheUpdateQuery = AvalancheRows.filter(_.extId === update.extId).map(a => (a.updateTime, a.viewable, a.submitterEmail, a.submitterExp, a.areaName, a.date, a.aspect, a.angle, a.comments))
    val sceneUpdateQuery = AvalancheWeatherRows.filter(_.avalanche === update.extId).map(w => (w.recentSnow, w.recentWindDirection, w.recentWindSpeed))
    val classificationUpdateQuery = AvalancheClassificationRows.filter(_.avalanche === update.extId).map(c => (c.avalancheType, c.trigger, c.triggerModifier, c.interface, c.rSize, c.dSize))

    db.run {
      avalancheUpdateQuery.update((DateTime.now, update.viewable, update.submitterEmail, update.submitterExp, update.areaName, update.date, update.slope.aspect, update.slope.angle, update.comments)) >>
      sceneUpdateQuery.update((update.weather.recentSnow, update.weather.recentWindDirection, update.weather.recentWindSpeed)) >>
      classificationUpdateQuery.update((update.classification.avyType, update.classification.trigger, update.classification.triggerModifier, update.classification.interface, update.classification.rSize, update.classification.dSize))
    }
    .map(_ => getAvalancheFromDisk(update.extId).map {
      case Some(a) => avalancheMap += (a.extId -> a)
      case None => logger.error("Unable to pull updated avalanche back out of database")
    })
  }

  def deleteAvalanche(extId: String) = {
    avalancheMap -= extId

    db.run(
      AvalancheImageRows.filter(_.avalanche === extId).delete >>
      AvalancheWeatherRows.filter(_.avalanche === extId).delete >>
      AvalancheClassificationRows.filter(_.avalanche === extId).delete >>
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
    logger.debug(s"Setting order $order on image $baseFilename")
    db.run(imageOrderUpdateQuery.update(order))
  })

  def deleteAvalancheImage(avyExtId: String, filename: String) = db.run(
    AvalancheImageRows.filter(img => img.avalanche === avyExtId && img.filename === filename).delete >>
    setAvalancheUpdateTimeAction(avyExtId)
  ).map(_ => getAvalancheImages(avyExtId).map(images => updateAvalancheImageOrder(avyExtId, images.map(_.filename))))

  private[data] def deleteOrphanAvalancheImages = db.run(
    AvalancheImageRows.filter(img => !AvalancheRows.filter(_.extId === img.avalanche).exists).result
  ).flatMap { orphanImages =>
    val unfinishedReports = orphanImages.filterNot(img => idService.reservationExists(img.avalanche)).map(_.avalanche).distinct
    logger.info(s"Deleting ${orphanImages.size} orphan images from ${unfinishedReports.size} unfinished avalanche reports")
    db.run(AvalancheImageRows.filter(img => img.avalanche inSetBind unfinishedReports).delete)
  }

  private def setAvalancheUpdateTimeAction(extId: String) = AvalancheRows.filter(_.extId === extId).map(_.updateTime).update(DateTime.now)
}
