define([], function() {
  var mapsAsyncLoad = function(callbackFuncName) {
    require(['http://maps.googleapis.com/maps/api/js?v=3.15&libraries=places&sensor=true&key=' 
             + GOOGLE_API_KEY + '&callback=' + callbackFuncName], function(){
    });
  }
 
  return mapsAsyncLoad;
});
