define(['gearth', 
        'gmaps', 
        'geplugin', 
        'ae-view'], 
        function(gearthMock, gmapsMock, gePluginMock, AvyEyesView) {
	
	var gearth = new gearthMock();
	var geplugin = new gePluginMock();
	var gmaps = new gmapsMock();
	
	var view;
	var geFeaturesMock = geplugin.getFeatures();
	
	describe('Create and cancel a report', function() {
		beforeEach(function() {
			view = new AvyEyesView(gearth, gmaps);
			view.setGE(geplugin);
		});
		
		it('Creates and initializes a new report', function() {
			spyOn(view, 'hideSearchDiv');
			
			expect(view.currentReport).toBeNull();
			expect(view.isFirstReport).toBe(true);
			view.doReport();
			expect(view.currentReport).not.toBeNull();
			expect(view.isFirstReport).toBe(false);
		});

		it('Cancels an existing report', function() {
			spyOn(view, 'stopNavControlBlink');
			spyOn(view, 'showSearchDiv');
			
			view.doReport();
			expect(view.currentReport).not.toBeNull();
			
			var report = view.currentReport;
			spyOn(report, 'clearAllFields');
			spyOn(report, 'clearAvyDrawing');
			
			view.cancelReport();
			
			expect(report.clearAllFields).toHaveBeenCalled();
			expect(report.clearAvyDrawing).toHaveBeenCalled();
			expect(view.currentReport).toBeNull();
			expect(view.stopNavControlBlink).toHaveBeenCalled();
		});
	});

	describe('Show and remove search results', function() {
		beforeEach(function() {
			view = new AvyEyesView(gearth, gmaps);
			view.setGE(geplugin);
		});
		
		it('Shows search results KML', function() {
			spyOn(geplugin, 'parseKml');
			spyOn(geFeaturesMock, 'appendChild');
			var kmlStr = '<kml>some data</kml>';
			
			expect(view.avySearchResultKmlObj).toBeNull();
			
			view.overlaySearchResultKml(kmlStr);
			
			expect(geplugin.parseKml).toHaveBeenCalledWith(kmlStr);
			expect(view.avySearchResultKmlObj).not.toBeNull();
			expect(geFeaturesMock.appendChild).toHaveBeenCalled();
		});
		
		it('Clears search results KML', function() {
			spyOn(geFeaturesMock, 'hasChildNodes').andReturn(true);
			spyOn(geFeaturesMock, 'removeChild');
			var kmlStr = '<kml>some data</kml>';
			view.overlaySearchResultKml(kmlStr);
			expect(view.avySearchResultKmlObj).not.toBeNull();
			
			view.clearSearchResultKml();
			
			expect(geFeaturesMock.hasChildNodes).toHaveBeenCalled();
			expect(geFeaturesMock.removeChild).toHaveBeenCalled();
			expect(view.avySearchResultKmlObj).toBeNull();
		});
	});
});