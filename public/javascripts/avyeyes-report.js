define(['jqueryui'], function() {

function AvyReport(avyEyesView) {
	this.view = avyEyesView;
	this.drawingPolygon;
}

// AvyReport.prototype.reserveExtId = function() {
// 	$.getJSON('/avalanche/newReportId', function(data) {
// 		$("#rwAvyFormExtId").val(data.extId);
// 	}.bind(this)).fail(function(jqxhr, textStatus, error) {
// 	    console.error("AvyEyes failed to reserve a new report ID: " + textStatus + ", " + error);
// 	    this.view.resetView();
// 	}.bind(this));
// }
//
// AvyReport.prototype.beginReport = function() {
// 	this.reserveExtId();
//     $('#avyReportInitLocation').val('');
//     $('#avyReportStep1').show(function() {
//     	this.view.showControls('#aeControlsReportInstructions');
//     }.bind(this));
// }

// AvyReport.prototype.startDrawing = function() {
//     this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
//
//     $('#cesiumContainer').css('cursor','crosshair');
//
//     var isDrawing = false;
//     var cartesian3Array = [];
//     var drawingPolyline;
//     var drawingPolylineColor = Cesium.Color.RED;
//     var drawingPolygonColor = Cesium.Color.RED.withAlpha(0.4);
//
//     this.view.cesiumEventHandler.setInputAction(function(click) {
//         if (isDrawing) {
//             this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
//             this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
//             this.view.setAvyMouseEventHandlers();
//
//             $('#cesiumContainer').css('cursor','default');
//
//             this.drawingPolygon = this.view.addEntity({
//                 polygon: {
//                     hierarchy: {
//                         positions: cartesian3Array
//                     },
//                     perPositionHeight: true,
//                     material: drawingPolygonColor,
//                     outline: false
//                 }
//             });
//             this.view.removeEntity(drawingPolyline);
//
//             this.digestDrawing(cartesian3Array);
//
//             $("#avyReportStep3").hide("slide", {"direction":"down"}, 600, function() {
//                 $("#avyReportStep4").slideDown("slow");
//             });
//         } else {
//             drawingPolyline = this.view.addEntity({
//                 polyline: {
//                     positions: new Cesium.CallbackProperty(function() {
//                         return cartesian3Array;
//                     }, false),
//                     material: drawingPolylineColor,
//                     width: 3
//                 }
//             });
//         }
//
//         isDrawing = !isDrawing;
//     }.bind(this), Cesium.ScreenSpaceEventType.LEFT_CLICK);
//
//     this.view.cesiumEventHandler.setInputAction(function(movement) {
//         if (!isDrawing) return;
//
//         var ray = this.view.cesiumViewer.camera.getPickRay(movement.endPosition);
//         var cartesianPos = this.view.cesiumViewer.scene.globe.pick(ray, this.view.cesiumViewer.scene);
//
//         if (Cesium.defined(cartesianPos)) {
//             if (cartesian3Array.length == 0) {
//                 cartesian3Array.push(cartesianPos);
//             } else if (Cesium.Cartesian3.distance(cartesian3Array[cartesian3Array.length - 1], cartesianPos) > 4) {
//                 cartesian3Array.push(cartesianPos);
//             }
//         }
//     }.bind(this), Cesium.ScreenSpaceEventType.MOUSE_MOVE);
// }
//
// AvyReport.prototype.digestDrawing = function(cartesian3Array) {
//     var coordStr = "";
//     var highestCartesian;
//     var highestCartographic;
//     var lowestCartesian;
//     var lowestCartographic;
//
//     $.each(cartesian3Array, function(i, cartesianPos) {
//         var cartographicPos = Cesium.Ellipsoid.WGS84.cartesianToCartographic(cartesianPos);
//
//         if (!highestCartographic || cartographicPos.height > highestCartographic.height) {
//             highestCartesian = cartesianPos;
//             highestCartographic = cartographicPos;
//         }
//         if (!lowestCartographic || cartographicPos.height < lowestCartographic.height) {
//             lowestCartesian = cartesianPos;
//             lowestCartographic = cartographicPos;
//         }
//
//         coordStr += Cesium.Math.toDegrees(cartographicPos.longitude).toFixed(8)
//         + "," + Cesium.Math.toDegrees(cartographicPos.latitude).toFixed(8)
//         + "," + cartographicPos.height.toFixed(2) + " ";
//     });
//
//     var hypotenuse = Cesium.Cartesian3.distance(highestCartesian, lowestCartesian);
//     var opposite = highestCartographic.height - lowestCartographic.height;
//
//     this.view.form.setReportDrawingInputs(Cesium.Math.toDegrees(highestCartographic.longitude).toFixed(8),
//         Cesium.Math.toDegrees(highestCartographic.latitude).toFixed(8),
//         Math.round(highestCartographic.height),
//         getAspect(highestCartographic, lowestCartographic),
//         Math.round(Cesium.Math.toDegrees(Math.asin(opposite/hypotenuse))),
//         coordStr.trim());
// }

AvyReport.prototype.submitReport = function() {
    var view = this.view;
    if (!view.form.validateReportFields()) return;

    var extId = $("#rwAvyFormExtId").val();
    var reportSubmitUri = "/avalanche/" + extId + "?csrfToken=" + view.csrfTokenFromCookie();

    $.post(reportSubmitUri, JSON.stringify(parseReportForm(extId))).done(function(response) {
        view.showModalDialog("Avalanche report successfully submitted. The report is viewable at:<br/><br/><a href='"
            + response + "' target='_blank' style='text-decoration: none;'>" + response + "</a>");
    }).fail(function(jqxhr) {
        view.showModalDialog("Error submitting report " + extId + ". Error: " + jqxhr.responseText);
    }).always(function() {
        view.resetView();
    });
}

AvyReport.prototype.updateReport = function(editKey) {
    var view = this.view;
    if (!view.form.validateReportFields()) return;

    var extId = $("#rwAvyFormExtId").val();
    var reportUpdateUri = "/avalanche/" + extId + "?edit=" + editKey + "&csrfToken=" + view.csrfTokenFromCookie();

    $.ajax({ type: 'PUT', url: reportUpdateUri, data: JSON.stringify(parseReportForm(extId)) }).done(function() {
        view.showModalDialog("Avalanche report " + extId + " successfully updated");
    }).fail(function(jqxhr) {
        view.showModalDialog("Error updating report " + extId + ". Error: " + jqxhr.responseText);
    }).always(function() {
        view.resetView();
    });
}

AvyReport.prototype.deleteReport = function() {
    var view = this.view;
    var extId = $("#rwAvyFormExtId").val();

    $.ajax({
        type: 'DELETE',
        url: "/avalanche/" + extId + "?csrfToken=" + view.csrfTokenFromCookie()
    }).done(function() {
        view.showModalDialog("Avalanche report " + extId + " successfully deleted");
    }).fail(function(jqxhr, textStatus, errorThrown) {
        view.showModalDialog("Error deleting report " + extId + ". Error: " + errorThrown);
    }).always(function() {
        view.resetView();
    });
}

function parseReportForm(reportExtId) {
    var parseIntWithDefault = function(selector) {
        var attemptedInt = parseInt($(selector).val());
        return isNaN(attemptedInt) ? -1 : attemptedInt;
    }

    return {
        extId: reportExtId,
        viewable: $('#rwAvyFormViewable').is(":checked"),
        submitterEmail: $('#rwAvyFormSubmitterEmail').val(),
        submitterExp: $('#rwAvyFormSubmitterExp').val(),
        location: {
          longitude: parseFloat($('#rwAvyFormLng').val()),
          latitude: parseFloat($('#rwAvyFormLat').val()),
          altitude: parseFloat($('#rwAvyFormElevation').val())
        },
        date: $("#rwAvyFormDate").val(),
        areaName: $("#rwAvyFormAreaName").val(),
        slope: {
          aspect: $("#rwAvyFormAspect").val(),
          angle: parseInt($("#rwAvyFormAngle").val()),
          elevation: parseFloat($('#rwAvyFormElevation').val())
        },
        weather: {
          recentSnow: parseIntWithDefault("#rwAvyFormRecentSnow"),
          recentWindSpeed: $("#rwAvyFormRecentWindSpeed").val(),
          recentWindDirection: $("#rwAvyFormRecentWindDirection").val()
        },
        classification: {
          avyType: $("#rwAvyFormType").val(),
          trigger: $("#rwAvyFormTrigger").val(),
          triggerModifier: $("#rwAvyFormTriggerModifier").val(),
          interface: $("#rwAvyFormInterface").val(),
          rSize: parseFloat($("#rwAvyFormRsizeValue").val()),
          dSize: parseFloat($("#rwAvyFormDsizeValue").val())
        },
        humanNumbers: {
          modeOfTravel: $("#rwAvyFormModeOfTravel").val(),
          caught: parseIntWithDefault("#rwAvyFormNumCaught"),
          partiallyBuried: parseIntWithDefault("#rwAvyFormNumPartiallyBuried"),
          fullyBuried: parseIntWithDefault("#rwAvyFormNumFullyBuried"),
          injured: parseIntWithDefault("#rwAvyFormNumInjured"),
          killed: parseIntWithDefault("#rwAvyFormNumKilled")
        },
        perimeter: $("#rwAvyFormCoords").val(),
        comments: $("#rwAvyFormComments").val()
      };
}

// function getAspect(highestCartographic, lowestCartographic) {
//     var lat1 = highestCartographic.latitude;
//     var lat2 = lowestCartographic.latitude;
//     var dLon = lowestCartographic.longitude - highestCartographic.longitude;
//
//     var y = Math.sin(dLon) * Math.cos(lat2);
//     var x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
//     var heading = (Cesium.Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
//
// 	if (heading > 22.5 && heading <= 67.5) return "NE";
// 	if (heading > 67.5 && heading <= 112.5) return "E";
// 	if (heading > 112.5 && heading <= 157.5) return "SE";
// 	if (heading > 157.5 && heading <= 202.5) return "S";
// 	if (heading > 202.5 && heading <= 247.5) return "SW";
// 	if (heading > 247.5 && heading <= 292.5) return "W";
// 	if (heading > 292.5 && heading <= 337.5) return "NW";
// 	return "N";
// }

// AvyReport.prototype.clearDrawing = function() {
// 	this.view.form.setReportDrawingInputs('', '', '', '', '', '');
// 	if (this.drawingPolygon) {
//     	this.view.removeEntity(this.drawingPolygon);
//     	this.drawingPolygon = null;
// 	}
// }

return AvyReport;
});