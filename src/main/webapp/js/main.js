require.config({
	baseUrl: '/js',
    paths: {
    	'earthAsyncLoad': 'lib/require/gearth-loader',
    	'mapsAsyncLoad': 'lib/require/gmaps-loader',
        'jquery-ui': 'lib/jquery-ui',
        'jquery-geocomplete': 'lib/jquery.geocomplete.min',
        'jquery-fileupload': 'lib/jquery.fileupload',
        'jquery-iframe-transport': 'lib/jquery.iframe-transport',
        'geojs': 'lib/geo.min',
        'lightbox': 'lib/lightbox.min'
    }
});

var avyeyes = null;
var avyeyesInitGate = $.Deferred();
var earthLoaded = false;
var mapsLoaded = false;

var GOOGLE_API_KEY = 'AIzaSyAHuPQo0kaoI-rEGr0q57EkOF2UPNpFP28';

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
requirejs(['earthAsyncLoad', 'mapsAsyncLoad', 'ae-view'], 
		function (earthAsyncLoad, mapsAsyncLoad, AvyEyesView) {
	earthAsyncLoad(earthLoadCB);
	mapsAsyncLoad("mapsLoadCB");
	
	avyeyesInitGate.done(function() {
	  avyeyes = new AvyEyesView(google.earth, google.maps);
	  avyeyes.init();
	})
});