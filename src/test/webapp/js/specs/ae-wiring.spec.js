define(['gearth', 
        'gmaps',
        'ae-view',
        'ae-wiring'], function(gearthMock, gmapsMock, AvyEyesView, wireUI) {

	var gearth = new gearthMock();
	var gmaps = new gmapsMock();
	var view = new AvyEyesView(gearth, gmaps); 
	
	describe('AvyEyes view UI wiring', function() {
		it('should wire jQuery UI fields', function() {
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
			var jqCss = spyOn($.fn, 'css');
			var jqSubmit = spyOn($.fn, 'submit');
			
			wireUI(view);
			
			expect(jquiAutocomplete.callCount).toEqual(2);
	    expect(jquiAutocomplete.calls[0].object.selector).toEqual('.avyAutoComplete');
			expect(jquiAutocomplete.calls[1].object.selector).toEqual('#avyReportDialog .avyAutoComplete');
			
			expect(jquiMenu.mostRecentCall.object.selector).toEqual('#aeMenu');
			expect(jquiDatepicker.mostRecentCall.object.selector).toEqual('.avyDate');
			expect(jquiSlider.mostRecentCall.object.selector).toEqual('.avyRDSlider');
			expect(jquiButton.mostRecentCall.object.selector).toEqual('.avyButton');
			expect(jqGeocomplete.mostRecentCall.object.selector).toEqual('.avyLocation');
			
			expect(jquiSpinner.callCount).toEqual(2);
			expect(jquiSpinner.calls[0].object.selector).toEqual('.avyHumanNumber');
			expect(jquiSpinner.calls[1].object.selector).toEqual('.avySlopeAngle');

			expect(jquiDialog.callCount).toEqual(8);
			expect(jquiDialog.calls[0].object.selector).toEqual('#multiDialog');
			expect(jquiDialog.calls[1].object.selector).toEqual('#avyDetailDialog');
			expect(jquiDialog.calls[2].object.selector).toEqual('#helpDialog');
			expect(jquiDialog.calls[3].object.selector).toEqual('#avyReportGeocodeDialog');
			expect(jquiDialog.calls[4].object.selector).toEqual('#avyReportBeginDrawDialog');
			expect(jquiDialog.calls[5].object.selector).toEqual('#avyReportConfirmDrawDialog');
			expect(jquiDialog.calls[6].object.selector).toEqual('#avyReportImgDialog');
			expect(jquiDialog.calls[7].object.selector).toEqual('#avyReportDialog');
			
			expect(jqCss.mostRecentCall.object.selector).toEqual("#aeControlContainer");
			expect(jqSubmit.mostRecentCall.object.selector).toEqual("#avyInitLiftCallback");
		});
	});
	
});