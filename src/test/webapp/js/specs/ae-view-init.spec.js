define(['gearth', 
        'gmaps', 
        'geplugin', 
        'ae-view'], 
        function(gearthMock, gmapsMock, gePluginMock, AvyEyesView) {
	
	var gearth = new gearthMock();
	var geplugin = new gePluginMock();
	var gmaps = new gmapsMock();
	
	var view; 

	describe('AvyEyesView instantiation', function() {
		it('should set state booleans to correct initial values', function() {
			view = new AvyEyesView(gearth, gmaps);
	    	expect(view.aeFirstImpression).toEqual(true);
	    	expect(view.initialGeocodeForReport).toEqual(false);
	    });
	    
	    it('should have called gearth.createInstance()', function() {
	    	spyOn(gearth, 'createInstance');
			new AvyEyesView(gearth, gmaps);
	    	expect(gearth.createInstance).toHaveBeenCalled();
	    });
	});

	describe('AvyEyesView initialization', function() {
		beforeEach(function() {
			view = new AvyEyesView(gearth, gmaps);
		});
		
		it('should init GE plugin and geocoder', function() {
			spyOn(gearth, 'addEventListener');
			spyOn(view, 'setGE').andCallThrough();
			spyOn(view, 'setGeocoder');
			spyOn(view, 'init');
			var jqFadeOut = spyOn($.fn, 'fadeOut');

			view.initCB(geplugin);

			expect(view.setGE).toHaveBeenCalledWith(geplugin);
			expect(view.setGeocoder).toHaveBeenCalled();
			expect(gearth.addEventListener).toHaveBeenCalledWith(
					geplugin.getView(), 'viewchangeend', view.viewChangeEndTimeout);
			expect(gearth.addEventListener).toHaveBeenCalledWith(
					geplugin.getGlobe(), 'click', view.handleMapClick);

			expect(jqFadeOut.mostRecentCall.object.selector).toEqual('#loadingDiv');
			expect(view.init).toHaveBeenCalled();
		});
		
		it('should init AE view', function() {
			var menuMock = function(){
				this.position = function(options){}
			}
			
			var jquiAutocomplete = spyOn($.fn, 'autocomplete');
			var jquiMenu = spyOn($.fn, 'menu').andReturn(new menuMock);
			var jquiDatepicker = spyOn($.fn, 'datepicker');
			var jquiSlider = spyOn($.fn, 'slider');
			var jquiSpinner = spyOn($.fn, 'spinner').andCallThrough(); 
			var jquiButton = spyOn($.fn, 'button');
			var jquiDialog = spyOn($.fn, 'dialog');
			var jqGeocomplete = spyOn($.fn, 'geocomplete');
			var jqSubmit = spyOn($.fn, 'submit');
			
			view.init();
			
			expect(jquiAutocomplete.mostRecentCall.object.selector).toEqual('.avyAutoComplete');
			expect(jquiMenu.mostRecentCall.object.selector).toEqual('#aeMenu');
			expect(jquiDatepicker.mostRecentCall.object.selector).toEqual('.avyDate');
			expect(jquiSlider.mostRecentCall.object.selector).toEqual('.avyRDSlider');
			expect(jquiButton.mostRecentCall.object.selector).toEqual('.avyButton');
			expect(jqGeocomplete.mostRecentCall.object.selector).toEqual('.avyLocation');
			
			expect(jquiSpinner.callCount).toEqual(2);
			expect(jquiSpinner.calls[0].object.selector).toEqual('.avyHumanNumber');
			expect(jquiSpinner.calls[1].object.selector).toEqual('.avySlopeAngle');

			expect(jquiDialog.callCount).toEqual(2);
			expect(jquiDialog.calls[0].object.selector).toEqual('#multiDialog');
			expect(jquiDialog.calls[1].object.selector).toEqual('#avyDetailDialog');

			expect(jqSubmit.mostRecentCall.object.selector).toEqual("#avyInitLiftCallback");
		});
	});
});