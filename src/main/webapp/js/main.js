require.config({
	baseUrl: 'js',
    paths: {
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
requirejs(['ae-view', 'goog!earth,1,other_params:sensor=true'], function (AvyEyesView) {
	view = new AvyEyesView();
	google.earth.createInstance('map3d', initCB, failureCB, { 'language': 'en' });
	
	function initCB(instance) {
		var ge = instance;
	    ge.getWindow().setVisibility(true);
	    ge.getOptions().setStatusBarVisibility(true);
	    ge.getOptions().setUnitsFeetMiles(true);
	    ge.getOptions().setFlyToSpeed(0.4);
	    ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);
	    ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, true);
	    ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);
	    google.earth.addEventListener(ge.getView(), 'viewchangeend', viewChangeEndTimeout);
	    
	    view.init(ge);
	}
		    
	function failureCB(errorCode) {
	    view.showModalDialog("Error", "failureCB: " + errorCode);
	}

	function viewChangeEndTimeout() {
		var viewChangeEndTimer;
		if(viewChangeEndTimer){
		    clearTimeout(viewChangeEndTimer);
		  }
		viewChangeEndTimer = setTimeout(view.viewChangeEnd(), 200);
	}
});