'use strict';

require.config({
  baseUrl: '/js',
  paths: {
    'earthAsyncLoad': 'lib/gearth-loader',
    'mapsAsyncLoad': 'lib/gmaps-loader',
    'analytics': 'lib/analytics',
    'facebook': 'lib/facebook',
    'twitter': '//platform.twitter.com/widgets',
    'jquery-ui': 'lib/jquery-ui',
    'jquery-geocomplete': 'lib/jquery.geocomplete.min',
    'jquery-fileupload': 'lib/jquery.fileupload',
    'jquery-iframe-transport': 'lib/jquery.iframe-transport',
    'jquery-datatables': 'lib/jquery.datatables.min',
    'geojs': 'lib/geo.min',
    'lightbox': 'lib/lightbox.min'
  }
});
    
var avyeyes = null;
var avyeyesInitGate = $.Deferred();
var earthLoaded = false;
var mapsLoaded = false;

var GA_TRACKING_CODE = 'UA-45548947-3';
var GOOGLE_API_KEY = 'AIzaSyAHuPQo0kaoI-rEGr0q57EkOF2UPNpFP28';
var FACEBOOK_APP_ID = '541063359326610';

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
requirejs(['avyeyes-view', 'earthAsyncLoad', 'mapsAsyncLoad', 'analytics', 'facebook', 'twitter'],
	function (AvyEyesView, earthAsyncLoad, mapsAsyncLoad) {
    earthAsyncLoad(earthLoadCB);
    mapsAsyncLoad("mapsLoadCB");
    
    avyeyesInitGate.done(function() {
      avyeyes = new AvyEyesView(google.earth, google.maps);
  	  avyeyes.init();
    })
});
