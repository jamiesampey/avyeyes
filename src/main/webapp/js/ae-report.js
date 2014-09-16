define(['ae-draw', 'jquery-ui'], function(AvyDraw) {

function AvyReport(avyEyesView) {
	this.view = avyEyesView;
	this.currentDrawing = null;
}

AvyReport.prototype.initAvyReport = function() {
	this.reserveExtId();
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
	var imgUploadUrl = '/rest/imgupload/' + $('#avyReportExtId').val();
	$("#imgUploadForm").fileupload({dataType:'json', url:imgUploadUrl, dropZone:$('#avyReportImgDropZone'),
        done: function(e, data) {
        	$('#avyReportFinishedImgsTable').show();
        	$('#avyReportFinishedImgsTable').append('<tr><td>' + data.result.fileName + '</td><td>' + data.result.fileSize + '</td></tr>');
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

return AvyReport;
});