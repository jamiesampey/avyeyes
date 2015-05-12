'use strict';

require.config({
  baseUrl: '/js'
});
    
var avyeyes = null;

function gmapsLoadCB() {
    avyeyes.init(google.maps);
}

//Start the main app logic.
requirejs(['avyeyes-view', 'lib/gmaps-loader',
  'lib/analytics', 'lib/facebook', '//platform.twitter.com/widgets.js'],
	function (AvyEyesView, gmapsAsyncLoad) {
      avyeyes = new AvyEyesView();
      gmapsAsyncLoad('gmapsLoadCB');
    }
);
