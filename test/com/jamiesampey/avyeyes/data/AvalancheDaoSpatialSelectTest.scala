package com.jamiesampey.avyeyes.data

import com.jamiesampey.avyeyes.model.Coordinate
import play.api.test.WithApplication


class AvalancheDaoSpatialSelectTest extends DatabaseTest {

  implicit val subject = injector.instanceOf[AvalancheDao]

  val neAvalanche = genAvalanche.generate.copy(viewable = true, location = Coordinate(7.59349050, 47.59349550, 2500))
  val seAvalanche = genAvalanche.generate.copy(viewable = true, location = Coordinate(170.2395494, -44.5943285, 2500))
  val swAvalanche = genAvalanche.generate.copy(viewable = true, location = Coordinate(-69.59349050, -25.5349550, 2500))
  val nwAvalanche = genAvalanche.generate.copy(viewable = true, location = Coordinate(-106.59349050, 39.59349550, 2500))

  "Latitude/Longitude filtering in all four hemispheres" should {
    "NE hemisphere lat/lng filtering works" in new WithApplication(appBuilder.build) {
      insertAvalanches(neAvalanche, seAvalanche, swAvalanche, nwAvalanche)

      val neLatLngInBoundsCriteria = AvalancheSpatialQuery(geoBounds =
        Some(createGeoBoundsToInclude(neAvalanche.location)))

      val resultList = subject.getAvalanches(neLatLngInBoundsCriteria)

      resultList must haveLength(1)
      resultList.head.extId mustEqual neAvalanche.extId
    }

    "SE hemisphere lat/lng filtering works" in new WithApplication(appBuilder.build) {
      insertAvalanches(neAvalanche, seAvalanche, swAvalanche, nwAvalanche)

      val seLatLngInBoundsCriteria = AvalancheSpatialQuery(geoBounds =
        Some(createGeoBoundsToInclude(seAvalanche.location)))

      val resultList = subject.getAvalanches(seLatLngInBoundsCriteria)

      resultList must haveLength(1)
      resultList.head.extId mustEqual seAvalanche.extId
    }

    "SW hemisphere lat/lng filtering works" in new WithApplication(appBuilder.build) {
      insertAvalanches(neAvalanche, seAvalanche, swAvalanche, nwAvalanche)

      val swLatLngInBoundsCriteria = AvalancheSpatialQuery(geoBounds =
        Some(createGeoBoundsToInclude(swAvalanche.location)))

      val resultList = subject.getAvalanches(swLatLngInBoundsCriteria)

      resultList must haveLength(1)
      resultList.head.extId mustEqual swAvalanche.extId
    }

    "NW hemisphere lat/lng filtering works" in new WithApplication(appBuilder.build) {
      insertAvalanches(neAvalanche, seAvalanche, swAvalanche, nwAvalanche)

      val nwLatLngInBoundsCriteria = AvalancheSpatialQuery(geoBounds =
        Some(createGeoBoundsToInclude(nwAvalanche.location)))

      val resultList = subject.getAvalanches(nwLatLngInBoundsCriteria)

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
