package com.avyeyes.data

import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.test.Generators
import com.avyeyes.test.Generators._
import com.avyeyes.util.UnauthorizedException
import org.joda.time.DateTime
import org.specs2.mutable.Specification

class CachedDaoWriteTest extends Specification with InMemoryDB {
  sequential

  val extId = "5j3fyjd9"

  "Avalanche insert" >> {
    "Inserts an avalanche" >> {
      val dao = new MemoryMapCachedDao(Authorized)
      
      dao.insertAvalanche(avalancheForTest.copy(extId = extId, viewable = true))
      val selectResult = dao.getAvalanche(extId).get
      
      selectResult.extId must_== extId
    }
    
    "Unauthorized session cannot insert a viewable avalanche" >> {
      val dao = new SquerylAvalancheDao(NotAuthorized)

      dao.insertAvalanche(avalancheAtLocation(extId, true, commonLat, commonLng), submitterEmail) must throwA[UnauthorizedException]
    }
  }
    
  "Avalanche update" >> {
    "Not allowed with unauthorized session" >> {
      val dao = new SquerylAvalancheDao(NotAuthorized)

      dao insertAvalanche(avalancheAtLocation(extId, false, commonLat, commonLng), submitterEmail)
      val updatedAvalanche = avalancheAtLocation(extId, true, commonLat, commonLng)
      
      dao.updateAvalanche(updatedAvalanche) must throwA[UnauthorizedException]
    }
    
    "Allowed with authorized session" >> {
      val dao = new SquerylAvalancheDao(Authorized)

      dao insertAvalanche(avalancheAtLocation(extId, false, commonLat, commonLng), submitterEmail)
      val updatedAvalanche = avalancheWithSize(extId, true, commonLat, commonLng, 2.5, 4)
      dao.updateAvalanche(updatedAvalanche)
      
      val selectResult = dao.selectAvalanche(extId).get
      selectResult.rSize must_== updatedAvalanche.rSize
      selectResult.dSize must_== updatedAvalanche.dSize
    }
    
    "Modifies all updatable avalanche fields" >> {
      val dao = new SquerylAvalancheDao(Authorized)

      dao insertAvalanche(avalancheWithSize(extId, false, commonLat, commonLng, .5, 5), submitterEmail)
      
      val updatedAvalanche = Avalanche(extId, true, ExperienceLevel.P1,
        commonLat, commonLng, "A totally new name!", DateTime.now, SkyCoverage.Few, Precipitation.SN, 2354, Aspect.SW, 45,
        AvalancheType.SF, AvalancheTrigger.AS, AvalancheInterface.O, 1.5, 3, 
        5, 3, 2, 3, 2, ModeOfTravel.Snowshoer, "some totally different comments", 
        "-105.875489242241,39.66464854369643,3709.514235071098")
      
      dao.updateAvalanche(updatedAvalanche)
      
      val selectResult = dao.selectAvalanche(extId).get
      selectResult.viewable must beTrue
      selectResult.submitterExp must_== updatedAvalanche.submitterExp
      selectResult.lat must_== updatedAvalanche.lat
      selectResult.lng must_== updatedAvalanche.lng
      selectResult.areaName must_== updatedAvalanche.areaName
      selectResult.avyDate must_== new DateTime(updatedAvalanche.avyDate)
      selectResult.sky must_== updatedAvalanche.sky
      selectResult.precip must_== updatedAvalanche.precip
      selectResult.aspect must_== updatedAvalanche.aspect
      selectResult.angle must_== updatedAvalanche.angle
      selectResult.avyType must_== updatedAvalanche.avyType
      selectResult.avyTrigger must_== updatedAvalanche.avyTrigger
      selectResult.avyInterface must_== updatedAvalanche.avyInterface
      selectResult.rSize must_== updatedAvalanche.rSize
      selectResult.dSize must_== updatedAvalanche.dSize
      selectResult.caught must_== updatedAvalanche.caught
      selectResult.partiallyBuried must_== updatedAvalanche.partiallyBuried
      selectResult.fullyBuried must_== updatedAvalanche.fullyBuried
      selectResult.injured must_== updatedAvalanche.injured
      selectResult.killed must_== updatedAvalanche.killed
      selectResult.modeOfTravel must_== updatedAvalanche.modeOfTravel
      selectResult.comments must_== updatedAvalanche.comments
      selectResult.getSubmitter.email must_== submitterEmail
    }
  }
  
  "Avalanche delete" >> {
    "Not allowed with unauthorized session" >> {
      val dao = new SquerylAvalancheDao(NotAuthorized)

      dao insertAvalanche(avalancheAtLocation(extId, false, commonLat, commonLng), submitterEmail)

      dao.deleteAvalanche(extId) must throwA[UnauthorizedException]
    }
    
    "Allowed (and works) with authorized session" >> {
      val dao = new SquerylAvalancheDao(Authorized)

      val extId2 = "3a9s59de"
      dao insertAvalanche(avalancheAtLocation(extId, true, commonLat, commonLng), submitterEmail)
      dao insertAvalanche(avalancheAtLocation(extId2, true, commonLat, commonLng), submitterEmail)
      dao.selectAvalanche(extId) must beSome
      dao.selectAvalanche(extId2) must beSome
      
      dao.deleteAvalanche(extId)
      
      dao.selectAvalanche(extId) must beNone
      dao.selectAvalanche(extId2) must beSome
      
      dao.deleteAvalanche(extId2)
      
      dao.selectAvalanche(extId2) must beNone
    }
  }
}