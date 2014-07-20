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
		$('#avyReportDialog').find('input:text, input:hidden, textarea').val('');
		$('#avyReportFinishedImgsTable > tbody').empty();
		$('#avyReportFinishedImgsTable').hide();
	}
	
	this.doAvyDrawing = function() {
		self.currentDrawing = new AvyDraw(view.getGEarth(), view.getGE(),
			function(lat, lng, elev, aspect, angle, kmlStr) {
				self.setAvyDrawingHiddenInputs(lat, lng, elev, aspect, angle, kmlStr);
				self.confirmDrawing();
			});
		
		self.currentDrawing.startAvyDraw();
	}
	
	this.initAvyReport = function() {
		$('#avyReportGeocodeDialog').dialog('open');
	}

	this.beginReportWithGeocode = function() {
		view.initialGeocodeForReport = true;
		view.geocodeAndFlyToLocation($('#avyReportInitLocation').val(), 8000.0, 65.0);
	}
	
	this.beginReport = function() {
		$.ui.dialog.prototype._focusTabbable = function(){};
		$('#avyReportBeginDrawDialog').dialog('open');
		view.navControlBlink(50);
	}
	
	this.confirmDrawing = function() {
		$.ui.dialog.prototype._focusTabbable = function(){};
		$('#avyReportConfirmDrawDialog').dialog('open');
	}
	
	this.enterAvyDetail = function() {
		var extId = $('#avyReportExtId').val();
		var imgUploadUrl = '/imgupload/' + extId;
		$("#imgUploadForm").fileupload({dataType:'json', url:imgUploadUrl, dropZone:$('#avyReportImgDropZone'),
	        done: function(e, data) {
	        	$('#avyReportFinishedImgsTable').show();
	        	$('#avyReportFinishedImgsTable').append('<tr><td>' + data.result.fileName + '</td><td>' + data.result.fileSize + '</td></tr>');
	        }
	    });
		
		$.ui.dialog.prototype._focusTabbable = function(){};
		$('#avyReportDialog').dialog('open');
	}

	this.initAvyReportDialogs = function() {
		$('#avyReportInitLocation').keydown(function (e){    
			  if (e.keyCode == 13) {
			    $('#avyReportGeocodeDialog').dialog('close');
			    if ($('#avyReportInitLocation').val()) {
			    	self.beginReportWithGeocode();
			    } else {
			    	self.beginReport();
			    }
			  }
		});
		
		$('#avyReportGeocodeDialog').dialog({
			  minWidth: 500,
			  autoOpen: false,
			  modal: true,
			  draggable: false,
			  closeOnEscape: false,
			  beforeclose: function (event, ui) { return false; },
			  dialogClass: "avyDialog",
			  title: "Avalanche Report",
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
			    	view.resetView();
			        $(this).dialog('close');
			      }
				}
			  ]
			});
		
		$('#avyReportBeginDrawDialog').dialog({
			  minWidth: 500,
			  autoOpen: false,
			  modal: true,
			  draggable: false,
			  closeOnEscape: false,
			  beforeclose: function (event, ui) { return false; },
			  dialogClass: "avyDialog",
			  title: "Avalanche Report",
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
				    view.resetView();
			        $(this).dialog('close');
			      }
				}
			  ]
			});
		
		$('#avyReportConfirmDrawDialog').dialog({
			  minWidth: 500,
			  autoOpen: false,
			  modal: true,
			  draggable: true,
			  closeOnEscape: false,
			  beforeclose: function (event, ui) { return false; },
			  dialogClass: "avyDialog",
			  title: "Avalanche Report",
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
		
		$('#avyReportImgDialog').dialog({
			  minWidth: 750,
			  minHeight: 760,
			  autoOpen: false,
			  modal: true,
			  draggable: false,
			  resizable: false,
			  closeOnEscape: false,
			  dialogClass: "avyReportDetailsDialog",
			  show: { effect: "slide", duration: 500 },
			  hide: { effect: "slide", duration: 500 },
			  buttons: [
			    {
			      text: "Done with Images",
			      click: function(event, ui) {
			    	  $(this).dialog('close');
			      }
			    }
			    ]
		});
		
		$('#avyReportDialog').dialog({
			  minWidth: 750,
			  minHeight: 760,
			  autoOpen: false,
			  modal: true,
			  draggable: false,
			  resizable: false,
			  closeOnEscape: false,
			  dialogClass: "avyReportDetailsDialog",
			  open: function(ev, ui) {
			      $('.ui-widget-overlay').css({opacity: .60});
                  $(this).parent().find('.ui-dialog-buttonset').css({'width':'100%','text-align':'right'});
				  $(this).parent().find('button:contains("Image")').css({'float':'left'});
			  },
			  close: function(ev, ui) {
			      $('.ui-widget-overlay').css({opacity: .40});
			  },
			  buttons: [
			    {
			      text: "Image Attachment",
			      click: function(event, ui) {
			    	  $('#avyReportImgDialog').dialog('open');
			      }
			    },
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
			    	  view.resetView();
				      $(this).dialog('close');
			      }
				}
			  ]
	    });	
	}
	
    } // end AvyReport function

return AvyReport;
});