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
    $('avyReportDrawButtonContainer').css('visibility', 'hidden');
    this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);

    var isDrawing = false;
    var lastRecordTime = 0;
    var cartesian3Array = [];
    var drawingPolyline;
    var drawingPolylineColor = Cesium.Color.RED;
    var drawingPolygonColor = Cesium.Color.RED.withAlpha(0.4);

    this.view.cesiumEventHandler.setInputAction(function(click) {
        if (isDrawing) {
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
            this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
            this.view.cesiumEventHandler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
            this.view.setAvySelectEventHandler();

            this.digestDrawing(cartesian3Array);
            this.confirmDrawing();
        } else {
            drawingPolyline = this.view.cesiumViewer.entities.add({
                drawingPolyline: {
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

AvyReport.prototype.clearDrawing = function() {
	setDrawingInputs('', '', '', '', '', '');
	if (this.drawingPolygon) {
    	this.cesiumViewer.entities.remove(this.drawingPolygon);
    	this.drawingPolygon = null;
	}
}

AvyReport.prototype.confirmDrawing = function() {
	$.ui.dialog.prototype._focusTabbable = function(){};
	$('#avyReportDrawingConfirmationDialog').dialog('open');
}

AvyReport.prototype.enterAvyDetail = function() {
    this.wireImageUpload(this.view);
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

AvyReport.prototype.finishReport = function() {
    $('#avyReportDetailsEntryDialog').dialog('close');
    this.view.resetView();
}

AvyReport.prototype.digestDrawing = function(cartesian3Array) {
    var coordStr = "";
    var highestAltitude = 0;
    var highestCartesian = cartesian3Array[0];
    var highestCartographic;
    var lowestAltitude = 9000;
    var lowestCartesian = cartesian3Array[0];
    var lowestCartographic;

    $.each(cartesian3Array, function(i, cartesianPos) {
        var cartographicPos = Cesium.Ellipsoid.WGS84.cartesianToCartographic(cartesianPos);

        if (cartographicPos.height > highestAltitude) {
            highestAltitude = cartographicPos.height;
            highestCartesian = cartesianPos;
            highestCartographic = cartographicPos;
        }
        if (cartographicPos.height < lowestAltitude) {
            lowestAltitude = cartographicPos.height;
            lowestCartesian = cartesianPos;
            lowestCartographic = cartographicPos;
        }

        coordStr += Cesium.Math.toDegrees(cartographicPos.longitude).toFixed(8)
        + "," + Cesium.Math.toDegrees(cartographicPos.latitude).toFixed(8)
        + "," + cartographicPos.height.toFixed(2) + " ";
    });

    var elevation = Math.round(highestAltitude);
    var aspect = getAspect(highestCartesian, lowestCartesian);

    var hDist = highestCartesian.distance(lowestCartesian);
    var vDist = highestCartographic.height - lowestCartographic.height;
    var angle = Cesium.Math.toDegrees(Math.asin(vDist/hDist));

    setDrawingInputs(highestCartographic.longitude, highestCartographic.latitude,
        elevation, aspect, angle, coordStr.trim());
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

AvyReport.prototype.wireImageUpload = function(view) {
  $('#avyReportImageTable > tbody').empty();

  var imgUploadUrl = '/rest/images/' + $('#avyReportExtId').val();
  $("#avyReportImageUploadForm").fileupload({dataType:'json', url:imgUploadUrl, dropZone:$('#avyReportImageDropZone'),
      fail: function(e, data) {
        view.showModalDialog("Error", data.errorThrown);
      },
      done: function(e, data) {
        $('#avyReportImageTable').append('<tr><td>' + data.result.fileName + '</td><td>'
          + AvyEyes.bytesToFileSize(data.result.fileSize) + '</td></tr>');
      }
  });
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

function getAspect(highestCartesian, lowestCartesian) {
    var y = Math.sin(lowestCartesian.x - highestCartesian.x) * Math.cos(lowestCartesian.y);
    var x = Math.cos(highestCartesian.y) * Math.sin(lowestCartesian.y) -
        Math.sin(highestCartesian.y) * Math.cos(lowestCartesian.y) * Math.cos(lowestCartesian.x - highestCartesian.x);
    var heading = Math.atan2(y, x).toDegrees().toFixed(1);

	if (heading > 22.5 && heading <= 67.5) return "NE";
	if (heading > 67.5 && heading <= 112.5) return "E";
	if (heading > 112.5 && heading <= 157.5) return "SE";
	if (heading > 157.5 && heading <= 202.5) return "S";
	if (heading > 202.5 && heading <= 247.5) return "SW";
	if (heading > 247.5 && heading <= 292.5) return "W";
	if (heading > 292.5 && heading <= 337.5) return "NW";
	return "N";
}

function setDrawingInputs(lng, lat, elevation, aspect, angle, coordStr) {
	$('#avyReportLng').val(lng);
	$('#avyReportLat').val(lat);
	$('#avyReportElevation').val(elevation);
	$('#avyReportElevationFt').val(AvyEyes.metersToFeet(elevation));
	$('#avyReportAspectAC').val(aspect);
	$('#avyReportAspect').val(aspect);
	$('#avyReportAngle').val(angle);
	$('#avyReportCoords').val(coordStr);
}

return AvyReport;
});