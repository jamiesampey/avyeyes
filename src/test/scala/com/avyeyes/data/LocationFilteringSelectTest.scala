package com.avyeyes.data

import com.avyeyes.data.{GeoBounds, AvalancheQuery}
import com.avyeyes.model.{Coordinate, Avalanche}
import com.avyeyes.test._
import org.specs2.mutable.Specification

class LocationFilteringSelectTest extends Specification with InMemoryDB with Generators {
  sequential

  val dao = new MemoryMapCachedDao()

  val neHemisphereAvalanche = genAvalanche.sample.get.copy(extId = "4a3jr23k", viewable = true, location = Coordinate(7.59349050, 47.59349550, 2500))
  val seHemisphereAvalanche = genAvalanche.sample.get.copy(extId = "83j859j3", viewable = true, location = Coordinate(170.2395494, -44.5943285, 2500))
  val swHemisphereAvalanche = genAvalanche.sample.get.copy(extId = "2r8f883s", viewable = true, location = Coordinate(-69.59349050, -25.5349550, 2500))
  val nwHemisphereAvalanche = genAvalanche.sample.get.copy(extId = "954fi4rf", viewable = true, location = Coordinate(-106.59349050, 39.59349550, 2500))

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