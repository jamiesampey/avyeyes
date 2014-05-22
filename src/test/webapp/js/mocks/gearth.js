define([], function() {
	function GearthMock() {
		this.createInstance = function(div, successCallback, failureCallback, options) {
			// no op
		}
	}
	return GearthMock;
});
