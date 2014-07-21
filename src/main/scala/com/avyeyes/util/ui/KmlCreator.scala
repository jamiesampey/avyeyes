package com.avyeyes.util.ui

import com.avyeyes.model.Avalanche
import com.avyeyes.model.enums._
import com.avyeyes.util.AEHelpers._
import com.avyeyes.util.AEConstants._
import scala.xml._
import net.liftweb.util.Props

class KmlCreator {
	def createCompositeKml(avalanches: Avalanche*): Node = {
	  Utility.trim(<kml xmlns="http://www.opengis.net/kml/2.2" xmlns:gx="http://www.google.com/kml/ext/2.2" 
	        xmlns:kml="http://www.opengis.net/kml/2.2" xmlns:atom="http://www.w3.org/2005/Atom">
	        <Document>{for(a <- avalanches) yield createPlacemark(a)}</Document></kml>
	        )
	}
	
	private def createPlacemark(avalanche: Avalanche): Node = {
	  Utility.trim(
    	  <Placemark>
	          <description><div id="avyExtId">{avalanche.extId}</div></description>
	          <Style>{getStyle}</Style>
	          <Polygon>{getPolygon(avalanche)}</Polygon>
	      </Placemark>
	  )
	}
	
	private def getHtmlXXXDesc(avalanche: Avalanche): Node = {
	        <table>
                  <tr><td colspan="2"><u>{avalanche.avyDate}:&nbsp;{avalanche.areaName}</u></td></tr>               
                  <tr><td>
                      <table>
                          <tr><td>Type:</td><td>{AvalancheType.getEnumLabel(avalanche.avyType)}</td></tr>
                          <tr><td>Trigger:</td><td>{AvalancheTrigger.getEnumLabel(avalanche.trigger)}</td></tr>
                          <tr><td>Interface:</td><td>{AvalancheInterface.getEnumLabel(avalanche.bedSurface)}</td></tr>
                          <tr><td>R Size:</td><td>{sizeToStr(avalanche.rSize)}</td></tr>
                          <tr><td>D Size:</td><td>{sizeToStr(avalanche.dSize)}</td></tr>
                      </table>
                  </td>
                  <td>
                      <table>
                          <tr><td>Caught:</td><td>{humanNumberToStr(avalanche.caught)}</td></tr>
                          <tr><td>Partially Buried:</td><td>{humanNumberToStr(avalanche.partiallyBuried)}</td></tr>
                          <tr><td>Fully Buried:</td><td>{humanNumberToStr(avalanche.fullyBuried)}</td></tr>
                          <tr><td>Injured:</td><td>{humanNumberToStr(avalanche.injured)}</td></tr>
                          <tr><td>Killed:</td><td>{humanNumberToStr(avalanche.killed)}</td></tr>
                      </table>
                  </td></tr>
                  <tr><td>&nbsp;</td></tr>
                  <tr><td colspan="2"><u>comments</u>:</td></tr>
                  <tr><td colspan="2">{if (!avalanche.comments.isEmpty) avalanche.comments.get}</td></tr>
                  { if (Props.get("base.url").isDefined) {
                      <tr><td colspan="2">External URL: {Props.get("base.url").get + avalanche.extId}</td></tr>
                    }
                  }
          </table>
	}
	
	private def getStyle: NodeSeq = {
        <LineStyle><color>773b3bff</color><colorMode>normal</colorMode><width>4</width></LineStyle>
        <PolyStyle><color>773b3bff</color><colorMode>normal</colorMode></PolyStyle>
	}
	
	private def getPolygon(avalanche: Avalanche): Node = 
	    <outerBoundaryIs><LinearRing><coordinates>{avalanche.kmlCoords}</coordinates></LinearRing></outerBoundaryIs>
}