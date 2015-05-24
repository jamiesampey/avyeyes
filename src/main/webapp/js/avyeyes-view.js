define(['avyeyes',
        'lib/Cesium/Cesium',
        'avyeyes-report',
        'lib/jquery-ui',
        'lib/jquery.geocomplete',
        'lib/jquery.fileupload',
        'lib/jquery.iframe-transport',
        'lib/lightbox'
        ], function(AvyEyes, Cesium, AvyReport) {

function AvyEyesView(gmapsInst) {
    this.gmaps = gmapsInst;
    this.geocoder = new this.gmaps.Geocoder();

    this.cesiumViewer = new Cesium.Viewer('cesiumContainer', {
        sceneMode: Cesium.SceneMode.SCENE3D,
        terrainProvider: new Cesium.CesiumTerrainProvider({
            url: '//cesiumjs.org/stk-terrain/tilesets/world/tiles'
        }),
        imageryProvider: new Cesium.BingMapsImageryProvider({
            url: '//dev.virtualearth.net',
            key: 'AiXcgClqr_8DxjhvM5bal45QdMumBNOllccwdibv5ViVRKR1xTh9iA5GugmmINPr',
            mapStyle: Cesium.BingMapsStyle.AERIAL_WITH_LABELS
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

    this.cesiumEventHandler = new Cesium.ScreenSpaceEventHandler(this.cesiumViewer.scene.canvas);
    this.setAvySelectEventHandler();
}

AvyEyesView.prototype.setAvySelectEventHandler = function() {
    this.cesiumEventHandler.setInputAction(function(movement) {
        if($('#avyDetailDialog').is(':visible')) {
            this.hideAvyDetails();
        }

        var pick = this.cesiumViewer.scene.pick(movement.position);
        if (Cesium.defined(pick)) {
            var selectedAvalanche = pick.id;

            $.getJSON('/rest/avydetails/' + selectedAvalanche.id, function(data) {
                if ($('#avyAdminLoggedInEmail').length) {
                    AvyEyes.wireReportAdminControls();
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
}

AvyEyesView.prototype.resetView = function() {
	this.cesiumViewer.entities.removeAll();
	this.cancelReport();
	AvyEyes.showSearchDiv();
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
    AvyEyes.closeReportDialogs();
    AvyEyes.clearReportFields();
	this.currentReport = null;
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
	$('#avyDetailElevationFt').text(AvyEyes.metersToFeet(a.elevation));
	$('#avyDetailAspect').text(a.aspect.label);
	$('#avyDetailAngle').text(a.angle);
	  
	$('#avyDetailType').text(a.avyType.label);
	$('#avyDetailTrigger').text(a.avyTrigger.label);
	$('#avyDetailInterface').text(a.avyInterface.label);
	$('#avyDetailRSize').text(a.rSize);
	$('#avyDetailDSize').text(a.dSize);

	$('#avyDetailSky').text(a.sky.label);
	$('#avyDetailPrecip').text(a.precip.label);
	
	setSpinnerVal('#avyDetailNumCaught', a.caught);
	setSpinnerVal('#avyDetailNumPartiallyBuried', a.partiallyBuried);
	setSpinnerVal('#avyDetailNumFullyBuried', a.fullyBuried);
	setSpinnerVal('#avyDetailNumInjured', a.injured);
	setSpinnerVal('#avyDetailNumKilled', a.killed);
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

    AvyEyes.hideSearchDiv();
}

AvyEyesView.prototype.addAvalanche = function(avalanche) {
	return this.cesiumViewer.entities.add({
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
    AvyEyes.raiseTheCurtain();
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
    AvyEyes.raiseTheCurtain();
    this.flyTo(this.targetEntityFromCoords(-112, 44, false),
        heading, pitch, range, true).then(function() {
        AvyEyes.showSearchDiv();
    }.bind(this));
  }.bind(this)

  if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function(pos) {
        AvyEyes.raiseTheCurtain();
		this.flyTo(this.targetEntityFromCoords(pos.coords.longitude, pos.coords.latitude, true),
		    0.0, pitch, range, true).then(function() {
		    AvyEyes.showSearchDiv();
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

    var alt = this.cesiumViewer.scene.globe.getHeight(Cesium.Cartographic.fromDegrees(lng, lat));
	return this.cesiumViewer.entities.add({
    	position: Cesium.Cartesian3.fromDegrees(lng, lat, alt),
    	billboard: {image: imageUri}
    });
}

AvyEyesView.prototype.flyTo = function (targetEntity, heading, pitch, range, removeTargetAfterFlight) {
	var flightDurationSeconds = 3.0;
	if (removeTargetAfterFlight) {
		setTimeout(function() {
			this.cesiumViewer.entities.remove(targetEntity)
		}.bind(this), flightDurationSeconds * 2000);
	}

	return this.cesiumViewer.flyTo(targetEntity, {
        duration: flightDurationSeconds,
        offset: new Cesium.HeadingPitchRange(Cesium.Math.toRadians(heading), Cesium.Math.toRadians(pitch), range)
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

function setSpinnerVal(inputElem, value) {
    if (value == -1) {
        $(inputElem).text('Unknown');
    } else {
        $(inputElem).text(value);
    }
}

return AvyEyesView;
});