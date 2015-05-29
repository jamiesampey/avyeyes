define(['lib/Cesium/Cesium',
        'lib/jquery-ui',
        'lib/jquery.geocomplete'
        ], function(Cesium) {

var AvyEyesUI = {};

AvyEyesUI.wire = function(view) {
    wireMainMenu(view);
    wireTooltips();
    wireAutoCompletes();
    wireDatePickers();
    wireSliders();
    wireSpinners();
    wireButtons(view);
    wireLocationInputs(view);
    wireDialogs(view);
}

function wireMainMenu(view) {
    $('#aeMenu').menu().position({
        my: 'left top',
        at: 'right top',
        of: $('#aeMenuButtonContainer'),
        collision: 'none none'
    });

    var menuBlurTimer;

    $('#aeMenu').on('menufocus', function(event, ui) {
        clearTimeout(menuBlurTimer);
    });

    $('#aeMenu').on('menublur', function(event, ui) {
        menuBlurTimer = setTimeout(function() {
        $('#aeMenu').hide('slide', 400);
    }, 200);
    });

    $('#aeMenu').on('menuselect', function(event, ui){
        $('#aeMenu').hide('slide', 400);
    });

    $('#aeMenu').hide();

    $('#aeMenuButton').click(function(){
        $('#aeMenu').show('slide', 400);
        menuBlurTimer = setTimeout(function() {
            $('#aeMenu').hide('slide', 400);
        }, 4000);
    });

    $('#searchMenuItem').click(function(){
        view.resetView();
    });

    $('#reportMenuItem').click(function(){
        view.resetView();
        hideSearchDiv();
        view.doReport();
    });

    $('#aboutMenuItem').click(function(){
        view.showHelp(2);
    });

    $('#howItWorks').click(function(){
        view.showHelp(0);
    });
}

function wireTooltips() {
	$('label').each(function() {
		$(this).tooltip({
			items: '[data-help]',
			content: $(this).data('help'),
			tooltipClass: 'avyTooltip',
			show: {delay: 700},
			position: {
				my: "left center",
				at: 'right+10 center',
				of: $(this),
				collision: "flipfit"
			}
		});
	});
}

function wireAutoCompletes() {
	$('.avyAutoComplete').autocomplete({
		minLength: 0,
		delay: 0,
		select: function(event, ui) {
			$(this).siblings(':hidden').val(ui.item.value);
			$(this).val(ui.item.label);
			return false;
		}
	});

	$('#rwAvyFormDialog .avyAutoComplete').autocomplete('option', 'appendTo', '#rwAvyFormDialog');

	$('.avyAutoComplete').change(function() {
		$(this).val('');
		$(this).siblings(':hidden').val('');
	});

	$('.avyAutoComplete').focus(function(){
		$(this).autocomplete("search");
    });

	$('.avyAutoComplete').click(function(){
		$(this).select();
    });

	$('.avyExperienceLevelAutoComplete').on("autocompleteselect", function(event, ui){
		if (ui.item.value === 'P2' || ui.item.value === 'PE') {
		  toggleTechnicalReportFields(true);
		} else {
		  toggleTechnicalReportFields(false);
		}
		return false;
	});

    $.extend($.ui.autocomplete.prototype, {
        _renderItem: function( ul, item ) {
            var re = new RegExp('(' + this.element.val() + ')', 'gi');
            var html = item.label.replace(re, '<span style="background-color: #1978AB">$&</span>');
            return $( "<li></li>" )
                .data( "item.autocomplete", item )
                .append( $("<a></a>").html(html) )
                .appendTo( ul );
        }
    });
}

function wireDatePickers() {
    $('.avyDate').datepicker({
        dateFormat: "mm-dd-yy",
        constrainInput: true,
        changeMonth: true,
        stepMonths: 1,
        changeYear: true,
        maxDate: 0,
        yearRange: "1970:+0"
    });
}

function wireSliders() {
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
}

function wireSpinners() {
    $('.avyHumanNumber').spinner({
        min: -1,
        max: 500,
        step: 1,
        spin: function(event, ui) {
            if (ui.value < 0) {
                event.preventDefault();
                $(this).spinner('value', ''); // empty value signifies unknown human number
            } else if ($(this).val() == '' && ui.value > 0) {
                event.preventDefault();
                $(this).spinner('value', 0); // start increasing at known human number of zero
            }
        }
    }).val('');

    $('.avySlopeAngle').spinner({
        min: 0,
        max: 90,
        step: 1
    }).val(0);
}

function wireButtons(view) {
	$('.avyButton').button();

	$('#avySearchButton').click(function() {
        var upperLeft = Cesium.Ellipsoid.WGS84.cartesianToCartographic(
            view.cesiumViewer.camera.pickEllipsoid(new Cesium.Cartesian2(0, 0), Cesium.Ellipsoid.WGS84));
        var lowerRight = Cesium.Ellipsoid.WGS84.cartesianToCartographic(
            view.cesiumViewer.camera.pickEllipsoid(new Cesium.Cartesian2(view.cesiumViewer.canvas.width,
            view.cesiumViewer.canvas.height), Cesium.Ellipsoid.WGS84));
        var camPos = Cesium.Ellipsoid.WGS84.cartesianToCartographic(view.cesiumViewer.camera.position);

        $("#avySearchLatTop").val(Cesium.Math.toDegrees(upperLeft.latitude));
        $("#avySearchLatBottom").val(Cesium.Math.toDegrees(lowerRight.latitude));
        $("#avySearchLngLeft").val(Cesium.Math.toDegrees(upperLeft.longitude));
        $("#avySearchLngRight").val(Cesium.Math.toDegrees(lowerRight.longitude));

        $("#avySearchCameraAlt").val(view.cesiumViewer.scene.globe.getHeight(camPos));
        $("#avySearchCameraPitch").val(Cesium.Math.toDegrees(view.cesiumViewer.camera.pitch));
        $("#avySearchCameraLat").val(Cesium.Math.toDegrees(camPos.latitude));
        $("#avySearchCameraLng").val(Cesium.Math.toDegrees(camPos.longitude));

	    $(this).submit();
	});

	$('#avySearchResetButton').click(function() {
        $('#aeSearchControlContainer').find('input:text').val('');
        $('#aeSearchControlContainer').find('.avyRDSliderValue').val('0');
        $('#aeSearchControlContainer').find('.avyRDSlider').slider('value', 0);
    });

    $('#avyReportStartDrawingButton').click(function() {
        view.currentReport.startDrawing();
    });
}

function wireLocationInputs(view) {
	$('.avyLocation').geocomplete({types: ['geocode']});

	$('#avySearchLocation').blur(function(event) {
        view.geocodeAndFlyTo($('#avySearchLocation').val(), 6000.0, -70.0);
	});

    $('#avyReportInitLocation').keydown(function (event) {
        if (event.keyCode == 13) {
            $('#avyReportLocationDialog').dialog('close');
            if ($('#avyReportInitLocation').val()) {
                view.geocodeAndFlyTo($('#avyReportInitLocation').val(), 8000.0, -65.0);
            }
            event.preventDefault();
        }
    });
}

function wireDialogs(view) {
	$('#multiDialog').dialog({
        minWidth: 500,
        autoOpen: false,
        modal: true,
        resizable: false,
        draggable: false,
        closeOnEscape: false,
        beforeclose: function (event, ui) {
            return false;
        },
        buttons: [{
            text: 'OK',
            click: function(event, ui) {
                $(this).dialog('close');
                $('#multiDialog').html('');
            }
        }]
	});

	$('#roAvyFormDialog').dialog({
        minWidth: 750,
        maxWidth: 750,
        autoOpen: false,
        modal: false,
        resizable: false,
        draggable: false,
        closeOnEscape: false,
        dialogClass: "roAvyFormDialog"
	});

	$('#helpDialog').dialog({
        minWidth: 750,
        minHeight: 750,
        maxHeight: 750,
        autoOpen: false,
        modal: true,
        resizable: false,
        draggable: false,
        closeOnEscape: true,
        dialogClass: "rwAvyFormDialog",
        buttons: [{
            text: 'Close',
            click: function(event, ui) {
                $(this).dialog('close');
            }
        }]
	});
	$('#helpDialog').tabs();


    $('#avyReportLocationDialog').dialog({
        minWidth: 700,
        maxWidth: 700,
        minHeight: 500,
        maxHeight: 500,
        autoOpen: false,
        modal: true,
        resizable: false,
        draggable: false,
        closeOnEscape: false,
        beforeclose: function (event, ui) {
            return false;
        },
        dialogClass: "avyReportDrawDialog",
        title: "Avalanche Report",
        open: function() {
            $('#avyReportInitLocation').focus();
        },
        buttons: [{
            text: "Begin Report",
            click: function(event, ui) {
                $(this).dialog('close');
                if ($('#avyReportInitLocation').val()) {
                    view.geocodeAndFlyTo($('#avyReportInitLocation').val(), 8000.0, -65.0);
                }
            }
        },{
            text: "Cancel",
            click: function(event, ui) {
                view.resetView();
            }
        }]
    });

    $('#avyReportDrawingConfirmationDialog').dialog({
        minWidth: 500,
        maxWidth: 500,
        autoOpen: false,
        modal: true,
        resizable: false,
        draggable: true,
        closeOnEscape: false,
        beforeclose: function (event, ui) {
            return false;
        },
        dialogClass: "avyReportDrawDialog",
        title: "Avalanche Report",
        buttons: [{
            text: "Accept Drawing",
            click: function(event, ui) {
                $(this).dialog('close');
                $.ui.dialog.prototype._focusTabbable = function(){};
                toggleTechnicalReportFields(false);
                $('#rwAvyFormDialog').dialog('open');
            }
        },{
            text: "Redraw",
            click: function(event, ui) {
                $(this).dialog('close');
                $('#avyReportDrawButtonContainer').css('visibility', 'visible');
                view.currentReport.clearDrawing();
            }
        }]
    });

    $('#rwAvyFormImageDialog').dialog({
        minWidth: 750,
        minHeight: 810,
        autoOpen: false,
        modal: true,
        resizable: false,
        draggable: false,
        closeOnEscape: false,
        dialogClass: "rwAvyFormDialog",
        show: {
            effect: "slide",
            duration: 500
        },
        hide: {
            effect: "slide",
            duration: 500
        },
        buttons: [{
            text: "Done with Images",
            click: function(event, ui) {
                $(this).dialog('close');
            }
        }]
    });

    $('#rwAvyFormDialog').dialog({
        minWidth: 750,
        maxWidth: 750,
        minHeight: 800,
        maxHeight: 810,
        autoOpen: false,
        modal: true,
        resizable: false,
        draggable: false,
        closeOnEscape: false,
        dialogClass: "rwAvyFormDialog",
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
                $('#rwAvyFormImageDialog').dialog('open');
            }
        },{
            text: "Submit",
            click: function(event, ui) {
                $("#rwAvyFormDialog").children('form').submit();
            }
        },{
            text: "Cancel",
            click: function(event, ui) {
                $(this).dialog('close');
                view.resetView();
            }
        }]
    });
}

function toggleTechnicalReportFields(enabled) {
    if (enabled) {
        $('#rwAvyFormClassification .avyHeader').css('color', 'white');
        $('#rwAvyFormClassification label').css('color', 'white');
        $('#rwAvyFormClassification .avyRDSliderValue').css('color', 'white');
        $('#rwAvyFormClassification :input').prop('disabled', false);
        $('#rwAvyFormClassification .avyRDSlider').slider('enable');
    } else {
        $('#rwAvyFormClassification .avyHeader').css('color', 'gray');
        $('#rwAvyFormClassification label').css('color', 'gray');
        $('#rwAvyFormClassification .avyRDSliderValue').css('color', 'gray');
        $('#rwAvyFormClassification :input').val('');
        $('#rwAvyFormClassification :input').prop("disabled", true);
        $('#rwAvyFormClassification .avyRDSlider').slider('disable');
        $('#rwAvyFormClassification .avyRDSliderValue').val('0');
        $('#rwAvyFormClassification .avyRDSlider').slider('value', 0);
    }
}

AvyEyesUI.raiseTheCurtain = function() {
    if ($('#loadingDiv').is(':visible')) {
        $('#loadingDiv').fadeOut(500);
    }
}

AvyEyesUI.showSearchDiv = function(delay) {
    if (delay > 0) {
        setTimeout(function() {
            $('#aeSearchControlContainer').slideDown("slow");
        }, delay);
    } else {
        $('#aeSearchControlContainer').slideDown("slow");
    }
}

AvyEyesUI.hideSearchDiv = hideSearchDiv;
function hideSearchDiv() {
    $('#aeSearchControlContainer').slideUp("slow");
}

return AvyEyesUI;
});