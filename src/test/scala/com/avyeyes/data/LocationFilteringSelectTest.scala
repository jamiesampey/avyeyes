package com.avyeyes.data

import com.avyeyes.model.Coordinate
import com.avyeyes.test.Generators._
import org.specs2.mutable.Specification

class LocationFilteringSelectTest extends Specification with InMemoryDB {
  sequential

  val dao = new MemoryMapCachedDao(NotAuthorized)

  val neHemisphereAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(7.59349050, 47.59349550, 2500))
  val seHemisphereAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(170.2395494, -44.5943285, 2500))
  val swHemisphereAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(-69.59349050, -25.5349550, 2500))
  val nwHemisphereAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(-106.59349050, 39.59349550, 2500))

  "Latitude/Longitude filtering in all four hemispheres" >> {
    "NE hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val neLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(neHemisphereAvalanche.location)))
      
      val resultList = dao.getAvalanches(neLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== neHemisphereAvalanche.extId
    }
  
    "SE hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val seLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(seHemisphereAvalanche.location)))
      
      val resultList = dao.getAvalanches(seLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== seHemisphereAvalanche.extId
    }
    
    "SW hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val swLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(swHemisphereAvalanche.location)))
      
      val resultList = dao.getAvalanches(swLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== swHemisphereAvalanche.extId
    }
    
    "NW hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val nwLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(nwHemisphereAvalanche.location)))
      
      val resultList = dao.getAvalanches(nwLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== nwHemisphereAvalanche.extId
    }
  }
  
  private def createGeoBoundsToInclude(c: Coordinate) = {
    GeoBounds(c.latitude+.01, c.longitude+.01, c.latitude-.01, c.longitude-.01)
  }
  
  private def insertAllAvalanches() = {
    dao.insertAvalanche(neHemisphereAvalanche)
    dao.insertAvalanche(nwHemisphereAvalanche)
    dao.insertAvalanche(swHemisphereAvalanche)
    dao.insertAvalanche(seHemisphereAvalanche)
  }
}