package com.avyeyes.persist

import org.apache.commons.lang3.StringUtils.isNotBlank

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.PostgreSqlAdapter

import com.avyeyes.model._
import com.avyeyes.model.enums._
import com.avyeyes.persist.AvalancheSchema._
import com.avyeyes.service.AvalancheSearchCriteria
import com.avyeyes.util.AEConstants._
import com.avyeyes.util.AEHelpers._

import net.liftweb.common.Loggable
import net.liftweb.http.FileParamHolder
import net.liftweb.util.Helpers.today

trait SquerylPersistence extends PersistenceContext with Loggable {
  def dao = new SquerylAvalancheDao
  dao.initSession()
  
  class SquerylAvalancheDao extends AvalancheDao {
    lazy val jdbcConnectionString = new StringBuilder("jdbc:postgresql://")
      .append(getProp("db.host")).append(":")
      .append(getProp("db.port")).append("/")
      .append(getProp("db.name")).toString
   
    def initSession() = {
      if (SessionFactory.concreteFactory.isEmpty) {
        logger.info("Initializing database session")
        Class.forName("org.postgresql.Driver")
        SessionFactory.concreteFactory = Some(()=>
          Session.create(java.sql.DriverManager.getConnection(jdbcConnectionString), new PostgreSqlAdapter))
      }
    }
    
    def selectViewableAvalanche(extId: String): Option[Avalanche] = {
      avalanches.where(a => a.viewable === true and a.extId === extId).headOption
    }
    
    def selectAvalanche(extId: String): Option[Avalanche] = {
      avalanches.where(a => a.extId === extId).headOption
    }
      
    def selectAvalanches(criteria: AvalancheSearchCriteria) = {
      val latBounds = List(strToDbl(criteria.northLimit), strToDbl(criteria.southLimit))
      val lngBounds = List(strToDbl(criteria.eastLimit), strToDbl(criteria.westLimit))
      
      val fromDate = if (!criteria.fromDateStr.isEmpty) parseDateStr(criteria.fromDateStr) else EarliestAvyDate
      val toDate = if (!criteria.toDateStr.isEmpty) parseDateStr(criteria.toDateStr) else today.getTime
  
      val avyType = if (isNotBlank(criteria.avyTypeStr)) AvalancheType.withName(criteria.avyTypeStr) else AvalancheType.U
      val avyTrigger = if (isNotBlank(criteria.avyTriggerStr)) AvalancheTrigger.withName(criteria.avyTriggerStr) else AvalancheTrigger.U
  
      from(avalanches)(a => where(
        a.viewable === true
        and a.lat.between(latBounds.min, latBounds.max)
        and a.lng.between(lngBounds.min, lngBounds.max)
        and a.avyDate.between(fromDate, toDate)
        and (a.avyType === avyType).inhibitWhen(criteria.avyTypeStr.isEmpty)
        and (a.trigger === avyTrigger).inhibitWhen(criteria.avyTriggerStr.isEmpty)
        and (a.rSize gte getAvySizeQueryVal(criteria.rSize).?)
        and (a.dSize gte getAvySizeQueryVal(criteria.dSize).?)
        and (a.caught gte getHumanNumberQueryVal(criteria.numCaught).?)
        and (a.killed gte getHumanNumberQueryVal(criteria.numKilled).?))
      select(a)).toList
    }  
  
    def insertAvalanche(avalanche: Avalanche) = avalanches insert avalanche
  
    def insertAvalancheImage(avyExtId: String, fph: FileParamHolder) = {
      avalancheImageDropbox insert new AvalancheImg(avyExtId, fph.fileName.split("\\.")(0), fph.mimeType, fph.file)
    }
    
    def selectAvalancheImage(avyExtId: String, filename: String) = {
      from(avalancheImageDropbox)(img => where(
        img.avyExtId === avyExtId 
        and img.filename === filename) 
      select(img)).headOption
    }
    
    def selectAvalancheImageFilenames(avyExtId: String) = {
      from(avalancheImageDropbox)(img => where(img.avyExtId === avyExtId) select(img.filename)).toList
    }
    
    private def getAvySizeQueryVal(sizeStr: String): Option[Double] = 
      if (strToDbl(sizeStr) > 0) Some(strToDbl(sizeStr)) else None
      
    private def getHumanNumberQueryVal(numStr: String): Option[Int] = 
      if (strToHumanNumber(numStr) >= 0) Some(strToHumanNumber(numStr)) else None
  }
}