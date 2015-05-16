define(['lib/Cesium/Cesium',
		'avyeyes-wiring',
        'avyeyes-report',
        'lib/jquery-ui',
        'lib/jquery.geocomplete',
        'lib/jquery.fileupload',
        'lib/jquery.iframe-transport',
        'lib/lightbox'
        ], function(Cesium, AvyEyesWiring, AvyReport) {

function AvyEyesView() {
  this.viewer = new Cesium.Viewer('cesiumContainer', {
  	sceneMode: Cesium.SceneMode.SCENE3D,
  	terrainProvider: new Cesium.CesiumTerrainProvider({
      url : '//cesiumjs.org/stk-terrain/tilesets/world/tiles'
    }),
    imageryProvider: new Cesium.BingMapsImageryProvider({
	  url : '//dev.virtualearth.net',
	  key : 'AiXcgClqr_8DxjhvM5bal45QdMumBNOllccwdibv5ViVRKR1xTh9iA5GugmmINPr',
	  mapStyle : Cesium.BingMapsStyle.AERIAL_WITH_LABELS
    }),
    animation: false,
    baseLayerPicker: false,
    fullscreenButton: true,
    geocoder: false,
    homeButton: false,
    infoBox: false,
    sceneModePicker: false,
    selectionIndicator: false,
    timeline: false,
    navigationHelpButton: true,
    navigationInstructionsInitiallyVisible: false,
	scene3DOnly: true
  });

  this.lastCameraPosition;
  this.lastCameraTime = Cesium.getTimestamp();

  this.gmaps = null;
  this.geocoder = null;
  this.currentReport = null;
}

AvyEyesView.prototype.init = function(gmapsInst) {
//  this.viewer.scene.preRender.addEventListener(function(scene) {
//	  var time = Cesium.getTimestamp();
//      var position = scene.camera.position;
//
//      if (!Cesium.Cartesian3.equalsEpsilon(this.lastCameraPosition, position, Cesium.Math.EPSILON4)) {
//        this.lastCameraTime = time;
//      } else if (time - this.lastCameraTime > 500) {
//        alert('view change end');
//      	this.lastCameraTime = time;
//      }
//
//      this.lastCameraPosition = position.clone();
//
//	  //	var viewBoundsBox = this.ge.getView().getViewportGlobeBounds();
//	  //	$("#avySearchNorthLimit").val(viewBoundsBox.getNorth());
//	  //	$("#avySearchEastLimit").val(viewBoundsBox.getEast());
//	  //	$("#avySearchSouthLimit").val(viewBoundsBox.getSouth());
//	  //	$("#avySearchWestLimit").val(viewBoundsBox.getWest());
//	  //
//	  //	var camera = this.ge.getView().copyAsCamera(this.ge.ALTITUDE_RELATIVE_TO_GROUND);
//	  //	$("#avySearchCameraAlt").val(camera.getAltitude());
//	  //	$("#avySearchCameraTilt").val(camera.getTilt());
//	  //	$("#avySearchCameraLat").val(camera.getLatitude());
//	  //	$("#avySearchCameraLng").val(camera.getLongitude());
//  	}.bind(this));

  this.gmaps = gmapsInst;
  this.geocoder = new this.gmaps.Geocoder();

  new AvyEyesWiring(this).wireUI();
  $('#loadingDiv').fadeOut(500);
}

AvyEyesView.prototype.showSearchDiv = function(delay) {
	if (delay > 0) {
	  setTimeout(function() {
		$('#aeSearchControlContainer').slideDown("slow");
	  }, delay);
	} else {
	  $('#aeSearchControlContainer').slideDown("slow");
	}
}

AvyEyesView.prototype.hideSearchDiv = function() {
  $('#aeSearchControlContainer').slideUp("slow");
}

AvyEyesView.prototype.resetView = function() {
//	this.clearKmlOverlay();
	this.cancelReport();
	this.showSearchDiv();
}

AvyEyesView.prototype.clearSearchFields = function() {
	$('#aeSearchControlContainer').find('input:text').val('');
	$('#aeSearchControlContainer').find('.avyRDSliderValue').val('0');
	$('#aeSearchControlContainer').find('.avyRDSlider').slider('value', 0);
}

AvyEyesView.prototype.showModalDialog = function(title, msg, delay) {
	$.ui.dialog.prototype._focusTabbable = function(){};
	$('#multiDialog').html(msg);
	$('#multiDialog').dialog('option', 'title', title);

	if (delay > 0) {
	  setTimeout(function() {
	    $('#multiDialog').dialog('open');
	    }, delay);
	} else {
	  $('#multiDialog').dialog('open');
	}
}

AvyEyesView.prototype.doReport = function() {
	this.cancelReport();
	this.currentReport = new AvyReport(this);
	this.currentReport.beginReportWizard();
}

AvyEyesView.prototype.cancelReport = function() {
	if (this.currentReport) {
		this.currentReport.closeAllReportDialogs();
		this.currentReport.clearAllFields();
		this.currentReport.clearAvyDrawing();
		this.currentReport = null;
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
    var title = a.avyDate + ': ' + a.areaName;

	$('#avyDetailTitle').text(title);
	$('#avyDetailSubmitterExp').text(a.submitterExp.label);

	$('#avyDetailExtLink').attr('href', a.extUrl);
	$('#avyDetailExtLink').text(a.extUrl);

    var fbContainer = $('#avyDetailSocialFacebookContainer');
    fbContainer.empty();
    fbContainer.append('<div class="fb-share-button" data-layout="button_count" data-href="' + a.extUrl + '">');

    var twttrContainer = $('#avyDetailSocialTwitterContainer');
    twttrContainer.empty();
    twttrContainer.append('<a class="twitter-share-button" data-url="' + a.extUrl + '" data-text="' + title
      + '" href="http://twitter.com/share" data-count="horizontal">');

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

  FB.XFBML.parse(fbContainer[0]);
  twttr.widgets.load();
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

//AvyEyesView.prototype.overlaySearchResultKml = function(kmlStr) {
//	this.clearKmlOverlay();
//	this.ge.getFeatures().appendChild(this.ge.parseKml(kmlStr));
//}
	
//AvyEyesView.prototype.clearKmlOverlay = function() {
//	while (this.ge.getFeatures().hasChildNodes()) {
//		var child = this.ge.getFeatures().getFirstChild();
//		this.ge.getFeatures().removeChild(child);
//	}
//}

AvyEyesView.prototype.flyTo = function(lng, lat, range, pitch, heading) {
	if ($('#loadingDiv').is(':visible')) {
	  $('#loadingDiv').fadeOut(500);
	}

    var alt = this.viewer.scene.globe.getHeight(Cesium.Cartographic.fromDegrees(lng, lat));
	var targetEntity = this.viewer.entities.add({
    	position: Cesium.Cartesian3.fromDegrees(lng, lat, alt),
    	billboard: {image: '/images/blue-flyto-pin.png'}
    });

	var flightDurationSeconds = 3.0;
	var headingPitchRange = new Cesium.HeadingPitchRange(degToRad(heading), degToRad(pitch), range);

	this.viewer.flyTo(targetEntity, {
		duration: flightDurationSeconds,
		offset: headingPitchRange
	});

	setTimeout(function() {
		this.viewer.entities.remove(targetEntity)
	}.bind(this), flightDurationSeconds * 1500);
}

AvyEyesView.prototype.geocodeAndFlyTo = function(address, rangeMeters, tiltDegrees) {
  if (!address) {
	  return;
  }
  
  this.geocoder.geocode( {'address': address}, function(results, status) {
    if (status == this.gmaps.GeocoderStatus.OK && results.length) {
		var latLng = results[0].geometry.location;
    	this.flyTo(latLng.lng(), latLng.lat(), rangeMeters, tiltDegrees, 0);
    } else {
      this.showModalDialog('Error', 'Failed to geocode "' + address + '"');
    }
  }.bind(this));
}

AvyEyesView.prototype.geolocateAndFlyTo = function(rangeMeters, tiltDegrees) {
  var self = this;
  var flown = false;

  var flyToWesternUnitedStates = function() {
    flown = true;
  	self.flyTo(-115, 44, rangeMeters, tiltDegrees, 0);
  }

  setTimeout(function() {if (!flown) flyToWesternUnitedStates();}, 10000) // 10 second 'ignore' timeout

  if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function(pos) {
		flown = true;
		self.flyTo(pos.coords.longitude, pos.coords.latitude, rangeMeters, tiltDegrees, 0);
	  }, flyToWesternUnitedStates, {timeout:5000, enableHighAccuracy:false});
  } else {
      flyToWesternUnitedStates();
  }
}

AvyEyesView.prototype.metersToFeet = function(meters) {
  return Math.round(meters * 3.28084);
}

function degToRad(degrees) {
	return Cesium.Math.toRadians(degrees);
}

return AvyEyesView;
});