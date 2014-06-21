define(['geojs'], function() {

function AvyDraw(gearthInst, gePlugin, submitDrawing) {
	var self = this;
	var gearth = gearthInst;
	var ge = gePlugin;
	var geo = window.geo;
	
	var AVY_DRAW_COLOR = '773b3bff';
	var FEET_PER_METER = 3.28084;
	
	this.drawingKmlObj = null;
	this.isMouseDown = false;
	this.lineStringPlacemark = null;
	this.coords = null;
	
	this.clearDrawing = function() {
		if (self.drawingKmlObj) {
			ge.getFeatures().removeChild(self.drawingKmlObj);
			self.drawingKmlObj = null;
		}
	}
	
	this.startAvyDraw = function() {
	//	$('#map3d').css('cursor', 'pointer');
		self.drawingKmlObj = ge.createDocument('');
		ge.getFeatures().appendChild(self.drawingKmlObj);
		gearth.addEventListener(ge.getGlobe(), 'mousemove', self.onMouseMove); 
		gearth.addEventListener(ge.getGlobe(), 'mousedown', self.onMouseDown); 
	}
	
	this.convertLineStringToPolygon = function() {
	  var polygon = ge.createPolygon('');
	  var outer = ge.createLinearRing('');
	  polygon.setOuterBoundary(outer);
	  
	  var highestCoord = ge.createCoord();
	  highestCoord.setAltitude(0);
	  var lowestCoord = ge.createCoord();
	  lowestCoord.setAltitude(9000); // meters
	  
	  var lineString = self.lineStringPlacemark.getGeometry();
	  for (var i = 0; i < lineString.getCoordinates().getLength(); i++) {
	    var coord = lineString.getCoordinates().get(i);
	    if (coord.getAltitude() > highestCoord.getAltitude()) {
	    	highestCoord = coord;
	    }
	    if (coord.getAltitude() < lowestCoord.getAltitude()) {
	    	lowestCoord = coord;
	    }
	    outer.getCoordinates().pushLatLngAlt(coord.getLatitude(), coord.getLongitude(), coord.getAltitude());
	  }
	
	  // replace the lineString with the polygon
	  self.lineStringPlacemark.setGeometry(polygon); 
	  
	  var highPoint = new geo.Point(highestCoord);
	  var lowPoint = new geo.Point(lowestCoord);
	  var midPoint = highPoint.midpoint(lowPoint);
	  var heading = highPoint.heading(lowPoint);
	  var hDist = highPoint.distance(lowPoint);
	  var vDist = highPoint.altitude() - lowPoint.altitude();  
	  var angleRadians = Math.asin(vDist/hDist);
	  
	  submitDrawing(midPoint.lat(), midPoint.lng(), 
			  Math.round(highPoint.altitude() * FEET_PER_METER), 
			  self.headingToDirection(heading.toFixed(1)),
			  Math.round(angleRadians * (180/Math.PI)),
			  self.drawingKmlObj.getKml());
	}
	
	this.headingToDirection = function(heading) {
		if (heading > 22.5 && heading <= 67.5) return "NE";
		if (heading > 67.5 && heading <= 112.5) return "E";
		if (heading > 112.5 && heading <= 157.5) return "SE";
		if (heading > 157.5 && heading <= 202.5) return "S";
		if (heading > 202.5 && heading <= 247.5) return "SW";
		if (heading > 247.5 && heading <= 292.5) return "W";
		if (heading > 292.5 && heading <= 337.5) return "NW";
		return "N";
	}
	
	this.onMouseMove = function(event) {
	  if (self.isMouseDown) {
	    self.coords.pushLatLngAlt(event.getLatitude(), event.getLongitude(), event.getAltitude());
	  }
	}
	
	this.onMouseDown = function(event) {
	  if (self.isMouseDown) {
	    self.isMouseDown = false;
	    gearth.removeEventListener(ge.getGlobe(), 'mousemove', self.onMouseMove); 
		gearth.removeEventListener(ge.getGlobe(), 'mousedown', self.onMouseDown);
		
	    self.coords.pushLatLngAlt(event.getLatitude(), event.getLongitude(), event.getAltitude());
	    self.convertLineStringToPolygon();
	  } else {
	    self.isMouseDown = true;
	
	    self.lineStringPlacemark = ge.createPlacemark('');
	    var lineString = ge.createLineString('');
	    self.lineStringPlacemark.setGeometry(lineString);
	    lineString.setTessellate(true);
	    lineString.setAltitudeMode(ge.ALTITUDE_CLAMP_TO_GROUND);
	
	    self.lineStringPlacemark.setStyleSelector(ge.createStyle(''));
	    var lineStyle = self.lineStringPlacemark.getStyleSelector().getLineStyle();
	    lineStyle.setWidth(4);
	    lineStyle.getColor().set(AVY_DRAW_COLOR);
	    lineStyle.setColorMode(ge.COLOR_NORMAL);
	    
	    var polyStyle = self.lineStringPlacemark.getStyleSelector().getPolyStyle();
	    polyStyle.getColor().set(AVY_DRAW_COLOR);
	    polyStyle.setColorMode(ge.COLOR_NORMAL);
	
	    self.coords = lineString.getCoordinates();
	    self.coords.pushLatLngAlt(event.getLatitude(), event.getLongitude(), event.getAltitude());
	    
	    self.drawingKmlObj.getFeatures().appendChild(self.lineStringPlacemark);
	  }
	}
}

return AvyDraw;
});