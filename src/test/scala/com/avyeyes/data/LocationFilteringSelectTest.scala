package com.avyeyes.data

import com.avyeyes.model.Coordinate
import com.avyeyes.test.Generators._
import com.avyeyes.util.FutureOps._

import org.specs2.mutable.Specification

import scala.concurrent.Future

class LocationFilteringSelectTest extends Specification with InMemoryDB {
  sequential

  val neHemisphereAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(7.59349050, 47.59349550, 2500))
  val seHemisphereAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(170.2395494, -44.5943285, 2500))
  val swHemisphereAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(-69.59349050, -25.5349550, 2500))
  val nwHemisphereAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(-106.59349050, 39.59349550, 2500))

  "Latitude/Longitude filtering in all four hemispheres" >> {
    "NE hemisphere lat/lng filtering works" >> {
      insertAllAvalanches

      val neLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(neHemisphereAvalanche.location)))
      
      val resultList = dal.getAvalanches(neLatLngInBoundsCriteria)
      
      resultList must haveLength(1)
      resultList.head.extId mustEqual neHemisphereAvalanche.extId
    }
  
    "SE hemisphere lat/lng filtering works" >> {
      insertAllAvalanches

      val seLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(seHemisphereAvalanche.location)))
      
      val resultList = dal.getAvalanches(seLatLngInBoundsCriteria)
      
      resultList must haveLength(1)
      resultList.head.extId mustEqual seHemisphereAvalanche.extId
    }
    
    "SW hemisphere lat/lng filtering works" >> {
      insertAllAvalanches

      val swLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(swHemisphereAvalanche.location)))
      
      val resultList = dal.getAvalanches(swLatLngInBoundsCriteria)
      
      resultList must haveLength(1)
      resultList.head.extId mustEqual swHemisphereAvalanche.extId
    }
    
    "NW hemisphere lat/lng filtering works" >> {
      insertAllAvalanches

      val nwLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(nwHemisphereAvalanche.location)))
      
      val resultList = dal.getAvalanches(nwLatLngInBoundsCriteria)
      
      resultList must haveLength(1)
      resultList.head.extId mustEqual nwHemisphereAvalanche.extId
    }
  }
  
  private def createGeoBoundsToInclude(c: Coordinate) = {
    GeoBounds(
      lngMax = c.longitude+.01,
      lngMin = c.longitude-.01,
      latMax = c.latitude+.01,
      latMin = c.latitude-.01)
  }

  private def insertAllAvalanches = Future.sequence(List(
    dal.insertAvalanche(neHemisphereAvalanche),
    dal.insertAvalanche(nwHemisphereAvalanche),
    dal.insertAvalanche(swHemisphereAvalanche),
    dal.insertAvalanche(seHemisphereAvalanche))
  ).resolve
}