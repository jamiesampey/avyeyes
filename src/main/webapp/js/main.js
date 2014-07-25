require.config({
	baseUrl: '/js',
    paths: {
    	'gearth': 'lib/require/gearth-loader',
    	'gmaps': 'lib/require/gmaps-loader',
        'jquery-ui': 'lib/jquery-ui',
        'jquery-geocomplete': 'lib/jquery.geocomplete.min',
        'jquery-fileupload': 'lib/jquery.fileupload',
        'jquery-iframe-transport': 'lib/jquery.iframe-transport',
        'geojs': 'lib/geo.min',
        'lightbox': 'lib/lightbox.min',
    	'async': 'lib/require/async',
        'goog': 'lib/require/goog',
        'propertyParser': 'lib/require/propertyParser'
    }
});

var view = null;
var GOOGLE_API_KEY = 'AIzaSyAHuPQo0kaoI-rEGr0q57EkOF2UPNpFP28';

//Start the main app logic.
requirejs(['gearth', 'gmaps', 'ae-view'], function (gearth, gmaps, AvyEyesView) {
	setTimeout(function() {view = new AvyEyesView(gearth, gmaps);}, 500);
});