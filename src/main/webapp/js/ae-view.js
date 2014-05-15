define(['ae-report', 
        'async!https://maps.googleapis.com/maps/api/js?v=3.15&libraries=places&sensor=true&key=' + GOOGLE_API_KEY, 
        'jquery-ui', 
        'jquery-geocomplete'], function(AvyReport) {

function AvyEyesView() {
	var self = this;
	var ge = null;
	var geocoder = new google.maps.Geocoder();
	var currentReport = null;
	var avySearchResultKmlObj = null;
	
	var INIT_LAT = 44.0;
	var INIT_LNG = -115.0;
	var INIT_ALT_FT = 2700000.0;
	
	this.aeFirstImpression = true;
	this.initialGeocodeForReport = false;
	
	this.showSearchMenu = function() {
		$('#aeSearchControlContainer').slideDown("slow");
	}
	
	this.hideSearchMenu = function() {
		$('#aeSearchControlContainer').slideUp("slow");
	}
	
	this.showModalDialog = function(dialogTitle, msg, details) {
		if (details) {
			msg = msg.concat("<br><br><u>Details:</u><p>" + details + "</p>");
		}
		$('#multiDialog').html(msg);
		
		$.ui.dialog.prototype._focusTabbable = function(){};
		$('#multiDialog').dialog({
			  minWidth: 500,
			  title: dialogTitle,
			  modal: true,
			  draggable: false,
			  closeOnEscape: false,
			  beforeclose: function (event, ui) { return false; },
			  dialogClass: 'noclose',
			  buttons: [{
			      text: 'OK',
			      click: function(event, ui) {
			    	$(this).dialog('close');
			    	$('#multiDialog').html('');
			      }
			  }]
			});
	}
	
	this.doReport = function() {
		currentReport = new AvyReport(self, function() {
			$("#avyReportDetailsDialog").children('form').submit();
			currentReport = null;
		});
		
		currentReport.initAvyReport();
	}

	this.cancelReport = function() {
		if (currentReport) {
			currentReport.clearAllFields();
			currentReport = null;
		}
		self.stopNavControlBlink();
		self.showSearchMenu();
	}
	
	this.viewChangeEndTimeout = function() {
		var viewChangeEndTimer;
		if(viewChangeEndTimer){
		    clearTimeout(viewChangeEndTimer);
		  }
		viewChangeEndTimer = setTimeout(avyEyesView.viewChangeEnd(), 200);
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
		
		if (this.aeFirstImpression) {
			this.showSearchMenu();
			this.aeFirstImpression = false;
		}
		
		if (currentReport && this.initialGeocodeForReport) {
			setTimeout(currentReport.beginReport, 2000);
			this.initialGeocodeForReport = false;
		}
	}
		
	this.flyTo = function(lat, lng, range, tilt) {
		var lookAt = ge.createLookAt('');
	    lookAt.setLatitude(lat);
	    lookAt.setLongitude(lng);
	    lookAt.setRange(range);
	    lookAt.setAltitudeMode(ge.ALTITUDE_RELATIVE_TO_GROUND);
	    lookAt.setTilt(tilt);
	    ge.getView().setAbstractView(lookAt);
	}
	    
	this.geocodeAndFlyToLocation = function(address, range, tilt) {
	  geocoder.geocode( { 'address': address}, function(results, status) {
	    if (status == google.maps.GeocoderStatus.OK && results.length) {
			var latLng = results[0].geometry.location;
	    	self.flyTo(latLng.lat(), latLng.lng(), range, tilt);
	    } else {
	      alert('Geocode was not successful for the following reason: ' + status);
	    }
	  });
	}

	this.stopNavControlBlink = function() {
		ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);
		clearInterval(this.navControlBlinkInterval);
	}
	
	this.navControlBlinkInterval = null;
	this.navControlBlink = function(numBlinks) {
		var blinkCount = 0;
		$('#map3d').focus();
		this.navControlBlinkInterval = setInterval(function(){
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
		avySearchResultKmlObj = ge.parseKml(kmlStr);
		ge.getFeatures().appendChild(avySearchResultKmlObj);
	}
		
	this.clearSearchResultKml = function() {
		ge.getFeatures().removeChild(avySearchResultKmlObj);
	}

	this.getGE = function() {
		return ge;
	}
	
	this.init = function(gePlugin) {
		ge = gePlugin;
		
		$(".avyAutoComplete").autocomplete({
			minLength: 0,
			delay: 0,
			select: function(event, ui) {
				$(this).siblings(':hidden').val(ui.item.value);
				$(this).val(ui.item.label);
				return false;
			}
		});
		
		$(".avyAutoComplete").change(function() {
			$(this).val('');
			$(this).siblings(':hidden').val('');
		});
		
		$(".avyAutoComplete").focus(function(){            
			$(this).autocomplete("search");
	    });
		
	  var aeMenuList = $('#aeMenuList');
	  aeMenuList.menu();
	
	  $('#aeMenuButton').click(function(){
		  aeMenuList.slideToggle(200);
		});
	  
	  $('#reportMenuItem').click(function(){
		 aeMenuList.slideToggle(200);
		 self.doReport();
		});
	  
	  $('#aboutMenuItem').click(function(){
		 aeMenuList.slideToggle(200);
		 alert('about the project');
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
		
		$.extend($.ui.autocomplete.prototype, {
		    _renderItem: function( ul, item ) {
		        var re = new RegExp('(' + this.element.val() + ')', 'gi'),
		            html = item.label.replace( re, '<span style="background-color: #FFFF00">$&</span>');
		        return $( "<li></li>" )
		            .data( "item.autocomplete", item )
		            .append( $("<a></a>").html(html) )
		            .appendTo( ul );
		    }
		});
		
		$('.avyLocation').geocomplete({types: ['geocode']});
		
		$('#avySearchLocation').blur(function (event) {
			self.geocodeAndFlyToLocation($('#avySearchLocation').val(), 14000.0, 44.0);
		});
		
		this.flyTo(INIT_LAT, INIT_LNG, INIT_ALT_FT, 0.0);
		document.dispatchEvent(new CustomEvent("avyEyesViewInit", {}));
	}
}

return AvyEyesView;
});