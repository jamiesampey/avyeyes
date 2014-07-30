define(['ae-report',
        'jquery-ui', 
        'jquery-geocomplete', 
        'jquery-fileupload', 
        'jquery-iframe-transport',
        'lightbox'
        ], function(AvyReport) {

function AvyEyesView(gearthInst, gmapsInst, loadingSpinner) {
	var self = this;
	var gearth = gearthInst;
	var gmaps = gmapsInst;
	var ge = null;
	var geocoder = null;
	
	this.currentReport = null;
	this.isFirstReport = true;
	this.avySearchResultKmlObj = null;
	this.aeFirstImpression = true;
	this.initialGeocodeForReport = false;
	
	this.showSearchDiv = function() {
		if (!$('#aeSearchControlContainer').is(':visible')) {
			$('#aeSearchControlContainer').slideDown("slow");
		}
	}
	
	this.hideSearchDiv = function() {
		if ($('#aeSearchControlContainer').is(':visible')) {
			$('#aeSearchControlContainer').slideUp("slow");
		}
	}
	
	this.clearSearchFields = function() {
		$('#aeSearchControlContainer').find('input:text').val('');
		$('#aeSearchControlContainer').find('.avyRDSliderValue').val('0');
		$('#aeSearchControlContainer').find('.avyRDSlider').slider('value', 0);
	}
	
	this.showModalDialog = function(title, msg) {
		$.ui.dialog.prototype._focusTabbable = function(){};
		$('#multiDialog').html(msg);
		$('#multiDialog').dialog('option', 'title', title);
		$('#multiDialog').dialog('open');
	}
	
	this.doReport = function() {
		self.currentReport = new AvyReport(self, function() {
			$("#avyReportDialog").children('form').submit();
			self.cancelReport();
		});
		
		if (self.isFirstReport) {
			self.currentReport.initAvyReportDialogs();
			self.isFirstReport = false;
		}
		
		self.currentReport.initAvyReport();
	}

	this.cancelReport = function() {
		if (self.currentReport) {
			self.currentReport.clearAllFields();
			self.currentReport.clearAvyDrawing();
			self.currentReport = null;
		}
		self.stopNavControlBlink();
	}
	
	this.viewChangeEnd = function() {
		var viewBoundsBox = ge.getView().getViewportGlobeBounds();
		$("#avySearchNorthLimit").val(viewBoundsBox.getNorth());
		$("#avySearchEastLimit").val(viewBoundsBox.getEast());
		$("#avySearchSouthLimit").val(viewBoundsBox.getSouth());
		$("#avySearchWestLimit").val(viewBoundsBox.getWest());
		
		var camera = ge.getView().copyAsCamera(ge.ALTITUDE_RELATIVE_TO_GROUND);
		$("#avySearchCameraAlt").val(camera.getAltitude());
		$("#avySearchCameraTilt").val(camera.getTilt());
		$("#avySearchCameraLat").val(camera.getLatitude());
		$("#avySearchCameraLng").val(camera.getLongitude());
		
		if (self.aeFirstImpression) {
			self.showSearchDiv();
			self.aeFirstImpression = false;
		}
		
		if (self.currentReport && self.initialGeocodeForReport) {
			setTimeout(self.currentReport.beginReport, 2000);
			self.initialGeocodeForReport = false;
		}
	}
		
	this.handleMapClick = function(event) {
		if ($('#avyDetailDialog').is(':visible')) {
			self.hideAvyDetails();
		}

	    var placemark = event.getTarget();
	    if (placemark.getType() != 'KmlPlacemark') {
	    	return;
	    }
		event.preventDefault();
		
		var kmlDoc = $.parseXML(placemark.getKml());
		var extId = $(kmlDoc).find('Placemark').attr('id');
		
		$.getJSON('/rest/avydetails/' + extId, function(data) {
			self.showAvyDetails(event, data);
		})
		.fail(function(jqxhr, textStatus, error) {
			var err = textStatus + ", " + error;
			console.log("Avy Eyes error: " + err);
		});
	}
	
	this.showAvyDetails = function(kmlClickEvent, a) {
		$('#avyDetailTitle').text(a.avyDate + ': ' + a.areaName);
		$('#avyDetailSubmitterExp').text(a.submitterExp);
		
		var extUrl = 'http://avyeyes.com/' + a.extId;
		$('#avyDetailExtLink').attr('href', extUrl);
		$('#avyDetailExtLink').text(extUrl);
		
		$('#avyDetailType').text(a.avyType);
		$('#avyDetailTrigger').text(a.trigger);
		$('#avyDetailInterface').text(a.bedSurface);
		$('#avyDetailRSize').text(a.rSize);
		$('#avyDetailDSize').text(a.dSize);

		$('#avyDetailElevation').text(a.elevation);
		$('#avyDetailAspect').text(a.aspect);
		$('#avyDetailAngle').text(a.angle);
		
		$('#avyDetailSky').text(a.sky);
		$('#avyDetailPrecip').text(a.precip);
		
		$('#avyDetailNumCaught').text(a.caught);
		$('#avyDetailNumPartiallyBuried').text(a.partiallyBuried);
		$('#avyDetailNumFullyBuried').text(a.fullyBuried);
		$('#avyDetailNumInjured').text(a.injured);
		$('#avyDetailNumKilled').text(a.killed);
		$('#avyDetailModeOfTravel').text(a.modeOfTravel);
		
		if (a.comments.length > 0) {
			$('#avyDetailCommentsRow').show();
			$('#avyDetailComments').text(a.comments);			
		}

		if (a.images.length > 0) {
			$('#avyDetailImageRow').show();
            $.each(a.images, function(i) {
				var imgUrl = '/rest/imgserve/' + a.extId + '/' + a.images[i];
				$('#avyDetailImageList').append('<li class="avyDetailImageListItem"><a href="' + imgUrl 
					+ '" data-lightbox="avyDetailImages"><img src="' + imgUrl + '" /></a></li>');
			});
		}
		
		Object.defineProperty(kmlClickEvent, "pageX", {value: kmlClickEvent.getClientX()});
		Object.defineProperty(kmlClickEvent, "pageY", {value: kmlClickEvent.getClientY()});
		$('#avyDetailDialog').dialog('option', 'position', {
			  my: 'center bottom-20', 
			  at: 'center top', 
			  of: kmlClickEvent,
			  collision: 'fit'
		  });

		$('#avyDetailDialog').dialog('open');
	}
	
	this.hideAvyDetails = function() {
		$('#avyDetailCommentsRow').hide();
		$('#avyDetailImageList').empty();
		$('#avyDetailImageRow').hide();
		$('#avyDetailDialog').dialog('close');
	}
	
	this.flyTo = function(lat, lng, rangeMeters, tiltDegrees, headingDegrees) {
		var lookAt = ge.createLookAt('');
	    lookAt.setLatitude(lat);
	    lookAt.setLongitude(lng);
	    lookAt.setRange(rangeMeters);
	    lookAt.setAltitudeMode(ge.ALTITUDE_RELATIVE_TO_GROUND);
	    lookAt.setTilt(tiltDegrees);
	    lookAt.setHeading(headingDegrees);
	    ge.getView().setAbstractView(lookAt);
	}
	    
	this.geocodeAndFlyToLocation = function(address, rangeMeters, tiltDegrees) {
	  if (!address) {
		  return;
	  }
	  
	  geocoder.geocode( {'address': address}, function(results, status) {
	    if (status == gmaps.GeocoderStatus.OK && results.length) {
			var latLng = results[0].geometry.location;
	    	self.flyTo(latLng.lat(), latLng.lng(), rangeMeters, tiltDegrees, 0);
	    }
	  });
	}

	this.stopNavControlBlink = function() {
		ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);
		clearInterval(self.navControlBlinkInterval);
	}
	
	this.navControlBlinkInterval = null;
	this.navControlBlink = function(numBlinks) {
		var blinkCount = 0;
		$('#map3d').focus();
		self.navControlBlinkInterval = setInterval(function(){
			if (ge.getNavigationControl().getVisibility() != ge.VISIBILITY_SHOW) {
				ge.getNavigationControl().setVisibility(ge.VISIBILITY_SHOW);
			} else {
				ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);
			}
			
			blinkCount++;
			if (numBlinks && blinkCount >= (numBlinks*2)) {
				self.stopNavControlBlink();
			}
		}, 1000);
	}
	
	this.overlaySearchResultKml = function(kmlStr) {
		self.avySearchResultKmlObj = ge.parseKml(kmlStr);
		ge.getFeatures().appendChild(self.avySearchResultKmlObj);
	}
		
	this.clearSearchResultKml = function() {
		if(ge.getFeatures().hasChildNodes()) {
			ge.getFeatures().removeChild(self.avySearchResultKmlObj);
		}
		self.avySearchResultKmlObj = null;
	}

	this.resetView = function() {
		self.clearSearchFields();
		self.clearSearchResultKml();
		self.cancelReport();
		self.showSearchDiv();
	}
	
	this.init = function() {
		$('label').each(function() {
			$(this).tooltip({
				items: '[data-help]',
				content: $(this).data('help'),
				tooltipClass: 'avyTooltip',
				position: {
					my: "left center", 
					at: 'right+10 center', 
					of: $(this), 
					collision: "flipfit"
				}
			});
		});
		
		$('.avyAutoComplete').autocomplete({
			minLength: 0,
			delay: 0,
			select: function(event, ui) {
				$(this).siblings(':hidden').val(ui.item.value);
				$(this).val(ui.item.label);
				return false;
			}
		});
		
		$('.avyAutoComplete').change(function() {
			$(this).val('');
			$(this).siblings(':hidden').val('');
		});
		
		$('.avyAutoComplete').focus(function(){            
			$(this).autocomplete("search");
	    });
		
	  $('#aeMenu').menu().position({
		  my: 'left top', 
		  at: 'right top', 
		  of: $('#aeMenuButtonContainer'), 
		  collision: 'none none'
	  });
	  $('#aeMenu').hide();
	  
	  $('#aeMenuButton').click(function(){
		  $('#aeMenu').toggle('slide', 400);
		});
	  
	  $('#searchMenuItem').click(function(){
		  $('#aeMenu').toggle('slide', 400);
			 self.resetView();
		});
	  
	  $('#reportMenuItem').click(function(){
		  $('#aeMenu').toggle('slide', 400);
		  self.resetView();
		  self.hideSearchDiv();
		  self.doReport();
		});
	  
	  $('#aboutMenuItem').click(function(){
		  $('#aeMenu').toggle('slide', 400);
		 self.showModalDialog('Info', '...about the project');
	  });
		
	  $('#debugReportForm').click(function(){
		  $('#aeMenu').toggle('slide', 400);
			 self.currentReport = new AvyReport(self, function() {
					$("#avyReportDialog").children('form').submit();
					self.currentReport = null;
				});
			 self.currentReport.initAvyReportDialogs();
			 self.currentReport.enterAvyDetail();
	  });
	  
		$('.avyDate').datepicker({
			dateFormat: "mm-dd-yy",
			constrainInput: true,
			changeMonth: true,
			stepMonths: 1,
			changeYear: true,
			maxDate: 0,
			yearRange: "1970:+0"
		});
			
		$('.avyRDSlider').slider({
			min: 0, 
			max: 5, 
			step: 0.5,
			range: 'min',
			create: function (event, ui) {
				$(this).siblings(".avyRDSliderValue").val(0);
			},
			slide: function (event, ui) {
				$(this).siblings(".avyRDSliderValue").val(ui.value);
		    }
		});
		
		$('.avyHumanNumber').spinner({
			min: -1,
			max: 500, 
			step: 1, 
			spin: function(event, ui) {
				if (ui.value < 0) {
					event.preventDefault();
					$(this).spinner('value', '');
				}
			}
		}).val('');
		
		$('.avySlopeAngle').spinner({
			min: 0,
			max: 90,
			step: 1}).val(0);
		
		$('.avyButton').button();
		
		$('#multiDialog').dialog({
			  minWidth: 500,
			  autoOpen: false,
			  modal: true,
			  resizable: false,
			  draggable: false,
			  closeOnEscape: false,
			  beforeclose: function (event, ui) { return false; },
			  dialogClass: 'avyDialog',
			  buttons: [{
			      text: 'OK',
			      click: function(event, ui) {
			    	$(this).dialog('close');
			    	$('#multiDialog').html('');
			      }
			  }]
		});
		
		$('#avyDetailDialog').dialog({
			  minWidth: 650,
			  autoOpen: false,
			  modal: false,
			  resizable: false,
			  draggable: false,
			  closeOnEscape: false,
			  dialogClass: 'avyReportDetailsDialog'
		});
		
		$.extend($.ui.autocomplete.prototype, {
		    _renderItem: function( ul, item ) {
		        var re = new RegExp('(' + this.element.val() + ')', 'gi'),
		            html = item.label.replace( re, '<span style="background-color: #1978AB">$&</span>');
		        return $( "<li></li>" )
		            .data( "item.autocomplete", item )
		            .append( $("<a></a>").html(html) )
		            .appendTo( ul );
		    }
		});
		
		$('.avyLocation').geocomplete({types: ['geocode']});
		
		$('#avySearchLocation').blur(function (event) {
			self.geocodeAndFlyToLocation($('#avySearchLocation').val(), 9400.0, 40.0);
		});
		
		$('#avyInitLiftCallback').submit();
	}
	
	this.initCB = function(instance) {
		self.setGE(instance);
	    self.setGeocoder(new gmaps.Geocoder());
	    
		ge.getWindow().setVisibility(true);
	    ge.getOptions().setStatusBarVisibility(true);
	    ge.getOptions().setUnitsFeetMiles(true);
	    ge.getOptions().setFlyToSpeed(0.4);
	    ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);
	    ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, true);
	    ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);
	    gearth.addEventListener(ge.getView(), 'viewchangeend', self.viewChangeEndTimeout);
	    gearth.addEventListener(ge.getGlobe(), 'click', self.handleMapClick);
	    
	    self.init();
	    $('#loadingDiv').fadeOut(500);
	}
		    
	this.failureCB = function(errorCode) {
	    self.showModalDialog("Error", "failureCB: " + errorCode);
	}

	this.viewChangeEndTimeout = function() {
		var viewChangeEndTimer;
		if(viewChangeEndTimer){
		    clearTimeout(viewChangeEndTimer);
		  }
		viewChangeEndTimer = setTimeout(self.viewChangeEnd(), 200);
	}

	this.getGEarth = function() {
		return gearth;
	}
	
	this.getGE = function() {
		return ge;
	}
	
	this.setGE = function(instance) {
		ge = instance;
	}
	
	this.setGeocoder = function(instance) {
		geocoder = instance;
	}
	
	gearth.createInstance('map3d', self.initCB, self.failureCB, { 'language': 'en' });
}

return AvyEyesView;
});