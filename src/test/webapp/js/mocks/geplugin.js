define([], function() {
	function GearthPluginMock() {
		var window = new GEWindow();
		var options = new GEOptions();
		var navControl = new GENavControl();
		var layerRoot = new GELayerRoot();
		var view = new GEView();
		var features = new GEFeatures();
		
		this.getWindow = function() {
			return window;
		}
		
		this.getOptions = function() {
			return options;
		}
		
		this.getNavigationControl = function() {
			return navControl;
		}
		
		this.getLayerRoot = function() {
			return layerRoot;
		}
		
		this.getView = function() {
			return view;
		}
		
		this.getFeatures = function() {
			return features;
		}
		
		this.createLookAt = function(name) {
			return new GELookAt(name);
		}
		
		this.parseKml = function(kml) {
			return {};
		}
	}
	
	function GEWindow() {
		this.setVisibility = function(bool){}
	}
	
	function GEOptions() {
		this.setStatusBarVisibility = function(bool){}
		this.setUnitsFeetMiles = function(bool){}
		this.setFlyToSpeed = function(val){}		
	}
	
	function GENavControl() {
		this.setVisibility = function(bool){}	
	}
	
	function GELayerRoot() {
		this.enableLayerById = function(id, bool){}	
	}
	
	function GEView() {
		this.getViewportGlobeBounds = function(){}
		this.copyAsCamera = function(id){}
		this.setAbstractView = function(lookAt){}
	}
	
	function GEFeatures() {
		this.appendChild = function(){}
		this.removeChild = function(){}
	}
	
	function GELookAt(name) {
		this.name = name;
		this.lat;
		this.lng;
		this.range;
		this.altMode;
		this.tilt;
		
		this.setLatitude = function(lat) {this.lat = lat;}
		this.setLongitude = function(lng) {this.lng = lng;}
		this.setRange = function(range) {this.range = range;}
	    this.setAltitudeMode = function(altMode) {this.altMode = altMode;}
	    this.setTilt = function(tilt) {this.tilt = tilt;} 
	}
	
	return GearthPluginMock;
});