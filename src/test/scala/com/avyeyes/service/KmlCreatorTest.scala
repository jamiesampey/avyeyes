package com.avyeyes.service

import com.avyeyes.test._
import scala.xml.Node

class KmlCreatorTest extends WebSpec2 with AvalancheGenerator {
  class KmlCreatorTester extends KmlCreator
  val kmlCreatorTester = new KmlCreatorTester
  
  "KML creation" should {
    "Create a polygon placemark for each avalanche" in {
      val kml = kmlCreatorTester.createCompositeKml(a1, a2)
     
      (kml \\ "Placemark").length must_== 2
      getPlacemarkCoords(kml, a1.extId) must_== a1.kmlCoords
      getPlacemarkCoords(kml, a2.extId) must_== a2.kmlCoords
    }
    
    "Create a single Style element that is referrenced by each Placemark" in {
      val kml = kmlCreatorTester.createCompositeKml(a1, a2)
      (kml \\ "Style" filter (node => (node\"@id").text == "avystyle")).length must_== 1
      (kml \\ "Placemark" \ "styleUrl" filter (node => node.text == "#avystyle")).length must_== 2
    }
  }

  private def getPlacemarkCoords(kml: Node, extId: String): String = {
    val placemark = (kml \\ "Placemark" filter (node => (node\"@id").text == extId)).head
    (placemark \ "Polygon" \ "outerBoundaryIs" \ "LinearRing" \ "coordinates").text.trim
  }
  
  val a1 = avalancheAtLocationWithCoords("3irti325", true, 38.2452, -103.463456546, 
    "-105.8951615969251,39.67575007525087,3539.155788866989 "
    + "-105.8951616699156,39.67575007886357,3539.152447558959"
    + "-105.8951771328018,39.67571482697459,3540.94301740672" 
    + "-105.8951970799988,39.67569178800338,3541.717087100393"
    + "-105.895201659936,39.67570386587279,3540.702443228543"
    + "-105.8952555133666,39.67560945830774,3546.3410861578"
    + "-105.895248205176,39.6756445004707,3543.386256089723")
  val a2 = avalancheAtLocationWithCoords("5ifki49e", true, 45.294955, -99.3959532, 
    "-105.8572230506443,39.65627565264674,3961.346022515275"
    + "-105.8571982685541,39.65636231994176,3964.432277365875"
    + "-105.8571609746246,39.65649273425334,3969.076324271463"
    + "-105.8571360338461,39.65657995214261,3972.181884457349"
    + "-105.8571110282602,39.65666739242872,3975.295983541534"
    + "-105.857144157093,39.65668855005326,3973.834346906527"
    + "-105.8572188331759,39.65684020060255,3972.561308547005")
}