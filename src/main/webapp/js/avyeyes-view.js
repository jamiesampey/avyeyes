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

  this.gmaps = null;
  this.geocoder = null;
  this.currentReport = null;
}

AvyEyesView.prototype.init = function(gmapsInst) {
  this.gmaps = gmapsInst;
  this.geocoder = new this.gmaps.Geocoder();

  var leftClickHandler = new Cesium.ScreenSpaceEventHandler(this.viewer.scene.canvas);
  leftClickHandler.setInputAction(function(movement) {
    if($('#avyDetailDialog').is(':visible')) {
        this.hideAvyDetails();
    }

    var pick = this.viewer.scene.pick(movement.position);
    if (Cesium.defined(pick)) {
        var selectedAvalanche = pick.id;

        $.getJSON('/rest/avydetails/' + selectedAvalanche.id, function(data) {
          if ($('#avyAdminLoggedInEmail').length) {
            this.wiring.wireReportAdminControls();
            this.cancelReport();
            this.currentReport = new AvyReport(this);
            this.currentReport.displayDetails(data);
          } else {
            this.displayDetails(movement.position, data);
          }
        }.bind(this))
        .fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            console.log("Avy Eyes error: " + err);
        });
    }
  }.bind(this), Cesium.ScreenSpaceEventType.LEFT_CLICK);

  new AvyEyesWiring(this).wireUI();
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
	this.viewer.entities.removeAll();
	this.cancelReport();
	this.showSearchDiv();
}

AvyEyesView.prototype.clearSearchFields = function() {
	$('#aeSearchControlContainer').find('input:text').val('');
	$('#aeSearchControlContainer').find('.avyRDSliderValue').val('0');
	$('#aeSearchControlContainer').find('.avyRDSlider').slider('value', 0);
}

AvyEyesView.prototype.setSearchViewBounds = function() {
	var upperLeft = Cesium.Ellipsoid.WGS84.cartesianToCartographic(
		this.viewer.camera.pickEllipsoid(new Cesium.Cartesian2(0, 0), Cesium.Ellipsoid.WGS84));
	var lowerRight = Cesium.Ellipsoid.WGS84.cartesianToCartographic(
		this.viewer.camera.pickEllipsoid(new Cesium.Cartesian2(this.viewer.canvas.width,
		this.viewer.canvas.height), Cesium.Ellipsoid.WGS84));
	var camPos = Cesium.Ellipsoid.WGS84.cartesianToCartographic(this.viewer.camera.position);

	$("#avySearchLatTop").val(radToDeg(upperLeft.latitude));
	$("#avySearchLatBottom").val(radToDeg(lowerRight.latitude));
	$("#avySearchLngLeft").val(radToDeg(upperLeft.longitude));
	$("#avySearchLngRight").val(radToDeg(lowerRight.longitude));

  	$("#avySearchCameraAlt").val(this.viewer.scene.globe.getHeight(camPos));
  	$("#avySearchCameraPitch").val(radToDeg(this.viewer.camera.pitch));
  	$("#avySearchCameraLat").val(radToDeg(camPos.latitude));
  	$("#avySearchCameraLng").val(radToDeg(camPos.longitude));
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
	this.currentReport.beginReport();
}

AvyEyesView.prototype.cancelReport = function() {
	if (this.currentReport) {
		this.currentReport.closeAllReportDialogs();
		this.currentReport.clearAllFields();
		this.currentReport.clearAvyDrawing();
		this.currentReport = null;
	}
}

AvyEyesView.prototype.displayDetails = function(mousePos, a) {
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
    of: $.Event('click', {pageX: mousePos.x, pageY: mousePos.y}),
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

AvyEyesView.prototype.addAvalanches = function(avalancheArray) {
    $.each(avalancheArray, function(i, avalanche) {
        this.addAvalanche(avalanche);
    }.bind(this));

    this.hideSearchDiv();
}

AvyEyesView.prototype.addAvalanche = function(avalanche) {
	return this.viewer.entities.add({
	    id: avalanche.extId,
	    polygon: {
            material: Cesium.Color.RED.withAlpha(0.4),
            perPositionHeight: true,
            hierarchy: Cesium.Cartesian3.fromDegreesArrayHeights(avalanche.coords)
        }
    });
}

AvyEyesView.prototype.addAvalancheAndFlyTo = function(avalanche) {
    var avalancheEntity = this.addAvalanche(avalanche);
    raiseTheCurtain();
    this.flyTo(avalancheEntity, flyToHeadingFromAspect(avalanche.aspect), -25, 500, false);
}

AvyEyesView.prototype.geocodeAndFlyTo = function(address, range, pitch) {
  if (!address) return;

  this.geocoder.geocode( {'address': address}, function(results, status) {
    if (status == this.gmaps.GeocoderStatus.OK && results.length) {
		var latLng = results[0].geometry.location;
    	this.flyTo(this.targetEntityFromCoords(latLng.lng(), latLng.lat(), true),	0.0, pitch, range, true);
    } else {
      this.showModalDialog('Error', 'Failed to geocode "' + address + '"');
    }
  }.bind(this));
}

AvyEyesView.prototype.geolocateAndFlyTo = function() {
  var self = this;

  var heading = 0.0;
  var pitch = -89.9; // work around -90 degree problem in flyToBoundingSphere
  var range = 2500000;

  var flyToWesternUS = function() {
    raiseTheCurtain();
    this.flyTo(this.targetEntityFromCoords(-112, 44, false),
        heading, pitch, range, true).then(function() {
        this.showSearchDiv();
    }.bind(this));
  }.bind(this)

  if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function(pos) {
        raiseTheCurtain();
		this.flyTo(this.targetEntityFromCoords(pos.coords.longitude, pos.coords.latitude, true),
		    0.0, pitch, range, true).then(function() {
		    this.showSearchDiv();
		}.bind(this));
	  }.bind(this), flyToWesternUS, {timeout:8000, enableHighAccuracy:false});
  } else {
      flyToWesternUS();
  }
}

AvyEyesView.prototype.targetEntityFromCoords = function(lng, lat, showPin) {
    var imageUri;
    if (showPin) {
        imageUri = '/images/flyto-pin.png';
    } else {
        imageUri = '/images/flyto-1px.png';
    }

    var alt = this.viewer.scene.globe.getHeight(Cesium.Cartographic.fromDegrees(lng, lat));
	return this.viewer.entities.add({
    	position: Cesium.Cartesian3.fromDegrees(lng, lat, alt),
    	billboard: {image: imageUri}
    });
}

AvyEyesView.prototype.flyTo = function (targetEntity, heading, pitch, range, removeTargetAfterFlight) {
	var flightDurationSeconds = 3.0;
	if (removeTargetAfterFlight) {
		setTimeout(function() {
			this.viewer.entities.remove(targetEntity)
		}.bind(this), flightDurationSeconds * 2000);
	}

	return this.viewer.flyTo(targetEntity, {
        duration: flightDurationSeconds,
        offset: new Cesium.HeadingPitchRange(degToRad(heading), degToRad(pitch), range)
    });
}

function flyToHeadingFromAspect(aspect) {
    if (aspect === "N") return 180.0;
    else if (aspect === "NE") return 225.0;
    else if (aspect === "E") return 270.0;
    else if (aspect === "SE") return 315.0;
    else if (aspect === "SW") return 45.0;
    else if (aspect === "W") return 90.0;
    else if (aspect === "NW") return 135.0;
    else return 0.0;
}

function raiseTheCurtain() {
    if ($('#loadingDiv').is(':visible')) {
        $('#loadingDiv').fadeOut(500);
    }
}

AvyEyesView.prototype.metersToFeet = function(meters) {
  return Math.round(meters * 3.28084);
}

function degToRad(degrees) {
	return Cesium.Math.toRadians(degrees);
}

function radToDeg(radians) {
	return Cesium.Math.toDegrees(radians);
}

return AvyEyesView;
});