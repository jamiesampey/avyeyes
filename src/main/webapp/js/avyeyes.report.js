define(['avyeyes', 'lib/Cesium/Cesium', 'lib/jquery-ui'], function(AvyEyes, Cesium) {

function AvyReport(avyEyesView) {
	this.view = avyEyesView;
	this.drawingPolygon;
}

AvyReport.prototype.reserveExtId = function() {
	$.getJSON('/rest/reserveExtId', function(data) {
		$('#avyReportExtId').val(data.extId);
	})
	.fail(function(jqxhr, textStatus, error) {
		var err = textStatus + ", " + error;
	    console.log("Avy Eyes failed to reserve an ExtId for the report:" + err);
	    this.view.resetView();
	}.bind(this));
}

AvyReport.prototype.beginReport = function() {
	this.reserveExtId();
	AvyEyes.toggleTechnicalReportFields(false);
    $('#avyReportInitLocation').val('');
	$('#avyReportLocationDialog').dialog('open');
	$('#avyReportDrawButtonContainer').css('visibility', 'visible');
}

AvyReport.prototype.startDrawing = function() {
    $('#avyReportDrawButtonContainer').css('visibility', 'hidden');
    this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);

    var isDrawing = false;
    var lastRecordTime = 0;
    var cartesian3Array = [];
    var drawingPolyline;
    var drawingPolylineColor = Cesium.Color.RED;
    var drawingPolygonColor = Cesium.Color.RED.withAlpha(0.4);

    this.view.cesiumEventHandler.setInputAction(function(click) {
        if (isDrawing) {
            this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
            this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
            this.view.setAvySelectEventHandler();

            this.view.cesiumViewer.entities.remove(drawingPolyline);
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

            digestDrawing(cartesian3Array);
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
        if (!isDrawing || Cesium.getTimestamp() - lastRecordTime < 10) return; // min 10ms between record points

        var ray = this.view.cesiumViewer.camera.getPickRay(movement.endPosition);
        var cartesianPos = this.view.cesiumViewer.scene.globe.pick(ray, this.view.cesiumViewer.scene);
        if (Cesium.defined(cartesianPos)) {
            cartesian3Array.push(cartesianPos);
            lastRecordTime = Cesium.getTimestamp();
        }
    }.bind(this), Cesium.ScreenSpaceEventType.MOUSE_MOVE);
}

function digestDrawing(cartesian3Array) {
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

    AvyEyes.setReportDrawingInputs(Cesium.Math.toDegrees(highestCartographic.longitude).toFixed(8),
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
	AvyEyes.setReportDrawingInputs('', '', '', '', '', '');
	if (this.drawingPolygon) {
    	this.view.cesiumViewer.entities.remove(this.drawingPolygon);
    	this.drawingPolygon = null;
	}
}

AvyReport.prototype.enterReportDetails = function() {
    AvyEyes.resetReportImageUpload(this.view);
	$.ui.dialog.prototype._focusTabbable = function(){};
	$('#avyReportDetailsEntryDialog').dialog('open');
}

AvyReport.prototype.highlightErrorFields = function(errorFields) {
    AvyEyes.resetReportErrorFields();
    $.each(errorFields, function(i) {
        if (errorFields[i] === 'avyReportAngle') {
            $('#' + errorFields[i]).parent().css('border', '1px solid red');
        } else {
            $('#' + errorFields[i]).css('border', '1px solid red');
        }
    });
}

AvyReport.prototype.displayDetails = function(a) {
  $('#avyReportExtId').val(a.extId);
  this.wireImageUpload(this.view);
  
  $('#avyReportViewableTd').children(':checkbox').attr('checked', a.viewable);
  $('#avyReportViewableTd').children(':checkbox').trigger('change');
    
  $('#avyReportSubmitterEmail').val(a.submitterEmail);
  setAutocompleteVal('#avyReportSubmitterExp', a.submitterExp);
    
  $('#avyReportAreaName').val(a.areaName);
  $('#avyReportDate').val(a.avyDate);
  setAutocompleteVal('#avyReportSky', a.sky);
  setAutocompleteVal('#avyReportPrecip', a.precip);
    
  setAutocompleteVal('#avyReportType', a.avyType);
  setAutocompleteVal('#avyReportTrigger', a.avyTrigger);
  setAutocompleteVal('#avyReportInterface', a.avyInterface);
  setSliderVal('#avyReportRsizeValue', a.rSize);
  setSliderVal('#avyReportDsizeValue', a.dSize);
  
  $('#avyReportElevation').val(a.elevation);
  $('#avyReportElevationFt').val(AvyEyes.metersToFeet(a.elevation));
  setAutocompleteVal('#avyReportAspect', a.aspect);
  $('#avyReportAngle').val(a.angle);
  
  setSpinnerVal('#avyReportNumCaught', a.caught);
  setSpinnerVal('#avyReportNumPartiallyBuried', a.partiallyBuried);
  setSpinnerVal('#avyReportNumFullyBuried', a.fullyBuried);
  setSpinnerVal('#avyReportNumInjured', a.injured);
  setSpinnerVal('#avyReportNumKilled', a.killed);
  setAutocompleteVal('#avyReportModeOfTravel', a.modeOfTravel);
  
  $('#avyReportComments').val(a.comments);
  
  $.each(a.images, function(i) {
    var imgUrl = '/rest/images/' + a.extId + '/' + a.images[i].filename;
    $('#avyReportImageTable').append('<tr id="' + a.images[i].filename + '">' 
      + '<td><a href="' + imgUrl + '" target="_blank">' + a.images[i].filename 
      + '</a></td><td>' + AvyEyes.bytesToFileSize(a.images[i].size) + '<div class="avyReportImageDeleteWrapper">'
      + '<input type="button" value="Delete" onclick="avyEyesView.currentReport.deleteImage(\''
      + a.extId + '\',\'' + a.images[i].filename + '\')"/></div></td></tr>');
  });
  
  $('#avyReportDeleteBinding').val(a.extId);
  $('#avyReportDetailsEntryDialog').dialog('open');
}

AvyReport.prototype.deleteImage = function(extId, filename) {
  $.ajax({
    url: '/rest/images/' + extId + '/' + filename,
    type: 'DELETE',
    success: function(result) {
      $('#avyReportImageTable').find('#' + filename).remove();
    },
    fail: function(jqxhr, textStatus, error) {
      var err = textStatus + ", " + error;
      alert('Failed to delete ' + filename + '. Error: ' + err);
    }
  });  
}

function setAutocompleteVal(hiddenSibling, enumObj) {
  $(hiddenSibling).val(enumObj.value);
  $(hiddenSibling).siblings('.avyAutoComplete').val(enumObj.label);
}

function setSliderVal(inputElem, value) {
  $(inputElem).val(value);
  $(inputElem).siblings('.avyRDSlider').slider('value', value);
}

function setSpinnerVal(inputElem, value) {
  if (value == -1) {
    $(inputElem).val('');
  } else {
    $(inputElem).val(value);
  }
}

return AvyReport;
});