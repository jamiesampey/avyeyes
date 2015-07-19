package com.avyeyes.data

import com.avyeyes.data.{GeoBounds, AvalancheQuery}
import com.avyeyes.model.Avalanche
import com.avyeyes.test._
import org.specs2.mutable.Specification

class LocationFilteringSelectTest extends Specification with InMemoryDB with Generators {
  sequential

  val dao = new SquerylAvalancheDao(Authorized)

  val neHemisphereAvalanche = avalancheAtLocation("4a3jr23k", true, 47.59349550, 7.59349050)
  val seHemisphereAvalanche = avalancheAtLocation("83j859j3", true, -44.5943285, 170.2395494)
  val swHemisphereAvalanche = avalancheAtLocation("2r8f883s", true, -25.5349550, -69.59349050)
  val nwHemisphereAvalanche = avalancheAtLocation("954fi4rf", true, 39.59349550, -106.59349050)

  "Latitude/Longitude filtering in all four hemispheres" >> {
    "NE hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val neLatLngInBoundsCriteria = AvalancheQuery(geoBounds = Some(createGeoBoundsToInclude(neHemisphereAvalanche)))
      
      val resultList = dao.selectAvalanches(neLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== neHemisphereAvalanche.extId
    }
  
    "SE hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val seLatLngInBoundsCriteria = AvalancheQuery(geoBounds = Some(createGeoBoundsToInclude(seHemisphereAvalanche)))
      
      val resultList = dao.selectAvalanches(seLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== seHemisphereAvalanche.extId
    }
    
    "SW hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val swLatLngInBoundsCriteria = AvalancheQuery(geoBounds = Some(createGeoBoundsToInclude(swHemisphereAvalanche)))
      
      val resultList = dao.selectAvalanches(swLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== swHemisphereAvalanche.extId
    }
    
    "NW hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val nwLatLngInBoundsCriteria = AvalancheQuery(geoBounds = Some(createGeoBoundsToInclude(nwHemisphereAvalanche)))
      
      val resultList = dao.selectAvalanches(nwLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== nwHemisphereAvalanche.extId
    }
  }
  
  private def createGeoBoundsToInclude(a: Avalanche) = {
    GeoBounds(a.lat+.01, a.lng+.01, a.lat-.01, a.lng-.01)
  }
  
  private def insertAllAvalanches() = {
    insertTestAvalanche(dao, neHemisphereAvalanche)
    insertTestAvalanche(dao, nwHemisphereAvalanche)
    insertTestAvalanche(dao, swHemisphereAvalanche)
    insertTestAvalanche(dao, seHemisphereAvalanche)
  }
}