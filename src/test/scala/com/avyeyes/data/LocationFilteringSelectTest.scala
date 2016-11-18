package com.avyeyes.data

import com.avyeyes.model.Coordinate
import com.avyeyes.test.Generators._
import org.specs2.mutable.Specification

class LocationFilteringSelectTest extends Specification with InMemoryDB {
  sequential

  val neAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(7.59349050, 47.59349550, 2500))
  val seAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(170.2395494, -44.5943285, 2500))
  val swAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(-69.59349050, -25.5349550, 2500))
  val nwAvalanche = avalancheForTest.copy(viewable = true, location = Coordinate(-106.59349050, 39.59349550, 2500))

  "Latitude/Longitude filtering in all four hemispheres" >> {
    "NE hemisphere lat/lng filtering works" >> {
      insertAvalanches(neAvalanche, seAvalanche, swAvalanche, nwAvalanche)

      val neLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(neAvalanche.location)))

      val resultList = dal.getAvalanches(neLatLngInBoundsCriteria)

      resultList must haveLength(1)
      resultList.head.extId mustEqual neAvalanche.extId
    }

    "SE hemisphere lat/lng filtering works" >> {
      insertAvalanches(neAvalanche, seAvalanche, swAvalanche, nwAvalanche)

      val seLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(seAvalanche.location)))

      val resultList = dal.getAvalanches(seLatLngInBoundsCriteria)

      resultList must haveLength(1)
      resultList.head.extId mustEqual seAvalanche.extId
    }

    "SW hemisphere lat/lng filtering works" >> {
      insertAvalanches(neAvalanche, seAvalanche, swAvalanche, nwAvalanche)

      val swLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(swAvalanche.location)))

      val resultList = dal.getAvalanches(swLatLngInBoundsCriteria)

      resultList must haveLength(1)
      resultList.head.extId mustEqual swAvalanche.extId
    }

    "NW hemisphere lat/lng filtering works" >> {
      insertAvalanches(neAvalanche, seAvalanche, swAvalanche, nwAvalanche)

      val nwLatLngInBoundsCriteria = AvalancheQuery(geoBounds =
        Some(createGeoBoundsToInclude(nwAvalanche.location)))

      val resultList = dal.getAvalanches(nwLatLngInBoundsCriteria)

      resultList must haveLength(1)
      resultList.head.extId mustEqual nwAvalanche.extId
    }
  }

  private def createGeoBoundsToInclude(c: Coordinate) = {
    GeoBounds(
      lngMax = c.longitude+.01,
      lngMin = c.longitude-.01,
      latMax = c.latitude+.01,
      latMin = c.latitude-.01)
  }
}