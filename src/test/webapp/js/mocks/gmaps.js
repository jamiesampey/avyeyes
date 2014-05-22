define([], function() {
	function GmapsMock() {
		this.Geocoder = function() {
			this.geocode = function(args, callback) {
				// no op
			}
		}
	}
	return GmapsMock;
});