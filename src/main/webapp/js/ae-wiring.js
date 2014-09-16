define(['jquery-ui', 
        'jquery-geocomplete', 
        'jquery-fileupload', 
        'jquery-iframe-transport'
        ], function() {

var wireUI = function(view) {
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

	$('#avyReportDialog .avyAutoComplete').autocomplete('option', 'appendTo', '#avyReportDialog');
	 
	$('.avyAutoComplete').change(function() {
		$(this).val('');
		$(this).siblings(':hidden').val('');
	});
	
	$('.avyAutoComplete').focus(function(){
		$(this).autocomplete("search");
  });
	
	$('.avyExperienceLevelAutoComplete').on("autocompleteselect", function(event, ui){
		if (ui.item.value === 'P2' || ui.item.value === 'PE') {
		  view.currentReport.toggleClassification(true); 
		} else {
		  view.currentReport.toggleClassification(false); 
		}
		return false;
	});

  $('#aeMenu').menu().position({
	  my: 'left top', 
	  at: 'right top', 
	  of: $('#aeMenuButtonContainer'), 
	  collision: 'none none'
  });
  $('#aeMenu').hide();
  
  $('#aeMenuButton').click(function(){
	  view.toggleMenu();
	});
  
  $('#searchMenuItem').click(function(){
	  view.toggleMenu();
	  view.resetView();
  });
  
  $('#reportMenuItem').click(function(){
	  view.toggleMenu();
	  view.resetView();
	  view.hideSearchDiv();
	  view.doReport();
	});
  
  $('#aboutMenuItem').click(function(){
	  view.toggleMenu();
	  view.showHelp(2);
  });

  $('#howItWorks').click(function(){
	  view.showHelp(0);
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

	$('#helpDialog').dialog({
		  minWidth: 750,
		  minHeight: 700,
		  autoOpen: false,
		  modal: true,
		  resizable: false,
		  draggable: false,
		  closeOnEscape: true,
		  dialogClass: "avyReportDetailsDialog",
		  buttons: [{
		      text: 'Close',
		      click: function(event, ui) {
		    	$(this).dialog('close');
		      }
		  }]
	});
	
	$('#helpDialog').tabs();
	
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
	
	$('#avySearchLocation').blur(function(event) {
		view.geocodeAndFlyToLocation($('#avySearchLocation').val(), 9400.0, 40.0);
	});
	
	$('#aeControlContainer').css('visibility', 'visible');
	$('#avyInitLiftCallback').submit();
	
	$('#avyReportInitLocation').keydown(function (e){
		  if (e.keyCode == 13) {
		    $('#avyReportGeocodeDialog').dialog('close');
		    if ($('#avyReportInitLocation').val()) {
		    	view.currentReport.beginReportWithGeocode();
		    } else {
		    	view.currentReport.beginReport();
		    }
		  }
	});
	
	$('#avyReportGeocodeDialog').dialog({
		  minWidth: 500,
		  autoOpen: false,
		  modal: true,
		  resizable: false,
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
	    			view.currentReport.beginReportWithGeocode();
	    		} else {
	    			view.currentReport.beginReport();
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
		  resizable: false,
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
			    view.currentReport.doAvyDrawing();
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
		  resizable: false,
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
			    view.currentReport.enterAvyDetail();
		      }
		    },
		    {
		      text: "Redraw",
		      click: function(event, ui) {
		        $(this).dialog('close');
		        view.currentReport.clearAvyDrawing();
		        view.currentReport.doAvyDrawing();
		      }
			}
		  ]
		});
	
	$('#avyReportImgDialog').dialog({
		  minWidth: 750,
		  minHeight: 700,
		  autoOpen: false,
		  modal: true,
		  resizable: false,
		  draggable: false,
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
		  minHeight: 700,
		  autoOpen: false,
		  modal: true,
		  resizable: false,
		  draggable: false,
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
		    	  $("#avyReportDialog").children('form').submit();
		    	  view.resetView();
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

return wireUI;
});