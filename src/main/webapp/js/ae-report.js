define(['ae-draw', 'jquery-ui'], function(AvyDraw) {

function AvyReport(avyEyesView, submitReportCallback) {
	var self = this;
	var view = avyEyesView;
	var avyDraw = null;
	
	this.clearAvyDrawing = function() {
		if (avyDraw) {
			avyDraw.clearDrawing();
			avyDraw = null;
		}
		
		$('#avyReportLat').val('');
		$('#avyReportLng').val('');
		$('#avyReportSlopeEvelvation').val('');
		$('#avyReportSlopeAspect').val('');
		$('#avyReportSlopeAngle').val('');
		$('#avyReportKml').val('');
	}

	this.clearAllFields = function() {
		self.clearAvyDrawing();
		$('#avyReportDetailsDialog').find('input:text, input:hidden, textarea').val('');
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
	
	this.beginReportWithGeocode = function() {
		view.initialGeocodeForReport = true;
		view.geocodeAndFlyToLocation($('#avyReportInitLocation').val(), 8000.0, 65.0);
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
				    self.doAvyDraw();
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
		
		view.navControlBlink(3);
	}
	
	this.doAvyDraw = function() {
		avyDraw = new AvyDraw(view.getGE(),
			function(lat, lng, elev, aspect, angle, kmlStr) {
				$('#avyReportLat').val(lat);
				$('#avyReportLng').val(lng);
				$('#avyReportElevation').val(elev);
				$('#avyReportAspectAC').val(aspect);
				$('#avyReportAspect').val(aspect);
				$('#avyReportAngle').val(angle);
				$('#avyReportKml').val(kmlStr);
				
				self.confirmDrawing();
			});
		
		avyDraw.startAvyDraw();
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
			        self.doAvyDraw();
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