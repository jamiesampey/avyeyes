define(['jasmine-jquery', 
        'gearth', 
        'gmaps', 
        'geplugin', 
        'avyeyes-view'],
        function(jas$, gearthMock, gmapsMock, gePluginMock, AvyEyesView) {
	
	var gearth = new gearthMock();
	var geplugin = new gePluginMock();
	var gmaps = new gmapsMock();
	
	var view;
	var geViewMock = geplugin.getView();
	
	describe('GE viewchangeend event handling', function() {
		var reportMock;
		
		var viewBoundNorth = 'nb';
		var viewBoundEast = 'eb';
		var viewBoundSouth = 'sb';
		var viewBoundWest = 'wb';
		var cameraAlt = 'camAlt';
		var cameraTilt = 'camTilt';
		var cameraLat = 'camLat';
		var cameraLng = 'camLng';
		
		beforeEach(function() {
			view = new AvyEyesView(gearth, gmaps);
			view.ge = geplugin;
	
			reportMock = function(){
				this.beginReport = function(){}
			}
			
			var viewBoundsBoxMock = function(){
				this.getNorth = function() {return viewBoundNorth;}
				this.getEast = function() {return viewBoundEast;}
				this.getSouth = function() {return viewBoundSouth;}
				this.getWest = function() {return viewBoundWest;}
			}
			
			var cameraMock = function(){
				this.getAltitude = function(){return cameraAlt;}
				this.getTilt = function(){return cameraTilt;}
				this.getLatitude = function(){return cameraLat;}
				this.getLongitude = function(){return cameraLng;}
			}
			
			spyOn(geplugin, 'getView').andReturn(geViewMock);
			spyOn(geViewMock, 'getViewportGlobeBounds').andReturn(new viewBoundsBoxMock());
			spyOn(geViewMock, 'copyAsCamera').andReturn(new cameraMock());
		});
		
		it('waits for a delay timer before executing', function() {
			var viewChangeEndStub = function(){}
			spyOn(view, 'viewChangeEnd').andReturn(viewChangeEndStub);
			spyOn(window, 'setTimeout');
			view.viewChangeEndTimeout();
			expect(window.setTimeout).toHaveBeenCalledWith(viewChangeEndStub, 200);
		});
		
		it('populates viewport and camera location hidden inputs', function() {	
			setFixtures('<div>'
					+ '<input id="avySearchNorthLimit" type="hidden"/>'
	                + '<input id="avySearchEastLimit" type="hidden"/>'
	                + '<input id="avySearchSouthLimit" type="hidden"/>'
	                + '<input id="avySearchWestLimit" type="hidden"/>'
	                + '<input id="avySearchCameraAlt" type="hidden"/>'
	                + '<input id="avySearchCameraTilt" type="hidden"/>'
	                + '<input id="avySearchCameraLat" type="hidden"/>'
	                + '<input id="avySearchCameraLng" type="hidden"/>'
	             + '</div>');
			
			view.viewChangeEnd();
			
			expect(geViewMock.getViewportGlobeBounds).toHaveBeenCalled();
			expect($("#avySearchNorthLimit")).toHaveValue(viewBoundNorth);
			expect($("#avySearchEastLimit")).toHaveValue(viewBoundEast);
			expect($("#avySearchSouthLimit")).toHaveValue(viewBoundSouth);
			expect($("#avySearchWestLimit")).toHaveValue(viewBoundWest);
			
			expect(geViewMock.copyAsCamera).toHaveBeenCalled();
			expect($("#avySearchCameraAlt")).toHaveValue(cameraAlt);
			expect($("#avySearchCameraTilt")).toHaveValue(cameraTilt);
			expect($("#avySearchCameraLat")).toHaveValue(cameraLat);
			expect($("#avySearchCameraLng")).toHaveValue(cameraLng);
		});
	});
	
	describe('Geocode and fly to functionality', function() {
		beforeEach(function() {
			view = new AvyEyesView(gearth, gmaps);
			view.ge = geplugin;
			spyOn(geplugin, 'getView').andReturn(geViewMock);
		});
		
		it('sets the GE View to a new LookAt', function() {
			spyOn(geViewMock, 'setAbstractView');
			
			var lat = '25.2542342';
			var lng = '96.324552';
			var range = '20000';
			var tilt = '35.2';
			var heading = '25';
			
			view.flyTo(lat, lng, range, tilt, heading);
			
			expect(geViewMock.setAbstractView.mostRecentCall.args[0].lat).toEqual(lat);
			expect(geViewMock.setAbstractView.mostRecentCall.args[0].lng).toEqual(lng);
			expect(geViewMock.setAbstractView.mostRecentCall.args[0].range).toEqual(range);
			expect(geViewMock.setAbstractView.mostRecentCall.args[0].tilt).toEqual(tilt);
			expect(geViewMock.setAbstractView.mostRecentCall.args[0].heading).toEqual(heading);
		});
		
		it('geocodes address', function() {
			var geocoderMock = new gmaps.Geocoder();
			spyOn(geocoderMock, 'geocode');
			view.geocoder = geocoderMock;
			
			var address = 'aspen, co';
			view.geocodeAndFlyTo(address, 13000, 65.5);
			expect(geocoderMock.geocode.mostRecentCall.args[0].address).toEqual(address);
		});
	});
});