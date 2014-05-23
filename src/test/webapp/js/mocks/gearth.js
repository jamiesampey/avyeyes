define([], function() {
	function GearthMock() {
		this.createInstance = function(div, successCallback, failureCallback, options) {
			// no op
		}
		this.addEventListener = function(geView, event, callback) {
			// no op
		}
	}
	return GearthMock;
});
