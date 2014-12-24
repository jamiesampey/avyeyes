'use strict';

require.config({
  baseUrl: '/js',
  paths: {
    'gmapsAsyncLoad': 'lib/gmaps-loader',
    'analytics': 'lib/analytics',
    'facebook': 'lib/facebook',
    'twitter': '//platform.twitter.com/widgets',
    'jquery-ui': 'lib/jquery-ui',
    'jquery-geocomplete': 'lib/jquery.geocomplete.min',
    'jquery-fileupload': 'lib/jquery.fileupload',
    'jquery-iframe-transport': 'lib/jquery.iframe-transport',
    'geojs': 'lib/geo.min',
    'lightbox': 'lib/lightbox.min'
  }
});
    
var avyeyes = null;

var GA_TRACKING_CODE = 'UA-45548947-3';
var GOOGLE_API_KEY = 'AIzaSyAHuPQo0kaoI-rEGr0q57EkOF2UPNpFP28';
var BING_API_KEY = 'AiXcgClqr_8DxjhvM5bal45QdMumBNOllccwdibv5ViVRKR1xTh9iA5GugmmINPr';
var FACEBOOK_APP_ID = '541063359326610';

function gmapsLoadCB() {
    avyeyes.init(google.maps);
}

//Start the main app logic.
requirejs(['avyeyes-view', 'lib/Cesium/Cesium', 'gmapsAsyncLoad', 'analytics', 'facebook', 'twitter'],
	function (AvyEyesView, amdCesium, gmapsAsyncLoad) {
      avyeyes = new AvyEyesView(amdCesium);
      gmapsAsyncLoad('gmapsLoadCB');
    }
);
