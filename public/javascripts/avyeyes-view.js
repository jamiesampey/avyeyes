define(['avyeyes-ui',
        'avyeyes-form',
        'avyeyes-report',
        'notify'
        ], function(AvyEyesUI, AvyForm, AvyReport) {

function AvyEyesView(socialMode) {
    this.socialEnabled = socialMode;
    console.info("Starting AvyEyes view. Social plugins loaded == " + socialMode);

    // this.bingKey = "AiXcgClqr_8DxjhvM5bal45QdMumBNOllccwdibv5ViVRKR1xTh9iA5GugmmINPr";
    // this.facebookAppId = "541063359326610";
    //
    // this.cesiumViewer = new Cesium.Viewer("cesiumContainer", {
    //     sceneMode: Cesium.SceneMode.SCENE3D,
    //     imageryProvider: new Cesium.BingMapsImageryProvider({
    //         url: "//dev.virtualearth.net",
    //         key: this.bingKey,
    //         mapStyle: Cesium.BingMapsStyle.AERIAL_WITH_LABELS
    //     }),
    //     terrainProvider: new Cesium.CesiumTerrainProvider({
    //         url: "//assets.agi.com/stk-terrain/world",
    //         requestVertexNormals: true
    //     }),
    //     contextOptions: {
    //         webgl: { preserveDrawingBuffer: true }
    //     },
    //     animation: false,
    //     baseLayerPicker: false,
    //     fullscreenButton: true,
    //     geocoder: false,
    //     homeButton: false,
    //     infoBox: false,
    //     sceneModePicker: false,
    //     selectionIndicator: false,
    //     timeline: false,
    //     navigationHelpButton: true,
    //     navigationInstructionsInitiallyVisible: false,
    //     scene3DOnly: true,
    //     shadows: false,
    //     terrainShadows: false
    // });
    //
    // this.setCameraMoveEventListener();
    // this.cesiumEventHandler = new Cesium.ScreenSpaceEventHandler(this.cesiumViewer.scene.canvas);
    this.setAvyMouseEventHandlers();

    window.addEventListener("keydown", function() {
        $("#helpOverlay").hide();
    }, false);

    this.form = new AvyForm(this);
    this.ui = new AvyEyesUI(this);

    if (this.socialEnabled) {
        FB.init({
            appId: this.facebookAppId,
            xfbml: true,
            version: "v2.10"
        });
    }

    this.clickPathClueShown = false;
    this.avalancheSpotlight = false;

    this.ui.loaded.then(function() {
        console.log("AvyEyes UI is wired");
        if (initAvalanche) {
            this.avalancheSpotlight = true;
            console.debug("Flying to avalanche " + initAvalanche.extId);
            this.addAvalancheAndFlyTo(initAvalanche);
        } else {
            console.debug("Geolocating user");
            this.geolocateAndFlyTo();
        }
    }.bind(this));
}

// AvyEyesView.prototype.setCameraMoveEventListener = function() {
//     var eyeAltSetter = {};
//     this.cesiumViewer.camera.moveStart.addEventListener(function() {
//         eyeAltSetter = setInterval(function() {
//             $("#eyeAltContainer").html("eye alt: " + this.camDisplayAlt());
//         }.bind(this), 10);
//     }.bind(this));
//     this.cesiumViewer.camera.moveEnd.addEventListener(function() {
//         clearInterval(eyeAltSetter);
//         if (!this.currentReport && !this.avalancheSpotlight) {
//             $('#avyFilterButton').trigger('click');
//         }
//     }.bind(this));
// }

AvyEyesView.prototype.setAvyMouseEventHandlers = function() {
    this.cesiumEventHandler.setInputAction(function(movement) {
        if ($("#cesiumContainer").css("cursor") === "wait") return; // in the process of opening a report

        $("#avyMouseHoverTitle").hide();
        var pick = this.cesiumViewer.scene.pick(movement.endPosition);

        if (Cesium.defined(pick) && pick.id.name) {
            $("#avyMouseHoverTitle").text(pick.id.name);
            $("#avyMouseHoverTitle").css({
                "left": movement.endPosition.x + 12,
                "top": movement.endPosition.y + 10
            });
            $("#avyMouseHoverTitle").show();
            $("#cesiumContainer").css("cursor", "pointer");
        } else {
            $("#cesiumContainer").css("cursor", "default");
        }
    }.bind(this), Cesium.ScreenSpaceEventType.MOUSE_MOVE);

    this.cesiumEventHandler.setInputAction(function(movement) {
        this.form.hideReadOnlyForm();

        var pick = this.cesiumViewer.scene.pick(movement.position);
        if (Cesium.defined(pick) && pick.id.name) {
            $("#avyMouseHoverTitle").hide();

            var selectedAvalanche = pick.id;
            var avalancheUrl = "/avalanche/" + selectedAvalanche.id;
            var editKeyParam = this.getRequestParam("edit");
            if (editKeyParam) avalancheUrl += "?edit=" + editKeyParam;

            $.getJSON(avalancheUrl, function(data) {
                if (pick.id.billboard) {
                    // click on a pin, add the path and fly to it
                    this.removeAllEntities();
                    this.avalancheSpotlight = true;
                    this.addAvalancheAndFlyTo(data);
                } else {
                    // click on a path, display details
                    $("#cesiumContainer").css("cursor", "wait");
                    if (data.hasOwnProperty("viewable")) {
                        this.form.enableAdminControls().then(function () {
                            this.form.displayReadWriteForm(data);
                            this.currentReport = new AvyReport(this);
                        }.bind(this));
                    } else if (data.hasOwnProperty("submitterEmail")) {
                        this.form.displayReadWriteForm(data);
                        this.currentReport = new AvyReport(this);
                    } else {
                        this.form.displayReadOnlyForm(movement.position, data);
                    }
                }
            }.bind(this)).fail(function(jqxhr, textStatus, error) {
                console.log("AvyEyes error: " + error);
            });
        }
    }.bind(this), Cesium.ScreenSpaceEventType.LEFT_CLICK);
}

// AvyEyesView.prototype.addEntity = function(entity) {
//   entity.createdBy = "AvyEyes";
//   return this.cesiumViewer.entities.add(entity);
// }

// AvyEyesView.prototype.removeEntity = function(entity) {
//   return this.cesiumViewer.entities.remove(entity);
// }

AvyEyesView.prototype.removeAllEntities = function() {
    var entitiesToRemove = [];
    $.each(this.cesiumViewer.entities.values, function(idx, entity) {
      if (entity.createdBy && entity.createdBy === "AvyEyes") entitiesToRemove.push(entity);
    });

    $.each(entitiesToRemove, function(idx, entity) { this.removeEntity(entity); }.bind(this));
}

AvyEyesView.prototype.showControls = function(divId) {
    var controlContentDiv = typeof divId == "undefined" ? "#aeControlsSearchForm" : divId;
    if ($(controlContentDiv).css("display") == "none") {
        return this.hideControls().then(function() {
            return $(controlContentDiv).slideDown("slow").promise();
        });
    } else {
        return Promise.resolve();
    }
}

AvyEyesView.prototype.hideControls = function() {
    return $('#aeControlsSearchForm, #aeControlsReportInstructions').slideUp("slow").promise();
}

AvyEyesView.prototype.showModalDialog = function(msg) {
    $("#multiDialog").html(msg);
    $("#multiDialog").dialog("option", "title", "Info");
    $("#multiDialog").dialog("open");
}

AvyEyesView.prototype.showHelp = function(tab) {
	$("#helpOverlayText").tabs("option", "active", tab);
	$("#helpOverlay").show();
}

AvyEyesView.prototype.doReport = function() {
	this.removeAllEntities();
	this.cancelReport();
	this.currentReport = new AvyReport(this);
	this.currentReport.beginReport();
    window.onbeforeunload = function(e) {
        return "Report in progress!";
    };
}

AvyEyesView.prototype.cancelReport = function() {
    $(".reportInstructions").hide();
    this.form.closeReportForm();
    this.form.clearReportFields();
	this.currentReport = null;
	window.onbeforeunload = null;
}

AvyEyesView.prototype.resetView = function() {
	this.removeAllEntities();
	this.cancelReport();
	this.showControls();
}

// AvyEyesView.prototype.addAvalanches = function(avalancheArray) {
//     var oldAvalancheIds = this.cesiumViewer.entities.values.map(function(entity) {
//         return entity.id;
//     });
//     var newAvalancheIds = avalancheArray.map(function(a) {
//        return a.extId;
//     });
//
//     // remove any old avalanches that do not exist in the new set
//     $.each(oldAvalancheIds.filter(function(oldId) {
//         return newAvalancheIds.indexOf(oldId) < 0;
//     }), function(i, oldIdToRemove) {
//         this.cesiumViewer.entities.removeById(oldIdToRemove);
//     }.bind(this));
//
//     // does the entity need to be updated? I.e. pin->path or path->pin?
//     var updateEntity = function(entity, avalanche) {
//         var noUpdateNeeded = (entity && entity.polygon && avalanche.coords)
//             || (entity && entity.billboard && avalanche.location);
//         return !noUpdateNeeded;
//     };
//
//     $.each(avalancheArray, function(i, a) {
//         var existingEntity = this.cesiumViewer.entities.getById(a.extId);
//         if (updateEntity(existingEntity, a)) {
//             this.removeEntity(existingEntity);
//             this.addAvalanche(a);
//             if (!this.clickPathClueShown && a.coords) {
//                 showClickPathClue("Click on any red avalanche path for the details");
//                 this.clickPathClueShown = true;
//             }
//         }
//     }.bind(this));
// }

// AvyEyesView.prototype.addAvalanche = function(a) {
//     var getEntity = function() {
//         if (a.coords) {
//             return {
//                 id: a.extId,
//                 name: a.title,
//                 polygon: {
//                     material: Cesium.Color.RED.withAlpha(0.4),
//                     hierarchy: Cesium.Cartesian3.fromDegreesArrayHeights(a.coords),
//                     heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
//                 }
//             };
//         } else {
//             return {
//                 id: a.extId,
//                 name: a.title,
//                 position: Cesium.Cartesian3.fromDegrees(a.location.longitude, a.location.latitude, a.location.altitude),
//                 billboard: { image: "/assets/images/poi-pin.png" }
//             };
//         }
//     };
//
//     this.addEntity(getEntity());
// }

AvyEyesView.prototype.addAvalancheAndFlyTo = function(a) {
    this.addAvalanche(a);
    var boundingSphere = Cesium.BoundingSphere.fromPoints(Cesium.Cartesian3.fromDegreesArrayHeights(a.coords));
    this.ui.raiseTheCurtain();

    this.cesiumViewer.camera.flyToBoundingSphere(boundingSphere, {
        duration: 4.0,
        offset: toHeadingPitchRange(0, -89.9, 4000),
        complete: function() {
            this.cesiumViewer.camera.flyToBoundingSphere(boundingSphere, {
                duration: 4.0,
                offset: toHeadingPitchRange(flyToHeadingFromAspect(a.slope.aspect), -25, 1200),
                complete: function() {
                    showClickPathClue("Click on the red avalanche path for the details").then(function() {
                        showCesiumHelpClue();
                    });
                    this.clickPathClueShown = true;
                    setTimeout(function() {
                        this.avalancheSpotlight = false;
                    }.bind(this), 5000);
                }.bind(this)
        });}.bind(this)
    });
}

AvyEyesView.prototype.geolocateAndFlyTo = function() {
    var heading = 0.0;
    var pitch = -89.9; // work around -90 degree problem in flyToBoundingSphere
    var range = 1000000;

    var finishUp = function(flyToEntity) {
        this.removeEntity(flyToEntity);
        this.showControls().then(function() {
            showZoomInClue().then(function() {
                showCesiumHelpClue();
            })
        });
    }.bind(this);

    var flyToDefaultView = function() {
        this.ui.raiseTheCurtain();
        var defaultTarget = this.targetEntityFromCoords(-105.5, 39.0); // colorado
        this.flyTo(defaultTarget, heading, pitch, range).then(function() {
            finishUp(defaultTarget);
        }.bind(this));
    }.bind(this)

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(pos) {
            this.ui.raiseTheCurtain();
            var geolocatedTarget = this.targetEntityFromCoords(pos.coords.longitude, pos.coords.latitude);
            this.flyTo(geolocatedTarget, 0.0, pitch, range).then(function() {
                finishUp(geolocatedTarget);
            }.bind(this));
        }.bind(this), flyToDefaultView, {timeout:8000, enableHighAccuracy:false});
    } else {
        flyToDefaultView();
    }
}

var geocodeAttempts = 0;
AvyEyesView.prototype.geocodeAndFlyTo = function(address, pitch, range) {
    if (!address) return;

    geocodeAttempts++;

    var geocodeFailure = function() {
        geocodeAttempts = 0;
        this.showModalDialog("Failed to geocode '" + address + "'");
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
        var geocodedTarget = this.targetEntityFromCoords(geocodePoints.coordinates[1], geocodePoints.coordinates[0]);
        this.flyTo(geocodedTarget, 0.0, pitch, range).then(function() {
            this.removeEntity(geocodedTarget);
        }.bind(this));
        geocodeAttempts = 0;
    }.bind(this), geocodeFailure);
}

AvyEyesView.prototype.geocode = function(address, onSuccess, onFailure) {
    if (!address) return;

    var boundingBox = this.getBoundingBox();
    var dataObj = { key: this.bingKey, q: address };
    if (boundingBox[0] && boundingBox[1] && boundingBox[2] && boundingBox[3]) {
        dataObj.umv = boundingBox[1] + "," + boundingBox[3] + "," + boundingBox[0] + "," + boundingBox[2];
    }

    $.ajax({
        url: "//dev.virtualearth.net/REST/v1/Locations",
        dataType: "jsonp",
        data: dataObj,
        jsonp: "jsonp",
        success: function(data) {
            onSuccess(data);
        },
        error: function(e) {
            onFailure(e);
        }
    });
}

// AvyEyesView.prototype.targetEntityFromCoords = function(lng, lat) {
//     var alt = this.cesiumViewer.scene.globe.getHeight(Cesium.Cartographic.fromDegrees(lng, lat));
//
//     return this.addEntity({
//         position: Cesium.Cartesian3.fromDegrees(lng, lat, alt),
//         billboard: {image: "/assets/images/flyto-pixel.png"}
//     });
// }
//
// AvyEyesView.prototype.flyTo = function(targetEntity, heading, pitch, range) {
//     return this.cesiumViewer.flyTo(targetEntity, {
//         duration: 4.0,
//         offset: toHeadingPitchRange(heading, pitch, range)
//     });
// }

// AvyEyesView.prototype.camDisplayAlt = function() {
//     var meters = this.cesiumViewer.camera.positionCartographic.height.toFixed(0);
//
//     if (meters > 10000) {
//         return (meters / 1000).toFixed(2) + " km";
//     } else {
//         return meters + " meters";
//     }
// }

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
    var LR = getCoordsAtWindowPos(this.cesiumViewer.canvas.clientWidth, this._viewer.canvas.clientHeight);
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

AvyEyesView.prototype.getRequestParam = function(paramName) {
  paramName = paramName.replace(/[\[\]]/g, "\\$&");
  var regex = new RegExp("[?&]" + paramName + "(=([^&#]*)|&|#|$)"),
      results = regex.exec(window.location.href);
  if (!results) return null;
  if (!results[2]) return '';
  return decodeURIComponent(results[2].replace(/\+/g, " "));
}

AvyEyesView.prototype.uploadCesiumScreenshot = function() {
    var base64ImageContent = this.cesiumViewer.canvas.toDataURL("image/jpeg", 0.8).replace(/^data:image\/jpeg;base64,/, "");
    var sliceSize = 1024;
    var byteChars = window.atob(base64ImageContent);
    var byteArrays = [];

    for (var offset = 0, len = byteChars.length; offset < len; offset += sliceSize) {
        var slice = byteChars.slice(offset, offset + sliceSize);

        var byteNumbers = new Array(slice.length);
        for (var i = 0; i < slice.length; i++) {
            byteNumbers[i] = slice.charCodeAt(i);
        }

        byteArrays.push(new Uint8Array(byteNumbers));
    }

    var formData = new FormData();
    formData.append("screenshot", new Blob(byteArrays, {type: 'image/jpeg'}));

    $.ajax({
        url: "/avalanche/" + $('#rwAvyFormExtId').val() + "/images/screenshot?csrfToken=" + this.csrfTokenFromCookie(),
        type: "POST",
        cache: false,
        contentType: false,
        processData: false,
        data: formData
    }).fail(function(jqxhr, textStatus, error) {
        var err = textStatus + ", " + error;
        console.log("AvyEyes screenshot upload error: " + err);
    });
}

AvyEyesView.prototype.csrfTokenFromCookie = function() {
  var docCookie = "; " + document.cookie;
  var parts = docCookie.split("; csrfToken=");
  if (parts.length === 2) return parts.pop().split(";").shift();
}

function showZoomInClue() {
    return showCenterTopClue("Click on an avalanche pin to zoom in, or zoom in manually");
}

function showClickPathClue(text) {
    return showCenterTopClue(text);
}

function showCenterTopClue(text) {
    var delay = 6000;
    var showDuration = 400;
    var hideDuration = 200;
    return new Promise(function(resolve) {
        $('#topCenterMsgDiv').notify(text, {
            clickToHide: true,
            autoHide: true,
            autoHideDelay: delay,
            arrowShow: false,
            arrowSize: 10,
            position: 'bottom center',
            style: 'bootstrap',
            className: 'info',
            showAnimation: 'slideDown',
            showDuration: showDuration,
            hideAnimation: 'slideUp',
            hideDuration: hideDuration,
            gap: 0
        });

        setTimeout(function() {
            resolve();
        }, delay + showDuration + hideDuration);
    });
}

function showCesiumHelpClue() {
    var delay = 6000;
    var showDuration = 400;
    var hideDuration = 200;
    return new Promise(function(resolve) {
        $('button.cesium-navigation-help-button').notify(
            "Click the ? button for help using the 3D map view", {
                clickToHide: true,
                autoHide: true,
                autoHideDelay: delay,
                arrowShow: true,
                arrowSize: 5,
                position: 'bottom right',
                style: 'bootstrap',
                className: 'info',
                showAnimation: 'slideDown',
                showDuration: showDuration,
                hideAnimation: 'slideUp',
                hideDuration: hideDuration,
                gap: 2
            });

        setTimeout(function() {
            resolve();
        }, delay + showDuration + hideDuration);
    });
}

// function toHeadingPitchRange(heading, pitch, range) {
//     return new Cesium.HeadingPitchRange(Cesium.Math.toRadians(heading), Cesium.Math.toRadians(pitch), range);
// }

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