define(['lib/jquery-ui'], function() {

function AvyReport(avyEyesView) {
	this.view = avyEyesView;
	this.drawingPolygon;
}

AvyReport.prototype.reserveExtId = function() {
	$.getJSON('/rest/reserveExtId', function(data) {
		$('#rwAvyFormExtId').val(data.extId);
	})
	.fail(function(jqxhr, textStatus, error) {
		var err = textStatus + ", " + error;
	    console.log("AvyEyes failed to reserve an ExtId for the report:" + err);
	    this.view.resetView();
	}.bind(this));
}

AvyReport.prototype.beginReport = function() {
	this.reserveExtId();
    $('#avyReportInitLocation').val('');
	$('#avyReportLocationDialog').dialog('open');
	$('#avyReportDrawButtonContainer').css('visibility', 'visible');
}

AvyReport.prototype.startDrawing = function() {
    $('#avyReportDrawButtonContainer').css('visibility', 'hidden');
    this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);

    $('#cesiumContainer').css('cursor','crosshair');

    var isDrawing = false;
    var cartesian3Array = [];
    var drawingPolyline;
    var drawingPolylineColor = Cesium.Color.RED;
    var drawingPolygonColor = Cesium.Color.RED.withAlpha(0.4);

    this.view.cesiumEventHandler.setInputAction(function(click) {
        if (isDrawing) {
            this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
            this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
            this.view.setAvySelectEventHandler();

            $('#cesiumContainer').css('cursor','default');

            this.drawingPolygon = this.view.cesiumViewer.entities.add({
                polygon: {
                    hierarchy: {
                        positions: cartesian3Array
                    },
                    perPositionHeight: true,
                    material: drawingPolygonColor,
                    outline: true
                }
            });
            this.view.cesiumViewer.entities.remove(drawingPolyline);

            this.digestDrawing(cartesian3Array);
            $.ui.dialog.prototype._focusTabbable = function(){};
           	$('#avyReportDrawingConfirmationDialog').dialog('open');
        } else {
            drawingPolyline = this.view.cesiumViewer.entities.add({
                polyline: {
                    positions: new Cesium.CallbackProperty(function() {
                        return cartesian3Array;
                    }, false),
                    material: drawingPolylineColor,
                    width: 3
                }
            });
        }

        isDrawing = !isDrawing;
    }.bind(this), Cesium.ScreenSpaceEventType.LEFT_CLICK);

    this.view.cesiumEventHandler.setInputAction(function(movement) {
        if (!isDrawing) return;

        var ray = this.view.cesiumViewer.camera.getPickRay(movement.endPosition);
        var cartesianPos = this.view.cesiumViewer.scene.globe.pick(ray, this.view.cesiumViewer.scene);

        if (Cesium.defined(cartesianPos)) {
            if (cartesian3Array.length == 0) {
                cartesian3Array.push(cartesianPos);
            } else if (Cesium.Cartesian3.distance(cartesian3Array[cartesian3Array.length - 1], cartesianPos) > 4) {
                cartesian3Array.push(cartesianPos);
            }
        }
    }.bind(this), Cesium.ScreenSpaceEventType.MOUSE_MOVE);
}

AvyReport.prototype.digestDrawing = function(cartesian3Array) {
    var coordStr = "";
    var highestCartesian;
    var highestCartographic;
    var lowestCartesian;
    var lowestCartographic;

    $.each(cartesian3Array, function(i, cartesianPos) {
        var cartographicPos = Cesium.Ellipsoid.WGS84.cartesianToCartographic(cartesianPos);

        if (!highestCartographic || cartographicPos.height > highestCartographic.height) {
            highestCartesian = cartesianPos;
            highestCartographic = cartographicPos;
        }
        if (!lowestCartographic || cartographicPos.height < lowestCartographic.height) {
            lowestCartesian = cartesianPos;
            lowestCartographic = cartographicPos;
        }

        coordStr += Cesium.Math.toDegrees(cartographicPos.longitude).toFixed(8)
        + "," + Cesium.Math.toDegrees(cartographicPos.latitude).toFixed(8)
        + "," + cartographicPos.height.toFixed(2) + " ";
    });

    var hypotenuse = Cesium.Cartesian3.distance(highestCartesian, lowestCartesian);
    var opposite = highestCartographic.height - lowestCartographic.height;

    this.view.form.setReportDrawingInputs(Cesium.Math.toDegrees(highestCartographic.longitude).toFixed(8),
        Cesium.Math.toDegrees(highestCartographic.latitude).toFixed(8),
        Math.round(highestCartographic.height),
        getAspect(highestCartographic, lowestCartographic),
        Math.round(Cesium.Math.toDegrees(Math.asin(opposite/hypotenuse))),
        coordStr.trim());
}

function getAspect(highestCartographic, lowestCartographic) {
    var lat1 = highestCartographic.latitude;
    var lat2 = lowestCartographic.latitude;
    var dLon = lowestCartographic.longitude - highestCartographic.longitude;

    var y = Math.sin(dLon) * Math.cos(lat2);
    var x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
    var heading = (Cesium.Math.toDegrees(Math.atan2(y, x)) + 360) % 360;

	if (heading > 22.5 && heading <= 67.5) return "NE";
	if (heading > 67.5 && heading <= 112.5) return "E";
	if (heading > 112.5 && heading <= 157.5) return "SE";
	if (heading > 157.5 && heading <= 202.5) return "S";
	if (heading > 202.5 && heading <= 247.5) return "SW";
	if (heading > 247.5 && heading <= 292.5) return "W";
	if (heading > 292.5 && heading <= 337.5) return "NW";
	return "N";
}

AvyReport.prototype.clearDrawing = function() {
	this.view.form.setReportDrawingInputs('', '', '', '', '', '');
	if (this.drawingPolygon) {
    	this.view.cesiumViewer.entities.remove(this.drawingPolygon);
    	this.drawingPolygon = null;
	}
}

AvyReport.prototype.highlightErrorFields = function(errorFields) {
    this.view.form.highlightReportErrorFields(errorFields);
}

return AvyReport;
});