var ge;   
var geocoder;
google.load('earth', '1', {other_params: 'sensor=true'});
google.load('maps', '3.14', {other_params: 'sensor=true'});
google.setOnLoadCallback(initGE);
	    
function initCB(instance) {
	ge = instance;
    ge.getWindow().setVisibility(true);
    ge.getOptions().setStatusBarVisibility(true);
    ge.getOptions().setUnitsFeetMiles(true);
    ge.getOptions().setFlyToSpeed(0.4);
    ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);
    ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, true);
    ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);
    google.earth.addEventListener(this.ge.getView(), 'viewchangeend', viewChangeEndTimeout);
    
    geocoder = new google.maps.Geocoder();
    geInitCallback();
}
	    
function failureCB(errorCode) {
    avyEyesView.showModalDialog("Error", "failureCB: " + errorCode);
}
	    
function initGE() {
  google.earth.createInstance('map3d', initCB, failureCB, { 'language': 'en' });
}

function viewChangeEndTimeout() {
	var viewChangeEndTimer;
	if(viewChangeEndTimer){
	    clearTimeout(viewChangeEndTimer);
	  }
	viewChangeEndTimer = setTimeout(avyEyesView.viewChangeEnd(), 200);
}