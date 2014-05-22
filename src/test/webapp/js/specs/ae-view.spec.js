define(['jquery', 'gearth', 'gmaps', 'ae-view'], function($, gearthMock, gmapsMock, AvyEyesView) {
	var gearth = new gearthMock();
	var gmaps = new gmapsMock();
	
	describe('AvyEyesView instantiation', function() {
		var view = new AvyEyesView(gearth, gmaps);
		
		it('should have firstImpression set to true', function() {
	    	expect(view.aeFirstImpression).toEqual(true);
	    });
	    
	    it('should have initialGeocodeForReport set to false', function() {
	    	expect(view.initialGeocodeForReport).toEqual(false);
	    });
	    
	    it('should have constructed a geocoder', function() {
	    	spyOn(gmaps, 'Geocoder');
			new AvyEyesView(gearth, gmaps);
	    	expect(gmaps.Geocoder).toHaveBeenCalled();
	    });
	    
	    it('should have called gearth.createInstance()', function() {
	    	spyOn(gearth, 'createInstance');
			new AvyEyesView(gearth, gmaps);
	    	expect(gearth.createInstance).toHaveBeenCalled();
	    });
	});

});