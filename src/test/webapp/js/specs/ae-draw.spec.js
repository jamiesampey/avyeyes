define(['jquery', 'gearth', 'geplugin', 'ae-draw'], 
		function($, gearthMock, gePluginMock, AvyDraw) {
	
	var gearth = new gearthMock();
	var geplugin = new gePluginMock();
	var gefeatures = geplugin.getFeatures(); 

	var callbackLat, callbackLng, callbackAlt, callbackAspect, callbackAngle, callbackKml;
	var submitDrawingCallbackMock = function(lat, lng, alt, aspect, angle, kml) {
		callbackLat = lat;
		callbackLng = lng;
		callbackAlt = alt;
		callbackAspect = aspect;
		callbackAngle = angle;
		callbackKml = kml;
	}
	
	var DrawingKmlObjMock = function(kml) {
		this.kml = kml;
		this.getKml = function() {return this.kml;}
	}
	
	var drawing;
	
	describe('Starting and clearing a drawing', function() {
		beforeEach(function() {
			spyOn(geplugin, 'createDocument').andCallThrough();
			spyOn(gearth, 'addEventListener');
			spyOn(gefeatures, 'appendChild');
			spyOn(gefeatures, 'removeChild');

			drawing = new AvyDraw(gearth, geplugin, submitDrawingCallbackMock);
		});
		
		it('Starts a drawing correctly', function() {
			expect(drawing.drawingKmlObj).toBeNull();
			drawing.startAvyDraw();
			
			expect(drawing.drawingKmlObj).not.toBeNull();
			expect(drawing.isMouseDown).toEqual(false);
			
			expect(geplugin.createDocument).toHaveBeenCalledWith('');
			expect(gefeatures.appendChild).toHaveBeenCalledWith(drawing.drawingKmlObj);
			
			expect(gearth.addEventListener.callCount).toEqual(2);
			expect(gearth.addEventListener).toHaveBeenCalledWith(geplugin.getGlobe(), 'mousemove', drawing.onMouseMove);
			expect(gearth.addEventListener).toHaveBeenCalledWith(geplugin.getGlobe(), 'mousedown', drawing.onMouseDown);
		});
		
		it('Clears a drawing correctly', function() {
			drawing.startAvyDraw();
			expect(drawing.drawingKmlObj).not.toBeNull();
			var kmlObj = drawing.drawingKmlObj;
			
			drawing.clearDrawing();
			expect(drawing.drawingKmlObj).toBeNull();
			expect(gefeatures.removeChild).toHaveBeenCalledWith(kmlObj);
		});
	});

	describe('MouseDown and MouseMove event handlers', function() {
		var eventLat = 34.254324;
		var eventLng = 178.345345;
		var eventAlt = 12990;
		var avyColorConst = '773b3bff';
		
		var mouseEventMock = function() {
			this.getLatitude = function() {return eventLat;}
			this.getLongitude = function() {return eventLng;}
			this.getAltitude = function() {return eventAlt;}
		}

		beforeEach(function() {
			spyOn(geplugin, 'createPlacemark').andCallThrough();
			spyOn(geplugin, 'createLineString').andCallThrough();
			spyOn(gearth, 'addEventListener');
			spyOn(gearth, 'removeEventListener');
			
			drawing = new AvyDraw(gearth, geplugin, submitDrawingCallbackMock);
			drawing.startAvyDraw();
		});
		
		it('Handles MouseDown up/down cycle correctly', function() {
			spyOn(drawing, 'convertLineStringToPolygon');
			
			// MOUSE DOWN
			expect(drawing.drawingKmlObj).not.toBeNull();
			expect(drawing.isMouseDown).toEqual(false);
			
			drawing.onMouseDown(new mouseEventMock());

			expect(drawing.isMouseDown).toEqual(true);
			expect(geplugin.createPlacemark).toHaveBeenCalledWith('');
			expect(geplugin.createLineString).toHaveBeenCalledWith('');
			
			var lineString = drawing.lineStringPlacemark.geometry;
			expect(lineString).not.toBeNull();
			expect(lineString.tessellate).toEqual(true);
			expect(lineString.altitudeMode).toEqual(geplugin.ALTITUDE_CLAMP_TO_GROUND);
			expect(drawing.coords).toEqual(lineString.coords);
			
			var lineStyle = drawing.lineStringPlacemark.getStyleSelector().getLineStyle();
			expect(lineStyle).not.toBeNull();
			expect(lineStyle.width).toEqual(4);
			expect(lineStyle.gecolor.color).toEqual(avyColorConst);
			expect(lineStyle.colorMode).toEqual(geplugin.COLOR_NORMAL);
			
			var polyStyle = drawing.lineStringPlacemark.getStyleSelector().getPolyStyle();
			expect(polyStyle).not.toBeNull();
			expect(polyStyle.gecolor.color).toEqual(avyColorConst);
			expect(polyStyle.colorMode).toEqual(geplugin.COLOR_NORMAL);
			
			var coords = lineString.getCoordinates();
			expect(coords.get(0).lat).toEqual(eventLat);
			expect(coords.get(0).lng).toEqual(eventLng);
			expect(coords.get(0).alt).toEqual(eventAlt);
			
			// MOUSE UP
			drawing.onMouseDown(new mouseEventMock());
			
			expect(drawing.isMouseDown).toEqual(false);
			expect(gearth.removeEventListener.callCount).toEqual(2);
			expect(gearth.removeEventListener).toHaveBeenCalledWith(geplugin.getGlobe(), 'mousemove', drawing.onMouseMove);
			expect(gearth.removeEventListener).toHaveBeenCalledWith(geplugin.getGlobe(), 'mousedown', drawing.onMouseDown);			
			expect(drawing.convertLineStringToPolygon).toHaveBeenCalled();
		});
		
		it('Handles MouseMove event correctly', function() {
			expect(drawing.drawingKmlObj).not.toBeNull();
			drawing.coords = geplugin.createGECoords();
			
			drawing.isMouseDown = false;
			drawing.onMouseMove(new mouseEventMock());
			expect(drawing.coords.get(0)).toBeUndefined();
			
			drawing.isMouseDown = true;
			drawing.onMouseMove(new mouseEventMock());
			expect(drawing.coords.get(0).lat).toEqual(eventLat);
			expect(drawing.coords.get(0).lng).toEqual(eventLng);
			expect(drawing.coords.get(0).alt).toEqual(eventAlt);
		});
	}) ;
	
	describe('Complete the drawing', function() {
		it('Does all necessary operations to complete a report drawing', function() {
			spyOn(gearth, 'addEventListener');
			spyOn(gefeatures, 'appendChild');
			
			drawing = new AvyDraw(gearth, geplugin, submitDrawingCallbackMock);
			drawing.startAvyDraw();
			
			spyOn(geplugin, 'createPolygon').andCallThrough();
			spyOn(geplugin, 'createLinearRing').andCallThrough();

			var highestCoordAlt = 2664.465388286975;
			
			var lineString = geplugin.createLineString('');
			lineString.getCoordinates().pushLatLngAlt(39.1878171317089, -106.7994083904971, 2656.033783100885);
			lineString.getCoordinates().pushLatLngAlt(39.18531355425417, -106.8007093577329, 2529.879913995753);
			lineString.getCoordinates().pushLatLngAlt(39.18575752359928, -106.798308756752, 2630.662674342588);
			lineString.getCoordinates().pushLatLngAlt(39.18738953399846, -106.7985430418609, highestCoordAlt);
			
			drawing.lineStringPlacemark = geplugin.createPlacemark('');
			drawing.lineStringPlacemark.setGeometry(lineString);
			var testKml = '<kml>some coords n stuff</kml>';
			drawing.drawingKmlObj = new DrawingKmlObjMock(testKml);
			
			window.geo._heading = 123.43953; // South east aspect
			window.geo._distance = 4554.3922;
			
			var midpointCoord = geplugin.createCoord();
			midpointCoord.lat = 39.745313;
			midpointCoord.lng = 106.90370;
			window.geo._midpointCoord = midpointCoord;
			
			drawing.convertLineStringToPolygon();
			
			expect(geplugin.createPolygon).toHaveBeenCalled();
			expect(geplugin.createLinearRing).toHaveBeenCalled();
			
			expect(callbackLat).toEqual(window.geo._midpointCoord.getLatitude());
			expect(callbackLng).toEqual(window.geo._midpointCoord.getLongitude());
			expect(callbackAlt).toEqual(Math.round(highestCoordAlt * 3.28084)); // meter to ft conversion
			expect(callbackAspect).toEqual('SE');
			expect(callbackKml).toEqual(testKml);
		});
		
		it('Translates heading to compass direction correctly', function() {
			drawing = new AvyDraw(gearth, geplugin, submitDrawingCallbackMock);

			expect(drawing.headingToDirection(22.6)).toEqual('NE');
			expect(drawing.headingToDirection(112.5)).toEqual('E');
			expect(drawing.headingToDirection(112.6)).toEqual('SE');
			expect(drawing.headingToDirection(157.6)).toEqual('S');
			expect(drawing.headingToDirection(202.6)).toEqual('SW');
			expect(drawing.headingToDirection(247.6)).toEqual('W');
			expect(drawing.headingToDirection(292.6)).toEqual('NW');
			expect(drawing.headingToDirection(0)).toEqual('N');
		});
	});
});