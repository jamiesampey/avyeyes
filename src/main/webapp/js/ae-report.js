define(['ae-draw', 'jquery-ui'], function(AvyDraw) {

function AvyReport(avyEyesView, submitReportCallback) {
	var self = this;
	var view = avyEyesView;
	
	this.currentDrawing = null;
	
	this.setAvyDrawingHiddenInputs = function(lat, lng, elev, aspect, angle, kmlStr) {
		$('#avyReportLat').val(lat);
		$('#avyReportLng').val(lng);
		$('#avyReportElevation').val(elev);
		$('#avyReportAspectAC').val(aspect);
		$('#avyReportAspect').val(aspect);
		$('#avyReportAngle').val(angle);
		$('#avyReportKml').val(kmlStr);
	}
	
	this.clearAvyDrawing = function() {
		if (self.currentDrawing) {
			self.currentDrawing.clearDrawing();
			self.currentDrawing = null;
		}
		self.setAvyDrawingHiddenInputs('', '', '', '', '', '');
	}

	this.clearAllFields = function() {
		self.clearAvyDrawing();
		$('#avyReportDetailsDialog').find('input:text, input:hidden, textarea').val('');
	}
	
	this.doAvyDrawing = function() {
		self.currentDrawing = new AvyDraw(view.getGEarth(), view.getGE(),
			function(lat, lng, elev, aspect, angle, kmlStr) {
				self.setAvyDrawingHiddenInputs(lat, lng, elev, aspect, angle, kmlStr);
				self.confirmDrawing();
			});
		
		self.currentDrawing.startAvyDraw();
	}
	
	this.beginReportWithGeocode = function() {
		view.initialGeocodeForReport = true;
		view.geocodeAndFlyToLocation($('#avyReportInitLocation').val(), 8000.0, 65.0);
	}
	
	this.initAvyReport = function() {
		view.hideSearchMenu();
	
		$('#avyReportInitLocation').keydown(function (e){    
			  if (e.keyCode == 13) {
			    $('#avyReportInitDialog').dialog('close');
			    if ($('#avyReportInitLocation').val()) {
			    	self.beginReportWithGeocode();
			    } else {
			    	self.beginReport();
			    }
			  }
		});
		
		$('#avyReportInitDialog').dialog({
			  minWidth: 500,
			  modal: true,
			  draggable: false,
			  closeOnEscape: false,
			  beforeclose: function (event, ui) { return false; },
			  dialogClass: "noclose",
			  buttons: [
			    {
			      text: "Begin Report",
			      click: function(event, ui) {
			    	$(this).dialog('close');
		    		if ($('#avyReportInitLocation').val()) {
		    			self.beginReportWithGeocode();
		    		} else {
		    			self.beginReport();
		    		}
			      }
			    },
			    {
			      text: "Cancel",
			      click: function(event, ui) {
			    	view.cancelReport();
			        $(this).dialog('close');
			      }
				}
			  ]
			});
	}
	
	this.beginReport = function() {
		$.ui.dialog.prototype._focusTabbable = function(){};
		$('#avyReportBeginDialog').dialog({
			  minWidth: 500,
			  modal: true,
			  draggable: false,
			  closeOnEscape: false,
			  beforeclose: function (event, ui) { return false; },
			  dialogClass: "noclose",
			  buttons: [
			    {
			      text: "Begin Drawing",
			      click: function(event, ui) {
				    $(this).dialog('close');
				    view.stopNavControlBlink();
				    self.doAvyDrawing();
			      }
			    },
			    {
			      text: "Cancel",
			      click: function(event, ui) {
			    	view.cancelReport();
			        $(this).dialog('close');
			      }
				}
			  ]
			});
		
		view.navControlBlink(50);
	}
	
	this.confirmDrawing = function() {
		$.ui.dialog.prototype._focusTabbable = function(){};
		$('#avyReportDrawConfirmDialog').dialog({
			  minWidth: 500,
			  modal: true,
			  draggable: true,
			  closeOnEscape: false,
			  beforeclose: function (event, ui) { return false; },
			  dialogClass: "noclose",
			  buttons: [
			    {
			      text: "Accept Drawing",
			      click: function(event, ui) {
				    $(this).dialog('close');
				    self.enterAvyDetail();
			      }
			    },
			    {
			      text: "Redraw",
			      click: function(event, ui) {
			        $(this).dialog('close');
			        self.clearAvyDrawing();
			        self.doAvyDrawing();
			      }
				}
			  ]
			});
	}
	
	this.enterAvyDetail = function() {
		$.ui.dialog.prototype._focusTabbable = function(){};
		$('#avyReportDetailsDialog').dialog({
			  minWidth: 750,
			  minHeight: 500,
			  modal: true,
			  draggable: false,
			  closeOnEscape: false,
			  beforeclose: function (event, ui) { return false; },
			  dialogClass: "noclose",
			  buttons: [
			    {
			      text: "Submit",
			      click: function(event, ui) {
			    	  submitReportCallback();
			    	  $(this).dialog('close');
			      }
			    },
			    {
			      text: "Cancel",
			      click: function(event, ui) {
			    	  view.cancelReport();
				      $(this).dialog('close');
			      }
				}
			  ]
	    });	
	}
}

return AvyReport;
});