define(['ae-draw', 'jquery-ui'], function(AvyDraw) {

function AvyReport(avyEyesView) {
	this.view = avyEyesView;
	this.currentDrawing = null;
}

AvyReport.prototype.beginReportWizard = function() {
	this.reserveExtId();
	this.toggleClassification(false);
	$('#avyReportGeocodeDialog').dialog('open');
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

AvyReport.prototype.clearAllFields = function() {
	$('#avyReportDialog').find('input:text, input:hidden, textarea').val('');
	$('#avyReportDialog').find('.avyRDSliderValue').val('0');
	$('#avyReportDialog').find('.avyRDSlider').slider('value', 0);
	$('#avyReportFinishedImgsTable > tbody').empty();
	$('#avyReportFinishedImgsTable').hide();
}

AvyReport.prototype.clearAvyDrawing = function() {
	if (this.currentDrawing) {
		this.currentDrawing.clearDrawing();
		this.currentDrawing = null;
	}
	this.setAvyDrawingHiddenInputs('', '', '', '', '', '');
}

AvyReport.prototype.doAvyDrawing = function() {
	this.currentDrawing = new AvyDraw(this);
	this.currentDrawing.startAvyDraw();
}

AvyReport.prototype.beginReportWithGeocode = function() {
	this.view.geocodeAndFlyToLocation($('#avyReportInitLocation').val(), 8000.0, 65.0);
	$('#avyReportInitLocation').val('');
	setTimeout((this.beginReport).bind(this), 5000);
}
	
AvyReport.prototype.beginReport = function() {
	$.ui.dialog.prototype._focusTabbable = function(){};
	$('#avyReportBeginDrawDialog').dialog('open');
	this.view.navControlBlink(50);
}
	
AvyReport.prototype.confirmDrawing = function() {
	$.ui.dialog.prototype._focusTabbable = function(){};
	$('#avyReportConfirmDrawDialog').dialog('open');
}
	
AvyReport.prototype.enterAvyDetail = function() {
	var imgUploadUrl = '/rest/images/' + $('#avyReportExtId').val();
	$("#avyReportImageUploadForm").fileupload({dataType:'json', url:imgUploadUrl, dropZone:$('#avyReportImageDropZone'),
        done: function(e, data) {
        	$('#avyReportImageTable').show();
        	$('#avyReportImageTable').append('<tr><td>' + data.result.fileName + '</td><td>' + data.result.fileSize + '</td></tr>');
        }
    });
	
	$.ui.dialog.prototype._focusTabbable = function(){};
	$('#avyReportDialog').dialog('open');
}

AvyReport.prototype.setAvyDrawingHiddenInputs = function(lat, lng, elev, aspect, angle, kmlStr) {
	$('#avyReportLat').val(lat);
	$('#avyReportLng').val(lng);
	$('#avyReportElevation').val(elev);
	$('#avyReportElevationFt').val(this.view.metersToFeet(elev));
	$('#avyReportAspectAC').val(aspect);
	$('#avyReportAspect').val(aspect);
	$('#avyReportAngle').val(angle);
	$('#avyReportKml').val(kmlStr);
}

AvyReport.prototype.toggleClassification = function(enabled) {
  if (enabled) {
    $('#avyReportClassification .avyHeader').css('color', 'white');
    $('#avyReportClassification label').css('color', 'white');
    $('#avyReportClassification .avyRDSliderValue').css('color', 'white');
    $('#avyReportClassification :input').prop('disabled', false);
    $('#avyReportClassification .avyRDSlider').slider('enable');
  } else {
    $('#avyReportClassification .avyHeader').css('color', 'gray');
    $('#avyReportClassification label').css('color', 'gray');
    $('#avyReportClassification .avyRDSliderValue').css('color', 'gray');
    $('#avyReportClassification :input').val('');
    $('#avyReportClassification :input').prop("disabled", true);
    $('#avyReportClassification .avyRDSlider').slider('disable');
    $('#avyReportClassification .avyRDSliderValue').val('0');
    $('#avyReportClassification .avyRDSlider').slider('value', 0);
  }
};

AvyReport.prototype.displayDetails = function(a) {
  $('#avyReportExtId').val(a.extId);

  $('#avyReportViewableTd').children(':checkbox').attr('checked', a.viewable);
  $('#avyReportViewableTd').children(':checkbox').trigger('change');
    
  $('#avyReportSubmitterEmail').val(a.submitterEmail);
  this.setAutocomplete('#avyReportSubmitterExp', a.submitterExp);
    
  $('#avyReportAreaName').val(a.areaName);
  $('#avyReportDate').val(a.avyDate);
  this.setAutocomplete('#avyReportSky', a.sky);
  this.setAutocomplete('#avyReportPrecip', a.precip);
    
  this.setAutocomplete('#avyReportType', a.avyType);
  this.setAutocomplete('#avyReportTrigger', a.trigger);
  this.setAutocomplete('#avyReportBedSurface', a.bedSurface);
  this.setSlider('#avyReportRsizeValue', a.rSize);
  this.setSlider('#avyReportDsizeValue', a.dSize);
  
  $('#avyReportElevation').val(a.elevation);
  $('#avyReportElevationFt').val(this.view.metersToFeet(a.elevation));
  this.setAutocomplete('#avyReportAspect', a.aspect);
  $('#avyReportAngle').val(a.angle);
  
  this.setSpinner('#avyReportNumCaught', a.caught);
  this.setSpinner('#avyReportNumPartiallyBuried', a.partiallyBuried);
  this.setSpinner('#avyReportNumFullyBuried', a.fullyBuried);
  this.setSpinner('#avyReportNumInjured', a.injured);
  this.setSpinner('#avyReportNumKilled', a.killed);
  this.setAutocomplete('#avyReportModeOfTravel', a.modeOfTravel);
  
  $('#avyReportComments').val(a.comments);
  
  $.each(a.images, function(i) {
    var imgUrl = '/rest/images/' + a.extId + '/' + a.images[i].filename;
    $('#avyReportImageTable').append('<tr><td><a href="' + imgUrl + '" target="_blank">'
      + a.images[i].filename + '</a></td><td>' + a.images[i].size + '</td></tr>');
  });
  
  $('#avyReportDeleteBinding').val(a.extId);
  $('#avyReportDialog').dialog('open');
}

AvyReport.prototype.setAutocomplete = function(hiddenSibling, enumObj) {
  $(hiddenSibling).val(enumObj.value);
  $(hiddenSibling).siblings('.avyAutoComplete').val(enumObj.label);
}

AvyReport.prototype.setSlider = function(inputElem, value) {
  $(inputElem).val(value);
  $(inputElem).siblings('.avyRDSlider').slider('value', value);
}

AvyReport.prototype.setSpinner = function(inputElem, value) {
  if (value == -1) {
    $(inputElem).val('');
  } else {
    $(inputElem).val(value);
  }
}

return AvyReport;
});