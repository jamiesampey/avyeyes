define(['avyeyes.ui',
        'avyeyes.form',
        'avyeyes.report',
        'lib/Cesium/Cesium'
        ], function(AvyEyesUI, AvyForm, AvyReport, Cesium) {

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
        AvyForm.hideReadOnlyForm();

        var pick = this.cesiumViewer.scene.pick(movement.position);
        if (Cesium.defined(pick)) {
            var selectedAvalanche = pick.id;

            $.getJSON('/rest/avydetails/' + selectedAvalanche.id, function(data) {
                if ($('#avyAdminLoggedInEmail').length) {
                    AvyForm.wireReadWriteFormAdminControls(this);
                    AvyForm.displayReadWriteForm(data);
                } else {
                    AvyForm.displayReadOnlyForm(movement.position, data);
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
	AvyEyesUI.showSearchDiv();
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
    AvyForm.closeReportDialogs();
    AvyForm.clearReportFields();
	this.currentReport = null;
}

AvyEyesView.prototype.showHelp = function(tab) {
	$('#helpDialog').tabs("option", "active", tab);
	$('#helpDialog').dialog('open');
}

AvyEyesView.prototype.addAvalanches = function(avalancheArray) {
    $.each(avalancheArray, function(i, avalanche) {
        this.addAvalanche(avalanche);
    }.bind(this));

    AvyEyesUI.hideSearchDiv();
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
    AvyEyesUI.raiseTheCurtain();
    this.flyTo(avalancheEntity, flyToHeadingFromAspect(avalanche.aspect), -25, 700, false);
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
  var range = 2000000;

  var flyToWesternUS = function() {
    AvyEyesUI.raiseTheCurtain();
    this.flyTo(this.targetEntityFromCoords(-112, 44, false),
        heading, pitch, range, true).then(function() {
        AvyEyesUI.showSearchDiv();
    }.bind(this));
  }.bind(this)

  if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function(pos) {
        AvyEyesUI.raiseTheCurtain();
		this.flyTo(this.targetEntityFromCoords(pos.coords.longitude, pos.coords.latitude, true),
		    0.0, pitch, range, true).then(function() {
		    AvyEyesUI.showSearchDiv();
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

return AvyEyesView;
});