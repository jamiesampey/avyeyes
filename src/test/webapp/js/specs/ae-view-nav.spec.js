define(['jquery', 'jasmine-jquery', 'gearth', 'gmaps', 'geplugin', 'ae-view'], 
		function($, jas$, gearthMock, gmapsMock, gePluginMock, AvyEyesView) {
	
	var gearth = new gearthMock();
	var geplugin = new gePluginMock();
	var gmaps = new gmapsMock();
	
	describe('GE viewchangeend event handling', function() {
		var view;
		var geViewMock;
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
			view.setGEPlugin(geplugin);
			geViewMock = geplugin.getView();
	
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
		
		it('shows search menu on initial page load', function() {
			spyOn(view, 'showSearchMenu');
			view.viewChangeEnd();
			expect(view.showSearchMenu).toHaveBeenCalled();
		});
		
		it('does not show search menu if not initial page load', function() {
			spyOn(view, 'showSearchMenu');
			view.aeFirstImpression = false;
			view.viewChangeEnd();
			expect(view.showSearchMenu).not.toHaveBeenCalled();
		});
		
		it('begins report if initial geocode for report', function() {
			spyOn(window, 'setTimeout');

			view.currentReport = new reportMock();
			view.initialGeocodeForReport = true;
			view.viewChangeEnd();

			expect(window.setTimeout).toHaveBeenCalledWith(view.currentReport.beginReport, 2000);
			expect(view.initialGeocodeForReport).toEqual(false);
		});
		
		it('does not begin report if not initial geocode for report', function() {
			spyOn(window, 'setTimeout');
			
			view.currentReport = new reportMock();
			view.initialGeocodeForReport = false;
			view.viewChangeEnd();
			
			expect(window.setTimeout).not.toHaveBeenCalled();
		});
	});
});