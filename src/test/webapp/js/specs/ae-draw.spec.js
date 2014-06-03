define(['jquery', 'gearth', 'geplugin', 'ae-draw'], 
		function($, gearthMock, gePluginMock, AvyDraw) {
	
	var gearth = new gearthMock();
	var geplugin = new gePluginMock();
	var gefeatures = geplugin.getFeatures(); 
	
	var drawing;
	var submitDrawingCallbackMock = function(lat, lng, alt, aspect, angle, kml) {}
	
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
			expect(coords.lat).toEqual(eventLat);
			expect(coords.lng).toEqual(eventLng);
			expect(coords.alt).toEqual(eventAlt);
			
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
			expect(drawing.coords.lat).toBeUndefined();
			expect(drawing.coords.lng).toBeUndefined();
			expect(drawing.coords.alt).toBeUndefined();
			
			drawing.isMouseDown = true;
			drawing.onMouseMove(new mouseEventMock());
			expect(drawing.coords.lat).toEqual(eventLat);
			expect(drawing.coords.lng).toEqual(eventLng);
			expect(drawing.coords.alt).toEqual(eventAlt);
		});
	});
});