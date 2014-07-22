package com.avyeyes.util.ui

import com.avyeyes.model.Avalanche
import scala.xml._

class KmlCreator {
	def createCompositeKml(avalanches: Avalanche*): Node = {
	  Utility.trim(<kml xmlns="http://www.opengis.net/kml/2.2" xmlns:gx="http://www.google.com/kml/ext/2.2" 
	        xmlns:kml="http://www.opengis.net/kml/2.2" xmlns:atom="http://www.w3.org/2005/Atom">
	        <Document>
	          <Style id="avystyle">
                  <BalloonStyle><bgColor>77003300</bgColor><textColor>ff770055</textColor></BalloonStyle>
	              <LineStyle><color>773b3bff</color><colorMode>normal</colorMode><width>4</width></LineStyle>
	              <PolyStyle><color>773b3bff</color><colorMode>normal</colorMode></PolyStyle>
	          </Style>
	          {for(a <- avalanches) yield createPlacemark(a)}
	        </Document></kml>
	        )
	}
	
	private def createPlacemark(avalanche: Avalanche): Node = {
	  Utility.trim(
    	  <Placemark id={avalanche.extId}>
	          <styleUrl>#avystyle</styleUrl>
	          <Polygon>{getPolygon(avalanche)}</Polygon>
	      </Placemark>
	  )
	}
	
	private def getPolygon(avalanche: Avalanche): Node = {
	    <outerBoundaryIs><LinearRing><coordinates>{avalanche.kmlCoords}</coordinates></LinearRing></outerBoundaryIs>
	}
}