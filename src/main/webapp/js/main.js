'use strict';

require.config({
  baseUrl: '/js'
});
    
var avyeyes = null;
var avyeyesInitGate = $.Deferred();
var earthLoaded = false;
var mapsLoaded = false;

function earthLoadCB() {
  earthLoaded = true;
  if (mapsLoaded) {
    avyeyesInitGate.resolve();
  }
}
    
function mapsLoadCB() {
  mapsLoaded = true;
  if (earthLoaded) {
    avyeyesInitGate.resolve();
  }
}
    
//Start the main app logic.
requirejs(['avyeyes-view', 'lib/gearth-loader', 'lib/gmaps-loader',
  'lib/analytics', 'lib/facebook', '//platform.twitter.com/widgets.js'],
	function (AvyEyesView, earthAsyncLoad, mapsAsyncLoad) {
    earthAsyncLoad(earthLoadCB);
    mapsAsyncLoad("mapsLoadCB");
    
    avyeyesInitGate.done(function() {
      avyeyes = new AvyEyesView(google.earth, google.maps);
  	  avyeyes.init();
    })
});
