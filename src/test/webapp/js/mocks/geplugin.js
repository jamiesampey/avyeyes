define([], function() {
	function GearthPluginMock() {
		var window = new GEWindow();
		var options = new GEOptions();
		var navControl = new GENavControl();
		var layerRoot = new GELayerRoot();
		var view = new GEView();
		var features = new GEFeatures();
		var globe = new GEGlobe();
		
		this.ALTITUDE_CLAMP_TO_GROUND = 'altClampToGround';
		this.COLOR_NORMAL = 'normalCOlorMode';
		
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
		
		this.getGlobe = function() {
			return globe;
		}
		
		this.createLookAt = function(name) {
			return new GELookAt(name);
		}
		
		this.createPlacemark = function(name) {
			return new GEPlacemark(name);
		}
		
		this.createLineString = function(name) {
			return new GELineString(name);
		}
		
		this.createStyle = function(name) {
			return new GEStyleSelector(name);
		}
		
		this.createDocument = function(name) {
			return new GEDocument(name);
		}
		
		this.createGECoords = function() {
			return new GECoords();
		}
		
		this.createCoord = function() {
			return new GECoord('', '', '');
		}
		
		this.createPolygon = function(name) {
			return new GEPolygon(name);
		}
		
		this.createLinearRing = function(name) {
			return new GELinearRing(name);
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
	
	function GEDocument(name) {
		this.name = name;
		this.features = new GEFeatures();
		this.getFeatures = function() {return this.features;}
	}
	
	function GEFeatures() {
		this.appendChild = function(child){}
		this.removeChild = function(child){}
	}
	
	function GEGlobe() {}
	
	function GELookAt(name) {
		this.name = name;
		this.lat;
		this.lng;
		this.range;
		this.altMode;
		this.tilt;
		this.heading;
		this.setLatitude = function(lat) {this.lat = lat;}
		this.setLongitude = function(lng) {this.lng = lng;}
		this.setRange = function(range) {this.range = range;}
	    this.setAltitudeMode = function(altMode) {this.altMode = altMode;}
	    this.setTilt = function(tilt) {this.tilt = tilt;}
	    this.setHeading = function(heading) {this.heading = heading;} 
	}
	
	function GEPlacemark(name) {
		this.name = name;
		this.geometry;
		this.styleSelector;
		this.setGeometry = function(obj) { this.geometry = obj;}
		this.getGeometry = function() {return this.geometry;}
		this.setStyleSelector = function(obj) {this.styleSelector = obj;}
		this.getStyleSelector = function() {return this.styleSelector;}
	}
	
	function GEStyleSelector(name) {
		this.name = name;
		this.lineStyle = new GEStyle();
		this.polyStyle = new GEStyle();
		this.getLineStyle = function() {}
		this.getLineStyle = function() {return this.lineStyle;}
		this.getPolyStyle = function() {return this.polyStyle;}
	}
	
	function GEStyle() {
		this.width;
		this.gecolor = new GEColor();
		this.colorMode;
	    this.setWidth = function(num) {this.width = num;}
	    this.getColor = function() {return this.gecolor;}
	    this.setColorMode = function(id) {this.colorMode = id;}
	}
	
	function GEColor() {
		this.color;
		this.set = function(c){this.color = c;}
	}
	
	function GELineString(name) {
		this.name = name;
		this.coords = new GECoords();
		this.tessellate;
		this.altitudeMode;
		this.setTessellate = function(bool) {this.tessellate = bool;}
		this.setAltitudeMode = function(id) {this.altitudeMode = id;}
		this.getCoordinates = function() { return this.coords;}
		
	}
	
	function GECoords() {
		this.coords = new Array();
		this.pushLatLngAlt = function(lat, lng, alt) {
			this.coords.push(new GECoord(lat, lng, alt));
		}
		this.get = function(idx) {
			return this.coords[idx];
		}
		this.getLength = function() {
			return this.coords.length;
		}
	}
	
	function GECoord(lat, lng, alt) {
		this.lat = lat;
		this.lng = lng;
		this.alt = alt;
		this.getLatitude = function() {return this.lat;}
		this.getLongitude = function() {return this.lng;}
		this.setAltitude = function(val) {this.alt = val;}
		this.getAltitude = function() {return this.alt;}
	}
	
	function GEPolygon(name) { 
		this.name = name;
		this.outerBoundary;
		this.setOuterBoundary = function(obj) {
			this.outerBoundary = obj;
		}
	}
	
	function GELinearRing(name) {
		this.name = name;
		this.coords = new GECoords();
		this.getCoordinates = function() { return this.coords;}
	}
	
	return GearthPluginMock;
});