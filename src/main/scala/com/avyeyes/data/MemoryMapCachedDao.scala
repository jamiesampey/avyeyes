package com.avyeyes.data

import javax.sql.DataSource

import com.avyeyes.data.DatabaseSchema._
import com.avyeyes.data.SlickColumnMappers._
import com.avyeyes.model.{Avalanche, AvalancheImage, User}
import com.avyeyes.service.ExternalIdService
import com.avyeyes.util.{UnauthorizedException, UserSession}
import net.liftweb.common.Loggable
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.collection.concurrent.{Map => CMap}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class MemoryMapCachedDao(ds: DataSource, avalancheMap: CMap[String, Avalanche], user: UserSession)
  extends CachedDao with ExternalIdService with Loggable {

  private val db = Database.forDataSource(ds)

  def getUser(email: String): Option[User] = {
    Await.result(db.run(Users.filter(_.email === email).result.headOption), Duration.Inf)
  }

  def countAvalanches(viewableOpt: Option[Boolean]): Int = viewableOpt match {
    case Some(viewable) => avalancheMap.filter(_._2.viewable == viewable).size
    case None => avalancheMap.size
  }

  def getAvalanche(extId: String) = avalancheMap.get(extId)

  def getAvalanches(query: AvalancheQuery) = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    matches.sortWith(query.sortFunction).drop(query.offset).take(query.limit)
  }

  def getAvalanchesAdmin(query: AdminAvalancheQuery) = {
    val matches = avalancheMap.values.filter(query.toPredicate).toList
    (matches.sortWith(query.sortFunction).drop(query.offset).take(query.limit), matches.size, avalancheMap.size)
  }

  def getAvalancheFromDisk(extId: String): Option[Avalanche] = {
    Await.result(db.run(Avalanches.filter(_.extId === extId).result.headOption), Duration.Inf)
  }

  def insertAvalanche(avalanche: Avalanche) = {
    if (avalanche.viewable && !user.isAuthorizedSession()) {
      throw new UnauthorizedException("Not authorized to insert a viewable avalanche")
    }

    val userInsert = Users insertOrUpdate User(DateTime.now, avalanche.submitterEmail)
    val avalancheInsert = Avalanches += avalanche
    Await.result(db.run(userInsert >> avalancheInsert), Duration.Inf)

    avalancheMap += (avalanche.extId -> avalanche.copy(comments = None))
  }

  def updateAvalanche(avalanche: Avalanche) = {
    if (!user.isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to update avalanche")
    }

    Await.result(db.run(Avalanches.filter(_.extId === avalanche.extId).update(avalanche)), Duration.Inf)

    avalancheMap += (avalanche.extId -> avalanche.copy(comments = None))
  }

  def deleteAvalanche(extId: String) = {
    if (!user.isAuthorizedSession) {
      throw new UnauthorizedException("Not authorized to delete avalanches")
    }

    Await.result(db.run(Avalanches.filter(_.extId === extId).delete), Duration.Inf)

    avalancheMap -= extId
  }

  def insertAvalancheImage(img: AvalancheImage) = {
    val insertAction = AvalancheImages += img
    Await.result(db.run(insertAction >> setAvalancheUpdateTimeAction(img.avyExtId)), Duration.Inf)
  }

  def countAvalancheImages(extId: String): Int = Await.result(db.run(
    AvalancheImages.filter(_.avyExtId === extId).length.result), Duration.Inf)

  def getAvalancheImage(avyExtId: String, filename: String): Option[AvalancheImage] = {
    Await.result(db.run(imageQuery(avyExtId, Some(filename)).result.headOption), Duration.Inf)
  }

  def getAvalancheImages(avyExtId: String): List[AvalancheImage] = Await.result(db.run(
    imageQuery(avyExtId, None).result), Duration.Inf).toList

  private def imageQuery(avyExtId: String, filename: Option[String]) = {
    val queryByExtId = reservationExists(avyExtId) || user.isAuthorizedSession() match {
      case true => AvalancheImages.filter(_.avyExtId === avyExtId)
      case false => for {
        img <- AvalancheImages if img.avyExtId === avyExtId
        a <- Avalanches if a.extId === avyExtId && a.viewable === true
      } yield img
    }

    filename match {
      case Some(fname) => queryByExtId.filter(_.filename === filename)
      case None => queryByExtId
    }
  }

  def deleteAvalancheImage(avyExtId: String, filename: String) = {
    if (!user.isAuthorizedSession && !reservationExists(avyExtId)) {
      throw new UnauthorizedException("Not authorized to delete image")
    }
      
    val deleteImageAction = AvalancheImages.filter(
      img => img.avyExtId === avyExtId && img.filename === filename).delete
    Await.result(db.run(deleteImageAction >> setAvalancheUpdateTimeAction(avyExtId)), Duration.Inf)
  }

  private def setAvalancheUpdateTimeAction(extId: String) = {
    val query = for { a <- Avalanches if a.extId === extId } yield a.updateTime
    query.update(DateTime.now)
  }

  //  private def getAvyViewableQueryVal(viewable: Option[Boolean]): Option[Boolean] = viewable match {
  //    case None if isAuthorizedSession => None // viewable criteria will NOT apply (ADMIN ONLY)
  //    case Some(bool) if (!bool && isAuthorizedSession) => Some(false) // criteria: viewable == false (ADMIN ONLY)
  //    case _ => Some(true) // criteria: viewable == true
  //  }
}
