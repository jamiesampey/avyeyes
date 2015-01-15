define([], function() {
  var mapsAsyncLoad = function(callbackFuncName) {
    require(['//maps.googleapis.com/maps/api/js?v=3&libraries=places&sensor=true&key='
             + 'AIzaSyAHuPQo0kaoI-rEGr0q57EkOF2UPNpFP28&callback=' + callbackFuncName],
             function(){});
  }
 
  return mapsAsyncLoad;
});
