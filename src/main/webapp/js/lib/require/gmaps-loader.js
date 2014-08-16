define([], function() {
  function MapsAsyncLoader(callbackFuncName) {
    require(['http://maps.googleapis.com/maps/api/js?v=3.15&libraries=places&sensor=true&key=' 
             + GOOGLE_API_KEY + '&callback=' + callbackFuncName], function(){
    });
  }
 
  return MapsAsyncLoader;
});
