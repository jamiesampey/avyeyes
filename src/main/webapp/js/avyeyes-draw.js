define(['geojs'], function() {

function AvyDraw(avyReport) {
	this.report = avyReport;
	this.gearth = avyReport.view.gearth;
	this.ge = avyReport.view.ge;
	
	this.AVY_DRAW_COLOR = '773b3bff';
	
	this.drawingKmlObj = null;
	this.isMouseDown = false;
	this.lineStringPlacemark = null;
	this.coords = null;
}

AvyDraw.prototype.clearDrawing = function() {
	this.report.view.clearKmlOverlay();
	this.drawingKmlObj = null;
}

AvyDraw.prototype.startAvyDraw = function() {
//	$('#map3d').css('cursor', 'pointer');
	this.drawingKmlObj = this.ge.createDocument('');
	this.ge.getFeatures().appendChild(this.drawingKmlObj);
	this.gearth.addEventListener(this.ge.getGlobe(), 'mousemove', (this.onMouseMove).bind(this)); 
	this.gearth.addEventListener(this.ge.getGlobe(), 'mousedown', (this.onMouseDown).bind(this)); 
}

AvyDraw.prototype.convertLineStringToPolygon = function() {
  var polygon = this.ge.createPolygon('');
  var outer = this.ge.createLinearRing('');
  polygon.setOuterBoundary(outer);
  
  var highestCoord = this.ge.createCoord();
  highestCoord.setAltitude(0);
  var lowestCoord = this.ge.createCoord();
  lowestCoord.setAltitude(9000); // meters
  
  var lineString = this.lineStringPlacemark.getGeometry();
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
  this.lineStringPlacemark.setGeometry(polygon); 
  
  var highPoint = new geo.Point(highestCoord);
  var lowPoint = new geo.Point(lowestCoord);
  var midPoint = highPoint.midpoint(lowPoint);
  var heading = highPoint.heading(lowPoint);
  var hDist = highPoint.distance(lowPoint);
  var vDist = highPoint.altitude() - lowPoint.altitude();  
  var angleRadians = Math.asin(vDist/hDist);
  
  this.report.setAvyDrawingHiddenInputs(midPoint.lat(), midPoint.lng(), Math.round(highPoint.altitude()), 
	  this.headingToDirection(heading.toFixed(1)), 
	  Math.round(angleRadians * (180/Math.PI)), 
	  this.drawingKmlObj.getKml());
  
  this.report.confirmDrawing();
}

AvyDraw.prototype.headingToDirection = function(heading) {
	if (heading > 22.5 && heading <= 67.5) return "NE";
	if (heading > 67.5 && heading <= 112.5) return "E";
	if (heading > 112.5 && heading <= 157.5) return "SE";
	if (heading > 157.5 && heading <= 202.5) return "S";
	if (heading > 202.5 && heading <= 247.5) return "SW";
	if (heading > 247.5 && heading <= 292.5) return "W";
	if (heading > 292.5 && heading <= 337.5) return "NW";
	return "N";
}

AvyDraw.prototype.onMouseMove = function(event) {
  if (this.isMouseDown) {
    this.coords.pushLatLngAlt(event.getLatitude(), event.getLongitude(), event.getAltitude());
  }
}

AvyDraw.prototype.onMouseDown = function(event) {
  if (this.isMouseDown) {
    this.isMouseDown = false;
    this.gearth.removeEventListener(this.ge.getGlobe(), 'mousemove', this.onMouseMove); 
	this.gearth.removeEventListener(this.ge.getGlobe(), 'mousedown', this.onMouseDown);
	
    this.coords.pushLatLngAlt(event.getLatitude(), event.getLongitude(), event.getAltitude());
    this.convertLineStringToPolygon();
  } else {
    this.isMouseDown = true;

    this.lineStringPlacemark = this.ge.createPlacemark('');
    var lineString = this.ge.createLineString('');
    this.lineStringPlacemark.setGeometry(lineString);
    lineString.setTessellate(true);
    lineString.setAltitudeMode(this.ge.ALTITUDE_CLAMP_TO_GROUND);

    this.lineStringPlacemark.setStyleSelector(this.ge.createStyle(''));
    var lineStyle = this.lineStringPlacemark.getStyleSelector().getLineStyle();
    lineStyle.setWidth(4);
    lineStyle.getColor().set(this.AVY_DRAW_COLOR);
    lineStyle.setColorMode(this.ge.COLOR_NORMAL);
    
    var polyStyle = this.lineStringPlacemark.getStyleSelector().getPolyStyle();
    polyStyle.getColor().set(this.AVY_DRAW_COLOR);
    polyStyle.setColorMode(this.ge.COLOR_NORMAL);

    this.coords = lineString.getCoordinates();
    this.coords.pushLatLngAlt(event.getLatitude(), event.getLongitude(), event.getAltitude());
    
    this.drawingKmlObj.getFeatures().appendChild(this.lineStringPlacemark);
  }
}

return AvyDraw;
});