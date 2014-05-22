require.config({
	baseUrl: 'js',
    paths: {
    	'gearth': 'lib/require/gearth-loader',
    	'gmaps': 'lib/require/gmaps-loader',
        'jquery-ui': 'lib/jquery-ui.custom.min',
        'jquery-geocomplete': 'lib/jquery.geocomplete.min',
        'geojs': 'lib/geo.min',
    	'async': 'lib/require/async',
        'goog': 'lib/require/goog',
        'propertyParser': 'lib/require/propertyParser'
    }
});

var view = null;
var GOOGLE_API_KEY = 'AIzaSyAHuPQo0kaoI-rEGr0q57EkOF2UPNpFP28';

//Start the main app logic.
requirejs(['gearth', 'gmaps', 'ae-view'], function (gearth, gmaps, AvyEyesView) {
	view = new AvyEyesView(gearth, gmaps);
});