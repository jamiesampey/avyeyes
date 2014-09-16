define(['jasmine-jquery', 
        'gearth', 
        'gmaps', 
        'geplugin', 
        'ae-view', 
        'ae-report', 
        'ae-draw'], 
        function(jas$, gearthMock, gmapsMock, gePluginMock, AvyEyesView, AvyReport, AvyDraw) {
	
	var gearth = new gearthMock();
	var geplugin = new gePluginMock();
	var gmaps = new gmapsMock();
	
	var view;
	var report;
	
	describe('Init and clear the avy drawing', function() {
		beforeEach(function() {
			view = new AvyEyesView(gearth, gmaps);
			view.ge = geplugin;
			report = new AvyReport(view);
		});
		
		it('Starts a new avy drawing', function() {
			spyOn(geplugin, 'createDocument');
			spyOn(gearth, 'addEventListener');
			
			expect(report.currentDrawing).toBeNull();
			report.doAvyDrawing();
			expect(report.currentDrawing).not.toBeNull();
			expect(geplugin.createDocument).toHaveBeenCalled();
			expect(gearth.addEventListener.callCount).toEqual(2);
		});
		
		it('Begins report with geocode correctly', function() {
			spyOn(window, 'setTimeout');
			
			var address = 'some mtn somewhere';
			setFixtures('<input id="avyReportInitLocation" value="' + address + '">');
			
			spyOn(view, 'geocodeAndFlyToLocation');
			
			report.beginReportWithGeocode();
			
			expect(view.geocodeAndFlyToLocation).toHaveBeenCalledWith(address, 8000.0, 65.0);
			expect(window.setTimeout).toHaveBeenCalled();
		});

		it('Clears an avy drawing', function() {
			var geFeatures = geplugin.getFeatures();
			
			spyOn(geplugin, 'createDocument');
			spyOn(gearth, 'addEventListener');
			spyOn(geFeatures, 'removeChild');
			spyOn(report, 'setAvyDrawingHiddenInputs');
			
			report.doAvyDrawing();
			expect(report.currentDrawing).not.toBeNull();
			
			report.clearAvyDrawing();
			expect(report.currentDrawing).toBeNull();
			expect(report.setAvyDrawingHiddenInputs).toHaveBeenCalledWith('', '', '', '', '', '');
		});
		
		it('Sets avy drawing hidden inputs correctly', function() {
			setFixtures('<div>'
					+ '<input id="avyReportLat" type="hidden"/>'
	                + '<input id="avyReportLng" type="hidden"/>'
	                + '<input id="avyReportKml" type="hidden"/>'
	                + '<input id="avyReportElevation" type="text"/>'
	                + '<input id="avyReportElevationFt" type="text"/>'
	                + '<input id="avyReportAspect" type="hidden"/>'
	                + '<input id="avyReportAspectAC" type="text"/>'
	                + '<input id="avyReportAngle"/>'
	             + '</div>');
			
			var lat = 24.54453;
			var lng = 65.4435667;
			var elev = 14255;
			var aspect = 'NE';
			var angle = 45;
			var kmlStr = '<kml>some coords & stuff</kml>';
			
			report.setAvyDrawingHiddenInputs(lat, lng, elev, aspect, angle, kmlStr)
			
			expect($("#avyReportLat")).toHaveValue(lat);
			expect($("#avyReportLng")).toHaveValue(lng);
			expect($("#avyReportKml")).toHaveValue(kmlStr);
			expect($("#avyReportElevation")).toHaveValue(elev);
			expect($("#avyReportElevationFt")).toHaveValue(view.metersToFeet(elev));
			expect($("#avyReportAspect")).toHaveValue(aspect);
			expect($("#avyReportAspectAC")).toHaveValue(aspect);
			expect($("#avyReportAngle")).toHaveValue(angle);
		});
	});

});