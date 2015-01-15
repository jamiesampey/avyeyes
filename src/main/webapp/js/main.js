'use strict';

require.config({
  baseUrl: '/js'
});
    
var avyeyes = null;

function gmapsLoadCB() {
    avyeyes.init(google.maps);
}

//Start the main app logic.
requirejs(['avyeyes-view', 'lib/Cesium/Cesium', 'lib/gmaps-loader',
  'lib/analytics', 'lib/facebook', '//platform.twitter.com/widgets.js'],
	function (AvyEyesView, amdCesium, gmapsAsyncLoad) {
      avyeyes = new AvyEyesView(amdCesium);
      gmapsAsyncLoad('gmapsLoadCB');
    }
);
