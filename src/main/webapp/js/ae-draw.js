function AvyDraw(submitDrawing) {
	var self = this;
	
	var AVY_DRAW_COLOR = '773b3bff';
	var FEET_PER_METER = 3.28084;
	
	var drawingKmlObj = null;
	var isMouseDown = false;
	var lineStringPlacemark = null;
	var coords = null;
	
	this.clearDrawing = function() {
		if (drawingKmlObj) {
			ge.getFeatures().removeChild(drawingKmlObj);
			drawingKmlObj = null;
		}
	}
	
	this.startAvyDraw = function() {
	//	$('#map3d').css('cursor', 'pointer');
		drawingKmlObj = ge.createDocument('');
		ge.getFeatures().appendChild(drawingKmlObj);
		google.earth.addEventListener(ge.getGlobe(), 'mousemove', this.onMouseMove); 
		google.earth.addEventListener(ge.getGlobe(), 'mousedown', this.onMouseDown); 
	}
	
	this.convertLineStringToPolygon = function() {
	  var polygon = ge.createPolygon('');
	  var outer = ge.createLinearRing('');
	  polygon.setOuterBoundary(outer);
	  var lineString = lineStringPlacemark.getGeometry();
	  
	  var highestCoord = ge.createCoord();
	  highestCoord.setAltitude(0);
	  var lowestCoord = ge.createCoord();
	  lowestCoord.setAltitude(9000); // meters
	  
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
	
	  lineStringPlacemark.setGeometry(polygon);
	  
	  var highPoint = new geo.Point(highestCoord);
	  var lowPoint = new geo.Point(lowestCoord);
	  var heading = highPoint.heading(lowPoint);
	  var hDist = highPoint.distance(lowPoint);
	  var vDist = highestCoord.getAltitude() - lowestCoord.getAltitude();  
	  var angleRadians = Math.asin(vDist/hDist);
	  
	  submitDrawing(highestCoord.getLatitude(), 
			  highestCoord.getLongitude(), 
			  Math.round(highestCoord.getAltitude() * FEET_PER_METER), 
			  self.headingToDirection(heading.toFixed(1)),
			  Math.round(angleRadians * (180/Math.PI)),
			  drawingKmlObj.getKml());
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
	  if (isMouseDown) {
	    coords.pushLatLngAlt(event.getLatitude(), event.getLongitude(), event.getAltitude());
	  }
	}
	
	this.onMouseDown = function(event) {
	  if (isMouseDown) {
	    isMouseDown = false;
	    google.earth.removeEventListener(ge.getGlobe(), 'mousemove', this.onMouseMove); 
		google.earth.removeEventListener(ge.getGlobe(), 'mousedown', this.onMouseDown);
		
	    coords.pushLatLngAlt(event.getLatitude(), event.getLongitude(), event.getAltitude());
	    self.convertLineStringToPolygon();
	  } else {
	    isMouseDown = true;
	
	    lineStringPlacemark = ge.createPlacemark('');
	    var lineString = ge.createLineString('');
	    lineStringPlacemark.setGeometry(lineString);
	    lineString.setTessellate(true);
	    lineString.setAltitudeMode(ge.ALTITUDE_CLAMP_TO_GROUND);
	
	    lineStringPlacemark.setStyleSelector(ge.createStyle(''));
	    var lineStyle = lineStringPlacemark.getStyleSelector().getLineStyle();
	    lineStyle.setWidth(4);
	    lineStyle.getColor().set(AVY_DRAW_COLOR);
	    lineStyle.setColorMode(ge.COLOR_NORMAL);
	    
	    var polyStyle = lineStringPlacemark.getStyleSelector().getPolyStyle();
	    polyStyle.getColor().set(AVY_DRAW_COLOR);
	    polyStyle.setColorMode(ge.COLOR_NORMAL);
	
	    coords = lineString.getCoordinates();
	    coords.pushLatLngAlt(event.getLatitude(), event.getLongitude(), event.getAltitude());
	    
	    drawingKmlObj.getFeatures().appendChild(lineStringPlacemark);
	  }
	}
}