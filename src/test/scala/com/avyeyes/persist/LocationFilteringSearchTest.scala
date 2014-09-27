package com.avyeyes.persist

import org.specs2.mutable.Specification
import com.avyeyes.test._

class LocationFilteringSearchTest extends Specification with InMemoryDB with AvalancheGenerator {
  sequential
  val dao = new SquerylAvalancheDao(() => true)
  
  val neHemisphereAvalanche = avalancheAtLocation("4a3jr23k", true, 47.59349550, 7.59349050)
  val seHemisphereAvalanche = avalancheAtLocation("83j859j3", true, -44.5943285, 170.2395494)
  val swHemisphereAvalanche = avalancheAtLocation("2r8f883s", true, -25.5349550, -69.59349050)
  val nwHemisphereAvalanche = avalancheAtLocation("954fi4rf", true, 39.59349550, -106.59349050)

  "Latitude/Longitude filtering in all four hemispheres" >> {
    "NE hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val neLatLngInBoundsCriteria = AvalancheSearchCriteria(
        (neHemisphereAvalanche.lat+.01).toString, 
        (neHemisphereAvalanche.lng+.01).toString, 
        (neHemisphereAvalanche.lat-.01).toString, 
        (neHemisphereAvalanche.lng-.01).toString, 
        "", "", "", "", "", "", "", "")
      
      val resultList = dao.selectAvalanches(neLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== neHemisphereAvalanche.extId
    }
  
    "SE hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val seLatLngInBoundsCriteria = AvalancheSearchCriteria(
        (seHemisphereAvalanche.lat+.01).toString, 
        (seHemisphereAvalanche.lng+.01).toString, 
        (seHemisphereAvalanche.lat-.01).toString, 
        (seHemisphereAvalanche.lng-.01).toString, 
        "", "", "", "", "", "", "", "")
      
      val resultList = dao.selectAvalanches(seLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== seHemisphereAvalanche.extId
    }
    
    "SW hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val swLatLngInBoundsCriteria = AvalancheSearchCriteria(
        (swHemisphereAvalanche.lat+.01).toString, 
        (swHemisphereAvalanche.lng+.01).toString, 
        (swHemisphereAvalanche.lat-.01).toString, 
        (swHemisphereAvalanche.lng-.01).toString, 
        "", "", "", "", "", "", "", "")
      
      val resultList = dao.selectAvalanches(swLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== swHemisphereAvalanche.extId
    }
    
    "NW hemisphere lat/lng filtering works" >> {
      insertAllAvalanches
      
      val nwLatLngInBoundsCriteria = AvalancheSearchCriteria(
        (nwHemisphereAvalanche.lat+.01).toString, 
        (nwHemisphereAvalanche.lng+.01).toString, 
        (nwHemisphereAvalanche.lat-.01).toString, 
        (nwHemisphereAvalanche.lng-.01).toString, 
        "", "", "", "", "", "", "", "")
      
      val resultList = dao.selectAvalanches(nwLatLngInBoundsCriteria)
      
      resultList must have length(1)
      resultList.head.extId must_== nwHemisphereAvalanche.extId
    }
  }
  
  private def insertAllAvalanches() = {
    dao insertAvalanche neHemisphereAvalanche
    dao insertAvalanche nwHemisphereAvalanche
    dao insertAvalanche swHemisphereAvalanche
    dao insertAvalanche seHemisphereAvalanche
  }
}