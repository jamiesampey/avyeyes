define(['ae-wiring',
        'ae-report',
        'jquery-ui', 
        'jquery-geocomplete', 
        'jquery-fileupload', 
        'jquery-iframe-transport',
        'lightbox'
        ], function(AvyEyesWiring, AvyReport) {

function AvyEyesView(gearthInst, gmapsInst) {
  this.wiring = new AvyEyesWiring(this);
  
	this.gearth = gearthInst;
	this.gmaps = gmapsInst;
	this.ge = null;
	this.geocoder = null;   
	
	this.currentReport = null;
	this.aeFirstImpression = true;
	this.navControlBlinkInterval = null;
}

AvyEyesView.prototype.showSearchDiv = function() {
	if (!$('#aeSearchControlContainer').is(':visible')) {
		$('#aeSearchControlContainer').slideDown("slow");
	}
}

AvyEyesView.prototype.hideSearchDiv = function() {
	if ($('#aeSearchControlContainer').is(':visible')) {
		$('#aeSearchControlContainer').slideUp("slow");
	}
}

AvyEyesView.prototype.resetView = function() {
	this.clearSearchFields();
	this.clearKmlOverlay();
	this.cancelReport();
	this.showSearchDiv();
}

AvyEyesView.prototype.clearSearchFields = function() {
	$('#aeSearchControlContainer').find('input:text').val('');
	$('#aeSearchControlContainer').find('.avyRDSliderValue').val('0');
	$('#aeSearchControlContainer').find('.avyRDSlider').slider('value', 0);
}

AvyEyesView.prototype.showModalDialog = function(title, msg) {
	$.ui.dialog.prototype._focusTabbable = function(){};
	$('#multiDialog').html(msg);
	$('#multiDialog').dialog('option', 'title', title);
	$('#multiDialog').dialog('open');
}

AvyEyesView.prototype.doReport = function() {
	this.cancelReport();
	this.currentReport = new AvyReport(this);
	this.currentReport.beginReportWizard();
}

AvyEyesView.prototype.cancelReport = function() {
	if (this.currentReport) {
		this.currentReport.clearAllFields();
		this.currentReport.clearAvyDrawing();
		this.currentReport = null;
	}
	this.stopNavControlBlink();
}

AvyEyesView.prototype.viewChangeEnd = function() {
	var viewBoundsBox = this.ge.getView().getViewportGlobeBounds();
	$("#avySearchNorthLimit").val(viewBoundsBox.getNorth());
	$("#avySearchEastLimit").val(viewBoundsBox.getEast());
	$("#avySearchSouthLimit").val(viewBoundsBox.getSouth());
	$("#avySearchWestLimit").val(viewBoundsBox.getWest());
	
	var camera = this.ge.getView().copyAsCamera(this.ge.ALTITUDE_RELATIVE_TO_GROUND);
	$("#avySearchCameraAlt").val(camera.getAltitude());
	$("#avySearchCameraTilt").val(camera.getTilt());
	$("#avySearchCameraLat").val(camera.getLatitude());
	$("#avySearchCameraLng").val(camera.getLongitude());
	
	if (this.aeFirstImpression) {
		this.showSearchDiv();
		this.aeFirstImpression = false;
	}
}

AvyEyesView.prototype.handleMapClick = function(event) {
	if ($('#avyDetailDialog').is(':visible')) {
		this.hideAvyDetails();
	}

    var placemark = event.getTarget();
    if (placemark.getType() != 'KmlPlacemark') {
    	return;
    }
	event.preventDefault();
	
	var kmlDoc = $.parseXML(placemark.getKml());
	var extId = $(kmlDoc).find('Placemark').attr('id');
	
	$.getJSON('/rest/avydetails/' + extId, function(data) {
	  if ($('#avyAdminLoggedInEmail').length) {
	    this.wiring.wireReportAdminControls();
	    this.cancelReport();
	    this.currentReport = new AvyReport(this);
	    this.currentReport.displayDetails(data);
	  } else {
	    this.displayDetails(event, data);
	  }
	}.bind(this))
	.fail(function(jqxhr, textStatus, error) {
		var err = textStatus + ", " + error;
		console.log("Avy Eyes error: " + err);
	});
}

AvyEyesView.prototype.displayDetails = function(kmlClickEvent, a) {
	$('#avyDetailTitle').text(a.avyDate + ': ' + a.areaName);
	$('#avyDetailSubmitterExp').text(a.submitterExp.label);
	$('#avyDetailExtLink').attr('href', a.extUrl);
	$('#avyDetailExtLink').text(a.extUrl);

	$('#avyDetailElevation').text(a.elevation);
	$('#avyDetailElevationFt').text(this.metersToFeet(a.elevation));
	$('#avyDetailAspect').text(a.aspect.label);
	$('#avyDetailAngle').text(a.angle);
	  
	$('#avyDetailType').text(a.avyType.label);
	$('#avyDetailTrigger').text(a.avyTrigger.label);
	$('#avyDetailInterface').text(a.avyInterface.label);
	$('#avyDetailRSize').text(a.rSize);
	$('#avyDetailDSize').text(a.dSize);

	$('#avyDetailSky').text(a.sky.label);
	$('#avyDetailPrecip').text(a.precip.label);
	
	this.setSpinner('#avyDetailNumCaught', a.caught);
	this.setSpinner('#avyDetailNumPartiallyBuried', a.partiallyBuried);
	this.setSpinner('#avyDetailNumFullyBuried', a.fullyBuried);
	this.setSpinner('#avyDetailNumInjured', a.injured);
	this.setSpinner('#avyDetailNumKilled', a.killed);
	$('#avyDetailModeOfTravel').text(a.modeOfTravel.label);
	
	if (a.comments.length > 0) {
		$('#avyDetailCommentsRow').show();
		$('#avyDetailComments').html('<pre>' + a.comments.trim() + '</pre>');
	}

	if (a.images.length > 0) {
		$('#avyDetailImageRow').show();
    $.each(a.images, function(i) {
			var imgUrl = '/rest/images/' + a.extId + '/' + a.images[i].filename;
			$('#avyDetailImageList').append('<li class="avyDetailImageListItem"><a href="' + imgUrl 
				+ '" data-lightbox="avyDetailImages"><img src="' + imgUrl + '" /></a></li>');
		});
	}
	
	$('#avyDetailDialog').dialog('option', 'position', {
    my: 'center bottom-20', 
    at: 'center top', 
    of: $.Event('click', {pageX: kmlClickEvent.getClientX(), pageY: kmlClickEvent.getClientY()}),
    collision: 'fit'
  });

  $('#avyDetailDialog').dialog('open');
}

AvyEyesView.prototype.setSpinner = function(inputElem, value) {
  if (value == -1) {
    $(inputElem).text('Unknown');
  } else {
    $(inputElem).text(value);
  }
}

AvyEyesView.prototype.hideAvyDetails = function() {
	$('#avyDetailCommentsRow').hide();
	$('#avyDetailImageList').empty();
	$('#avyDetailImageRow').hide();
	$('#avyDetailDialog').dialog('close');
}

AvyEyesView.prototype.showHelp = function(tab) {
	$('#helpDialog').tabs("option", "active", tab);
	$('#helpDialog').dialog('open');
}

AvyEyesView.prototype.toggleMenu = function() {
	$('#aeMenu').toggle('slide', 400);
}

AvyEyesView.prototype.flyTo = function(lat, lng, rangeMeters, tiltDegrees, headingDegrees) {
	var lookAt = this.ge.createLookAt('');
	lookAt.setLatitude(lat);
	lookAt.setLongitude(lng);
	lookAt.setRange(rangeMeters);
	lookAt.setAltitudeMode(this.ge.ALTITUDE_RELATIVE_TO_GROUND);
	lookAt.setTilt(tiltDegrees);
	lookAt.setHeading(headingDegrees);
	this.ge.getView().setAbstractView(lookAt);
}
    
AvyEyesView.prototype.geocodeAndFlyToLocation = function(address, rangeMeters, tiltDegrees) {
  if (!address) {
	  return;
  }
  
  this.geocoder.geocode( {'address': address}, function(results, status) {
    if (status == this.gmaps.GeocoderStatus.OK && results.length) {
		  var latLng = results[0].geometry.location;
    	this.flyTo(latLng.lat(), latLng.lng(), rangeMeters, tiltDegrees, 0);
    } else {
      this.showModalDialog('Error', 'Failed to geocode "' + address + '"');
    }
  }.bind(this));
}

AvyEyesView.prototype.stopNavControlBlink = function() {
	this.ge.getNavigationControl().setVisibility(this.ge.VISIBILITY_AUTO);
	clearInterval(this.navControlBlinkInterval);
}

AvyEyesView.prototype.navControlBlink = function(numBlinks) {
	var blinkCount = 0;
	$('#map3d').focus();
	this.navControlBlinkInterval = setInterval(function() {
		if (this.ge.getNavigationControl().getVisibility() != this.ge.VISIBILITY_SHOW) {
			this.ge.getNavigationControl().setVisibility(this.ge.VISIBILITY_SHOW);
		} else {
			this.ge.getNavigationControl().setVisibility(this.ge.VISIBILITY_AUTO);
		}
		
		blinkCount++;
		if (numBlinks && blinkCount >= (numBlinks*2)) {
			this.stopNavControlBlink();
		}
	}.bind(this), 1000);
}

AvyEyesView.prototype.overlaySearchResultKml = function(kmlStr) {
	this.clearKmlOverlay();
	this.ge.getFeatures().appendChild(this.ge.parseKml(kmlStr));
}
	
AvyEyesView.prototype.clearKmlOverlay = function() {
	while (this.ge.getFeatures().hasChildNodes()) {
		var child = this.ge.getFeatures().getFirstChild();
		this.ge.getFeatures().removeChild(child);
	}
}

AvyEyesView.prototype.viewChangeEndTimeout = function() {
	var viewChangeEndTimer;
	if(viewChangeEndTimer){
	    clearTimeout(viewChangeEndTimer);
	  }
	viewChangeEndTimer = setTimeout(this.viewChangeEnd(), 200);
}

AvyEyesView.prototype.initEarthCB = function(instance) {
	this.ge = instance;
	this.geocoder = new this.gmaps.Geocoder();
    
	this.ge.getWindow().setVisibility(true);
	this.ge.getOptions().setStatusBarVisibility(true);
	this.ge.getOptions().setUnitsFeetMiles(true);
	this.ge.getOptions().setFlyToSpeed(0.4);
	this.ge.getNavigationControl().setVisibility(this.ge.VISIBILITY_AUTO);
	this.ge.getLayerRoot().enableLayerById(this.ge.LAYER_BORDERS, true);
	this.ge.getLayerRoot().enableLayerById(this.ge.LAYER_ROADS, true);
  this.gearth.addEventListener(this.ge.getView(), 'viewchangeend', (this.viewChangeEndTimeout).bind(this));
  this.gearth.addEventListener(this.ge.getGlobe(), 'click', (this.handleMapClick).bind(this));
    
  this.wiring.wireUI();
  $('#loadingDiv').fadeOut(500);
}
	    
AvyEyesView.prototype.failureEarthCB = function(errorCode) {
  $('#loadingDiv').fadeOut(500); // allow the 'download Google Earth plugin' msg to be shown 
	console.log('Failed to create an instance of Google Earth. Error code: ' + errorCode);
}

AvyEyesView.prototype.init = function() {
  this.gearth.createInstance('map3d', (this.initEarthCB).bind(this), (this.failureEarthCB).bind(this), { 'language': 'en' });
}

AvyEyesView.prototype.metersToFeet = function(meters) {
  return Math.round(meters * 3.28084);
}

return AvyEyesView;
});