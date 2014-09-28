define(['jquery-ui', 
        'jquery-geocomplete', 
        'jquery-fileupload', 
        'jquery-iframe-transport'
        ], function() {

var AvyEyesWiring = function(avyEyesView) {
  this.view = avyEyesView;
}

AvyEyesWiring.prototype.wireUI = function() {
  var aeView = this.view;
  
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
		  aeView.currentReport.toggleClassification(true); 
		} else {
		  aeView.currentReport.toggleClassification(false); 
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
	  aeView.toggleMenu();
	});
  
  $('#searchMenuItem').click(function(){
    aeView.toggleMenu();
    aeView.resetView();
  });
  
  $('#reportMenuItem').click(function(){
    aeView.toggleMenu();
    aeView.resetView();
    aeView.hideSearchDiv();
    aeView.doReport();
	});
  
  $('#aboutMenuItem').click(function(){
    aeView.toggleMenu();
    aeView.showHelp(2);
  });

  $('#howItWorks').click(function(){
    aeView.showHelp(0);
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
	
	$('#avyReportViewableTd').children(':checkbox').change(function(){
      if ($(this).is(':checked')) {
        $('#avyReportViewableTd').css('background', 'rgba(0, 255, 0, 0.3)');
      } else {
        $('#avyReportViewableTd').css('background', 'rgba(255, 0, 0, 0.3)');
      }
  });
	
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
	  aeView.geocodeAndFlyToLocation($('#avySearchLocation').val(), 5000.0, 20.0);
	});
	
	$('#aeControlContainer').css('visibility', 'visible');
	$('#avyInitLiftCallback').submit();
	
	$('#avyReportInitLocation').keydown(function (e) {
	  if (e.keyCode == 13) {
	    $('#avyReportGeocodeDialog').dialog('close');
	    if ($('#avyReportInitLocation').val()) {
	      aeView.currentReport.beginReportWithGeocode();
	    } else {
	      aeView.currentReport.beginReport();
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
	  buttons: [{
      text: "Begin Report",
      click: function(event, ui) {
      	$(this).dialog('close');
    		if ($('#avyReportInitLocation').val()) {
    		  aeView.currentReport.beginReportWithGeocode();
    		} else {
    		  aeView.currentReport.beginReport();
    		}
      }
	  },{
      text: "Cancel",
      click: function(event, ui) {
        aeView.resetView();
        $(this).dialog('close');
      }
    }]
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
	  buttons: [{
      text: "Begin Drawing",
      click: function(event, ui) {
        $(this).dialog('close');
        aeView.stopNavControlBlink();
        aeView.currentReport.doAvyDrawing();
      }
    },{
      text: "Cancel",
      click: function(event, ui) {
        aeView.resetView();
        $(this).dialog('close');
      }
    }]
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
	  buttons: [{
      text: "Accept Drawing",
      click: function(event, ui) {
        $(this).dialog('close');
        aeView.currentReport.enterAvyDetail();
      }
    },{
      text: "Redraw",
      click: function(event, ui) {
        $(this).dialog('close');
        aeView.currentReport.clearAvyDrawing();
        aeView.currentReport.doAvyDrawing();
      }
    }]
	});
	
	$('#avyReportImageDialog').dialog({
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
	  buttons: [{
      text: "Done with Images",
      click: function(event, ui) {
        $(this).dialog('close');
      }
		}]
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
		buttons: [{
      text: "Image Attachment",
      click: function(event, ui) {
    	  $('#avyReportImageDialog').dialog('open');
      }
    },{
      text: "Submit",
      click: function(event, ui) {
    	  $("#avyReportDialog").children('form').submit();
    	  aeView.resetView();
    	  $(this).dialog('close');
      }
    },{
      text: "Cancel",
      click: function(event, ui) {
        aeView.resetView();
	      $(this).dialog('close');
      }
    }]
  });	
}

AvyEyesWiring.prototype.wireReportAdminControls = function() {
  if ($('#avyReportViewableTd').is(':visible')) return; // already wired admin fields
  
  var aeView = this.view;
  
  $('#avyReportImageTable').show();
  
  $('#avyReportViewableTd').css('visibility', 'visible');
  $('#avyReportDeleteConfirmDialog').css('visibility', 'visible');
  
  $('#avyReportDeleteConfirmDialog').dialog({
    title: "Confirm",
    minWidth: 500,
    autoOpen: false,
    modal: true,
    resizable: false,
    draggable: false,
    closeOnEscape: false,
    beforeclose: function(event, ui) { return false; },
    dialogClass: 'avyDialog',
    open: function() { $('#avyReportDeleteConfirmNo').focus(); }, 
    buttons: [{
      text: 'Yes',
      click: function(event, ui) {
        $('#avyReportDeleteBinding').click();
        $(this).dialog('close');
        $('#avyReportDialog').dialog('close');
        aeView.resetView();
      }
    },
    {
      id: 'avyReportDeleteConfirmNo',
      text: 'No',
      click: function(event, ui) {
        $(this).dialog('close');
      }
    }]
  });
  
  var reportDialogButtons = $('#avyReportDialog').dialog("option", "buttons");
  reportDialogButtons.push({ 
    text: "Delete",
    click: function(event, ui) {
      $('#avyReportDeleteConfirmDialog').dialog('open');
    }
  });
  
  $('#avyReportDialog').dialog('option', 'buttons', reportDialogButtons);  
}

return AvyEyesWiring;
});