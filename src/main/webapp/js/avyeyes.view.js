define(["avyeyes.ui",
        "avyeyes.form",
        "avyeyes.report"
        ], function(AvyEyesUI, AvyForm, AvyReport) {

function AvyEyesView() {
    this.bingKey = "AiXcgClqr_8DxjhvM5bal45QdMumBNOllccwdibv5ViVRKR1xTh9iA5GugmmINPr";

    this.cesiumViewer = new Cesium.Viewer("cesiumContainer", {
        sceneMode: Cesium.SceneMode.SCENE3D,
        imageryProvider: new Cesium.BingMapsImageryProvider({
            url: "//dev.virtualearth.net",
            key: this.bingKey,
            mapStyle: Cesium.BingMapsStyle.AERIAL_WITH_LABELS
        }),
        terrainProvider: new Cesium.CesiumTerrainProvider({
            url: "//assets.agi.com/stk-terrain/world",
            requestVertexNormals: true
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

    this.setCameraMoveEventListener();

    this.cesiumEventHandler = new Cesium.ScreenSpaceEventHandler(this.cesiumViewer.scene.canvas);
    this.setAvySelectEventHandler();

    this.form = new AvyForm();
    this.ui = new AvyEyesUI();
    this.ui.wire(this, function() {
        var tryLiftCallback = function() {
             if (typeof window.liftAjax != "undefined") {
                 $("#avyInitLiftCallback").submit();
                 return true;
             } else {
                 return false;
             }
        }

        if (!tryLiftCallback()) {
            var liftCheckInterval;
            liftCheckInterval = setInterval(function() {
                console.log("liftAjax is undefined. Trying again...");
                if (tryLiftCallback()) clearInterval(liftCheckInterval);
            }, 500);
        }
    });
}

AvyEyesView.prototype.setCameraMoveEventListener = function() {
    var eyeAltSetter = {};
    this.cesiumViewer.camera.moveStart.addEventListener(function() {
        eyeAltSetter = setInterval(function() {
            $("#eyeAltContainer").html("eye alt: " + this.camDisplayAlt());
        }.bind(this), 10);
    }.bind(this));
    this.cesiumViewer.camera.moveEnd.addEventListener(function() {
        clearInterval(eyeAltSetter);
    }.bind(this));
}

AvyEyesView.prototype.setAvySelectEventHandler = function() {
    this.cesiumEventHandler.setInputAction(function(movement) {
        this.form.hideReadOnlyForm();

        var pick = this.cesiumViewer.scene.pick(movement.position);
        if (Cesium.defined(pick)) {
            $("#cesiumContainer").css("cursor", "wait");

            $.getJSON("/rest/avydetails/" + pick.id, function(data) {
                if (adminLogin()) {
                    this.form.wireReadWriteFormAdminControls(this);
                    this.form.displayReadWriteForm(data);
                } else {
                    this.form.displayReadOnlyForm(movement.position, data);
                }
            }.bind(this))
            .fail(function(jqxhr, textStatus, error) {
                var err = textStatus + ", " + error;
                console.log("AvyEyes error: " + err);
            });
        }
    }.bind(this), Cesium.ScreenSpaceEventType.LEFT_CLICK);
}

AvyEyesView.prototype.showModalDialog = function(title, msg) {
    $("#multiDialog").html(msg);
    $("#multiDialog").dialog("option", "title", title);
    $("#multiDialog").dialog("open");
}

AvyEyesView.prototype.showHelp = function(tab) {
	$("#helpDialog").tabs("option", "active", tab);
	$("#helpDialog").dialog("open");
}

AvyEyesView.prototype.doReport = function() {
	this.cancelReport();
	this.currentReport = new AvyReport(this);
	this.currentReport.beginReport();
}

AvyEyesView.prototype.cancelReport = function() {
    this.form.closeReportDialogs();
    this.form.clearReportFields();
	this.currentReport = null;
}

AvyEyesView.prototype.resetView = function() {
	this.cesiumViewer.entities.removeAll();
	if (!this.cesiumViewer.scene.groundPrimitives.isDestroyed()) {
	    this.cesiumViewer.scene.groundPrimitives.removeAll();
	}

	this.cancelReport();
	this.ui.showSearchDiv();
}

AvyEyesView.prototype.addAvalanches = function(avalancheArray) {
    $.each(avalancheArray, function(i, avalanche) {
        this.addAvalanche(avalanche);
    }.bind(this));

    this.ui.hideSearchDiv();
}

AvyEyesView.prototype.addAvalanche = function(avalanche) {
    console.log("gpSupported: " + Cesium.GroundPrimitive.isSupported(this.cesiumViewer.scene));

    var avalancheGeometry = new Cesium.GeometryInstance({
        id: avalanche.extId,
        geometry: new Cesium.PolygonGeometry({
            polygonHierarchy: {
                positions: Cesium.Cartesian3.fromDegreesArrayHeights(avalanche.coords)
            }
        }),
        attributes: {
            color: Cesium.ColorGeometryInstanceAttribute.fromColor(new Cesium.Color(1.0, 0.0, 0.0, 0.4))
        }
    });

    this.cesiumViewer.scene.groundPrimitives.add(new Cesium.GroundPrimitive({
        compressVertices: false,
        geometryInstance: avalancheGeometry
    }));
}

AvyEyesView.prototype.addAvalancheAndFlyTo = function(a) {
    var showTitle = function() {
        this.showModalDialog("Info", a.date + ": " + a.areaName + "<br/>" +
            "<span style='font-size: .8em;'>Submitter: </span><span style='font: italic .8em times,serif'>"
            + a.submitterExp.label + "</span><br/><br/>Click on the red avalanche path for details");
    }.bind(this);

    this.addAvalanche(a);
    var avalancheTargetEntity = this.cesiumViewer.entities.add({
        polygon: {
            material: Cesium.Color.RED.withAlpha(0.0),
            perPositionHeight: true,
            hierarchy: Cesium.Cartesian3.fromDegreesArrayHeights(a.coords)
        }
    });

    this.ui.raiseTheCurtain();
    this.flyTo(avalancheTargetEntity, flyToHeadingFromAspect(a.slope.aspect.value), -25, 700).then(function() {
        showTitle();
        this.cesiumViewer.entities.remove(avalancheTargetEntity);
    }.bind(this));
}

var geocodeAttempts = 0;
AvyEyesView.prototype.geocodeAndFlyTo = function(address, pitch, range) {
    if (!address) return;

    geocodeAttempts++;

    var geocodeFailure = function(error) {
        geocodeAttempts = 0
        this.showModalDialog("Error", "Failed to geocode '" + address + "'");
    }.bind(this);

    this.geocode(address, function(data) {
        if (data.resourceSets.length === 0
           || data.resourceSets[0].resources.length === 0
           || data.resourceSets[0].resources[0].geocodePoints.length === 0) {
            if (geocodeAttempts < 3) {
                this.geocodeAndFlyTo(address, pitch, range);
            } else {
                geocodeFailure(address);
            }
            return;
        }

        var geocodePoints = data.resourceSets[0].resources[0].geocodePoints[0];
        var geocodedTarget = this.targetEntityFromCoords(geocodePoints.coordinates[1], geocodePoints.coordinates[0], true);
        this.flyTo(geocodedTarget, 0.0, pitch, range).then(function() {
            this.cesiumViewer.entities.remove(geocodedTarget);
        }.bind(this));
        geocodeAttempts = 0;
    }.bind(this), geocodeFailure);
}

AvyEyesView.prototype.geocode = function(address, onSuccess, onFailure) {
    if (!address) return;
    var boundingBox = this.getBoundingBox();

    $.ajax({
        url: "//dev.virtualearth.net/REST/v1/Locations",
        dataType: "jsonp",
        data: {
            key: this.bingKey,
            q: address,
            umv: boundingBox[1] + "," + boundingBox[3] + "," + boundingBox[0] + "," + boundingBox[2]
        },
        jsonp: "jsonp",
        success: function(data) {
            onSuccess(data);
        },
        error: function(e) {
            onFailure(e);
        }
    });
}

AvyEyesView.prototype.geolocateAndFlyTo = function() {
  var self = this;

  var heading = 0.0;
  var pitch = -89.9; // work around -90 degree problem in flyToBoundingSphere
  var range = 2000000;

  var flyToWesternUS = function() {
    this.ui.raiseTheCurtain();
    var westernUSTarget = this.targetEntityFromCoords(-112, 44, false);
    this.flyTo(westernUSTarget, heading, pitch, range).then(function() {
        this.cesiumViewer.entities.remove(westernUSTarget);
        this.ui.showSearchDiv();
    }.bind(this));
  }.bind(this)

  if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(function(pos) {
        this.ui.raiseTheCurtain();
        var geolocatedTarget = this.targetEntityFromCoords(pos.coords.longitude, pos.coords.latitude, true);
		this.flyTo(geolocatedTarget, 0.0, pitch, range).then(function() {
		    this.cesiumViewer.entities.remove(geolocatedTarget);
		    this.ui.showSearchDiv();
		}.bind(this));
	  }.bind(this), flyToWesternUS, {timeout:8000, enableHighAccuracy:false});
  } else {
      flyToWesternUS();
  }
}

AvyEyesView.prototype.targetEntityFromCoords = function(lng, lat, showPin) {
    var alt = this.cesiumViewer.scene.globe.getHeight(Cesium.Cartographic.fromDegrees(lng, lat));

    if (showPin) {
        return this.cesiumViewer.entities.add({
            position: Cesium.Cartesian3.fromDegrees(lng, lat, alt),
            billboard: {image: "/images/flyto-pin.png"}
        });
    } else {
        return this.cesiumViewer.entities.add({
            position: Cesium.Cartesian3.fromDegrees(lng, lat, alt),
            point: {color: Cesium.Color.WHITE.withAlpha(0.0)}
        });
    }
}

AvyEyesView.prototype.flyTo = function (targetEntity, heading, pitch, range) {
	var camHeight = this.cesiumViewer.camera.positionCartographic.height

	var flightDurationSeconds = 3.0;

    var finalFlight = function() {
        return this.cesiumViewer.flyTo(targetEntity, {
            duration: flightDurationSeconds,
            offset: toHeadingPitchRange(heading, pitch, range)
        });
    }.bind(this);

    if (range < 2000 && camHeight > 50000) {
        return this.cesiumViewer.flyTo(targetEntity, {
            duration: flightDurationSeconds,
            offset: toHeadingPitchRange(0, -89.9, 4000)
        }).then(finalFlight);
    } else {
        return finalFlight();
    }
}

AvyEyesView.prototype.camDisplayAlt = function() {
    var meters = this.cesiumViewer.camera.positionCartographic.height.toFixed(0);

    if (meters > 10000) {
        return (meters / 1000).toFixed(2) + " km";
    } else {
        return meters + " meters";
    }
}

AvyEyesView.prototype.getBoundingBox = function() {
    var getCoordsAtWindowPos = function(x, y) {
        var ray = this.cesiumViewer.camera.getPickRay(new Cesium.Cartesian2(x, y));
        var cart3 = this.cesiumViewer.scene.globe.pick(ray, this.cesiumViewer.scene);
        if (cart3) {
            return Cesium.Ellipsoid.WGS84.cartesianToCartographic(cart3);
        }
    }.bind(this);

    var UL = getCoordsAtWindowPos(0, 0);
    var UR = getCoordsAtWindowPos(this.cesiumViewer.canvas.clientWidth, 0);
    var LR = getCoordsAtWindowPos(this.cesiumViewer.canvas.clientWidth, this.cesiumViewer.canvas.clientHeight);
    var LL = getCoordsAtWindowPos(0, this.cesiumViewer.canvas.clientHeight);

    if (!UL || !UR || !LR || !LL) {
        return ['','','',''];
    }

    return [
        Cesium.Math.toDegrees(Math.max(UL.latitude, UR.latitude, LR.latitude, LL.latitude)),
        Cesium.Math.toDegrees(Math.min(UL.latitude, UR.latitude, LR.latitude, LL.latitude)),
        Cesium.Math.toDegrees(Math.max(UL.longitude, UR.longitude, LR.longitude, LL.longitude)),
        Cesium.Math.toDegrees(Math.min(UL.longitude, UR.longitude, LR.longitude, LL.longitude))
    ];
}

function toHeadingPitchRange(heading, pitch, range) {
    return new Cesium.HeadingPitchRange(Cesium.Math.toRadians(heading), Cesium.Math.toRadians(pitch), range);
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

function adminLogin() {
  var adminEmailSpan = $("#avyAdminLoggedInEmail");
  return adminEmailSpan.length > 0 && adminEmailSpan.text().length > 0;
}

return AvyEyesView;
});