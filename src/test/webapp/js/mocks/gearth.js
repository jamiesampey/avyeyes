define([], function() {
	function GearthMock() {
		this.createInstance = function(div, successCallback, failureCallback, options) {}
		this.addEventListener = function(geView, event, callback) {}
	}
	return GearthMock;
});
