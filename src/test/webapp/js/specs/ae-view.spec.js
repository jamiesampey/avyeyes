define(['ae-view'], function(AvyEyesView) {

	describe('AvyEyesView initialization', function() {
		var view = new AvyEyesView();
		
		it('should have first impression set to true', function() {
	    	expect(view.aeFirstImpression).toEqual(true);
	    });
	    
	    it('should have initial geocode set to false', function() {
	    	expect(view.initialGeocodeForReport).toEqual(false);
	    });
	});

});