define(['ae-wiring',
        'ae-report',
        'jquery-ui', 
        'jquery-geocomplete', 
        'jquery-fileupload', 
        'jquery-iframe-transport',
        'lightbox'
        ], function(wireFunction, AvyReport) {

function AvyEyesView(gearthInst, gmapsInst) {
	this.wireUI = wireFunction;
	
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
	this.currentReport.initAvyReport();
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
	    this.displayDetailsReadWrite(event, data);
	  } else {
	    this.displayDetailsReadOnly(event, data);
	  }
	}.bind(this))
	.fail(function(jqxhr, textStatus, error) {
		var err = textStatus + ", " + error;
		console.log("Avy Eyes error: " + err);
	});
}

AvyEyesView.prototype.displayDetailsReadOnly = function(kmlClickEvent, a) {
  $('#avyReportViewableTd').css('visibility', 'hidden');
  
	$('#avyDetailTitle').text(a.avyDate + ': ' + a.areaName);
	$('#avyDetailSubmitterExp').text(a.submitterExp.label);
	$('#avyDetailExtLink').attr('href', a.extUrl);
	$('#avyDetailExtLink').text(a.extUrl);

	$('#avyDetailElevation').text(a.elevation);
	$('#avyDetailElevationFt').text(this.metersToFeet(a.elevation));
	$('#avyDetailAspect').text(a.aspect.label);
	$('#avyDetailAngle').text(a.angle);
	  
	$('#avyDetailType').text(a.avyType.label);
	$('#avyDetailTrigger').text(a.trigger.label);
	$('#avyDetailInterface').text(a.bedSurface.label);
	$('#avyDetailRSize').text(a.rSize);
	$('#avyDetailDSize').text(a.dSize);

	$('#avyDetailSky').text(a.sky.label);
	$('#avyDetailPrecip').text(a.precip.label);
	
	$('#avyDetailNumCaught').text(this.getSpinnerValueRO(a.caught));
	$('#avyDetailNumPartiallyBuried').text(this.getSpinnerValueRO(a.partiallyBuried));
	$('#avyDetailNumFullyBuried').text(this.getSpinnerValueRO(a.fullyBuried));
	$('#avyDetailNumInjured').text(this.getSpinnerValueRO(a.injured));
	$('#avyDetailNumKilled').text(this.getSpinnerValueRO(a.killed));
	$('#avyDetailModeOfTravel').text(a.modeOfTravel.label);
	
	if (a.comments.length > 0) {
		$('#avyDetailCommentsRow').show();
		$('#avyDetailComments').text(a.comments);			
	}

	if (a.images.length > 0) {
		$('#avyDetailImageRow').show();
        $.each(a.images, function(i) {
			var imgUrl = '/rest/imgserve/' + a.extId + '/' + a.images[i];
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

AvyEyesView.prototype.displayDetailsReadWrite = function(kmlClickEvent, a) {
  $('#avyReportViewableTd').css('visibility', 'visible');
  if (a.viewable) {
    $('#avyReportViewable').attr('checked', true);
    $('#avyReportViewable').siblings('label').css('color', 'green');
  }
  else {
    $('#avyReportViewable').attr('checked', false);
    $('#avyReportViewable').siblings('label').css('color', 'red');
  }
  
  $('#avyReportSubmitterEmail').val(a.submitterEmail);
  this.setAutocomplete('#avyReportSubmitterExp', a.submitterExp);
  
  $('#avyReportAreaName').val(a.areaName);
  $('#avyReportDate').val(a.avyDate);
  this.setAutocomplete('#avyReportSky', a.sky);
  this.setAutocomplete('#avyReportPrecip', a.precip);
  
  this.setAutocomplete('#avyReportType', a.avyType);
  this.setAutocomplete('#avyReportTrigger', a.trigger);
  this.setAutocomplete('#avyReportBedSurface', a.bedSurface);
  this.setSlider('#avyReportRsizeValue', a.rSize);
  this.setSlider('#avyReportDsizeValue', a.dSize);
  
  $('#avyReportElevation').val(a.elevation);
  $('#avyReportElevationFt').val(this.metersToFeet(a.elevation));
  this.setAutocomplete('#avyReportAspect', a.aspect);
  $('#avyReportAngle').val(a.angle);
  
  $('#avyReportNumCaught').val(this.getSpinnerValueRW(a.caught));
  $('#avyReportNumPartiallyBuried').val(this.getSpinnerValueRW(a.partiallyBuried));
  $('#avyReportNumFullyBuried').val(this.getSpinnerValueRW(a.fullyBuried));
  $('#avyReportNumInjured').val(this.getSpinnerValueRW(a.injured));
  $('#avyReportNumKilled').val(this.getSpinnerValueRW(a.killed));
  this.setAutocomplete('#avyReportModeOfTravel', a.modeOfTravel);
  
  $('#avyReportComments').val(a.comments);
  
  $('#avyReportDialog').dialog('open');
}

AvyEyesView.prototype.setAutocomplete = function(hiddenSibling, enumObj) {
  $(hiddenSibling).val(enumObj.value)
  $(hiddenSibling).siblings('.avyAutoComplete').val(enumObj.label);
}

AvyEyesView.prototype.setSlider = function(inputElem, value) {
  $(inputElem).val(value)
  $(inputElem).siblings('.avyRDSlider').slider('value', value);
}

AvyEyesView.prototype.getSpinnerValueRO = function(rawValue) {
  if (rawValue == -1) return 'Unknown';
  else return rawValue;
}

AvyEyesView.prototype.getSpinnerValueRW = function(rawValue) {
  if (rawValue == -1) return '';
  else return rawValue;
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
    
    this.wireUI(this);
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