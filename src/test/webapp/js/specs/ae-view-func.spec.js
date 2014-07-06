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
			spyOn(view, 'hideSearchMenu');
			
			expect(view.currentReport).toBeNull();
			expect(view.isFirstReport).toBe(true);
			view.doReport();
			expect(view.currentReport).not.toBeNull();
			expect(view.isFirstReport).toBe(false);
			
			expect(view.hideSearchMenu).toHaveBeenCalled();
		});

		it('Cancels an existing report', function() {
			spyOn(view, 'stopNavControlBlink');
			spyOn(view, 'showSearchMenu');
			
			view.doReport();
			expect(view.currentReport).not.toBeNull();
			
			view.cancelReport();
			expect(view.currentReport).toBeNull();
			expect(view.stopNavControlBlink).toHaveBeenCalled();
			expect(view.showSearchMenu).toHaveBeenCalled();
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
			spyOn(geFeaturesMock, 'removeChild');
			var kmlStr = '<kml>some data</kml>';
			view.overlaySearchResultKml(kmlStr);
			expect(view.avySearchResultKmlObj).not.toBeNull();
			
			view.clearSearchResultKml();
			
			expect(geFeaturesMock.removeChild).toHaveBeenCalled();
			expect(view.avySearchResultKmlObj).toBeNull();
		});
	});
});