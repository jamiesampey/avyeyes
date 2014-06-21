define([], function() {
	var GeoJS = {
		_heading: {},
		_distance: {},
		_midpointCoord: {},
		Point: function(obj) {
			this.gecoord = obj;
			this.lat = function() {return this.gecoord.getLatitude();}
			this.lng = function() {return this.gecoord.getLongitude();}
			this.altitude = function() {return this.gecoord.getAltitude();}
			this.heading = function(pt) {return GeoJS._heading;}
			this.distance = function(pt) {return GeoJS._distance;}
			this.midpoint = function(pt) {return new GeoJS.Point(GeoJS._midpointCoord);}
		}
	}
	window.geo = GeoJS;
});
