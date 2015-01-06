define(['gearth', 
        'gmaps', 
        'geplugin', 
        'avyeyes-view'],
        function(gearthMock, gmapsMock, gePluginMock, AvyEyesView) {
	
	var gearth = new gearthMock();
	var geplugin = new gePluginMock();
	var gmaps = new gmapsMock();
	
	var view;
	var geFeaturesMock = geplugin.getFeatures();
	
	describe('AvyEyesView initialization', function() {
		beforeEach(function() {
			view = new AvyEyesView(gearth, gmaps);
		});
		
	    it('init should call gearth.createInstance()', function() {
	    	spyOn(gearth, 'createInstance');
			view.init();
	    	expect(gearth.createInstance).toHaveBeenCalled();
	    });
	    
		it('initEarthCB should init GE plugin and geocoder', function() {
			spyOn(gearth, 'addEventListener');
			spyOn(view.wiring, 'wireUI');

			expect(view.ge).toBeNull();
			expect(view.geocoder).toBeNull();
			
			view.initEarthCB(geplugin);

			expect(view.ge).toEqual(geplugin);
			expect(view.geocoder).not.toBeNull();
			
			expect(gearth.addEventListener.callCount).toEqual(2);
			expect(gearth.addEventListener.calls[0].args[1]).toEqual('viewchangeend');
			expect(gearth.addEventListener.calls[1].args[1]).toEqual('click');

			expect(view.wiring.wireUI).toHaveBeenCalled();
		});

		it('failureEarthCB should fade out the loading div to display GE plugin message', function() {
		  var jqFadeOut = spyOn($.fn, 'fadeOut');
		  var consoleLog = spyOn(console, 'log');
		  
		  view.failureEarthCB();
		  
		  expect(jqFadeOut.mostRecentCall.object.selector).toEqual('#loadingDiv');
		  expect(console.log).toHaveBeenCalled();
		});
	});
	
	describe('Create and cancel a report', function() {
		var mockExtIdResponse = {fail: function(callback){}}
		
		beforeEach(function() {
			view = new AvyEyesView(gearth, gmaps);
			view.ge = geplugin;
			
			spyOn($, 'getJSON').andReturn(mockExtIdResponse);
		});
		
		it('Creates and initializes a new report', function() {
			spyOn(view, 'hideSearchDiv');
			
			expect(view.currentReport).toBeNull();
			view.doReport();
			expect(view.currentReport).not.toBeNull();
		});

		it('Cancels an existing report', function() {
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
		});
	});

	describe('Show and remove search results', function() {
		beforeEach(function() {
			view = new AvyEyesView(gearth, gmaps);
			view.ge = geplugin;
		});
		
		it('Shows search results KML', function() {
			spyOn(view, 'clearKmlOverlay');
			spyOn(geplugin, 'parseKml');
			spyOn(geFeaturesMock, 'appendChild');
			var kmlStr = '<kml>some data</kml>';
			
			view.overlaySearchResultKml(kmlStr);
			
			expect(view.clearKmlOverlay).toHaveBeenCalled();
			expect(geplugin.parseKml).toHaveBeenCalledWith(kmlStr);
			expect(geFeaturesMock.appendChild).toHaveBeenCalled();
		});
		
		it('Clears search results KML', function() {
			var appendChild = spyOn(geFeaturesMock, 'appendChild').andCallThrough();
			var hasChildNodes = spyOn(geFeaturesMock, 'hasChildNodes').andCallThrough();
			var getFirstChild = spyOn(geFeaturesMock, 'getFirstChild').andCallThrough();
			var removeChild = spyOn(geFeaturesMock, 'removeChild').andCallThrough();
			
			view.overlaySearchResultKml('<kml>some data</kml>');

			expect(appendChild.callCount).toEqual(1);
			
			view.clearKmlOverlay();
			
			expect(hasChildNodes.callCount).toEqual(3);
			expect(getFirstChild.callCount).toEqual(1);
			expect(removeChild.callCount).toEqual(1);
		});
	});
});