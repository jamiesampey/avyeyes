define(['http://www.google.com/jsapi?key=' + GOOGLE_API_KEY], function() {
  function EarthAsyncLoader(callbackFunc) {
	var options = {
  	  'sensor': true,
	  'callback': callbackFunc
	};  
    google.load('earth', 1, options);
  }
 
  return EarthAsyncLoader;
});
