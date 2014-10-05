define(['http://www.google.com/jsapi'], function() {
  var earthAsyncLoad = function(callbackFunc) {
	var options = {
  	'sensor': true,
	  'callback': callbackFunc
	};  
    google.load('earth', 1, options);
  }
 
  return earthAsyncLoad;
});
