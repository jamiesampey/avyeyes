define(['lib/jquery-ui'], function() {

var GEOCODE_FLYTO_PITCH = -70.0;
var GEOCODE_FLYTO_RANGE = 6500.0;
    
function AvyEyesUI() {};

AvyEyesUI.prototype.wire = function(view, callback) {
    wireMainMenu(view);
    wireTooltips();
    wireAutoCompletes(view);
    wireDatePickers();
    wireSliders();
    wireSpinners();
    wireButtons(view);
    wireLocationInputs(view);
    wireDialogs(view);
    callback();
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

    $('#searchMenuItem').parent("li").click(function(e){
        e.preventDefault();
        view.resetView();
    });

    $('#reportMenuItem').parent("li").click(function(e){
        e.preventDefault();
        view.resetView();
        hideSearchDiv();
        view.doReport();
    });

    $('#aboutMenuItem').parent("li").click(function(e){
        e.preventDefault();
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

function wireAutoCompletes(view) {
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
   	$('#rwAvyFormDialog .avyAutoComplete').focus(function() {
   	    var containerScrollTop = $("#rwAvyFormDialog").scrollTop();
   	    var dropDownBottom = $(this).position().top + containerScrollTop + 300;
   	    var containerBottom = containerScrollTop + $("#rwAvyFormDialog").height();
   	    var unseenDropdown = dropDownBottom - containerBottom;
   	    if (unseenDropdown > 0) {
   	        $('#rwAvyFormDialog').scrollTop(containerScrollTop + unseenDropdown);
   	    }
    });

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

	$('.avyWindSpeedAutoComplete').on("autocompleteselect", function(event, ui){
		if (ui.item.value === 'U') {
		  view.form.toggleWindDirectionFields(false);
		} else {
		  view.form.toggleWindDirectionFields(true);
		}
		return false;
	});

	$('.avyExperienceLevelAutoComplete').on("autocompleteselect", function(event, ui){
		if (ui.item.value === 'P2' || ui.item.value === 'PE') {
		  view.form.toggleTechnicalReportFields(true);
		} else {
		  view.form.toggleTechnicalReportFields(false);
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
    $("#northButton").click(function() {
        var camPos = view.cesiumViewer.camera.positionCartographic;
        var range = camPos.height < 20000 ? 20000 : camPos.height;
        var target = view.targetEntityFromCoords(Cesium.Math.toDegrees(camPos.longitude), Cesium.Math.toDegrees(camPos.latitude), false);
        view.flyTo(target, 0.0, -89.9, range);
    });

	$('.avyButton').button();

	$('#avySearchButton').click(function() {
        var boundingBox = view.getBoundingBox();

        $("#avySearchLatMax").val(boundingBox[0]);
        $("#avySearchLatMin").val(boundingBox[1]);
        $("#avySearchLngMax").val(boundingBox[2]);
        $("#avySearchLngMin").val(boundingBox[3]);

        $("#avySearchCameraAlt").val(view.cesiumViewer.camera.positionCartographic.height);
        $("#avySearchCameraPitch").val(Cesium.Math.toDegrees(view.cesiumViewer.camera.pitch));
        $("#avySearchCameraLng").val(Cesium.Math.toDegrees(view.cesiumViewer.camera.positionCartographic.longitude));
        $("#avySearchCameraLat").val(Cesium.Math.toDegrees(view.cesiumViewer.camera.positionCartographic.latitude));

	    $(this).submit();
	});

	$('#avySearchResetButton').click(function() {
        $('#avySearchDetailsTable').find('input:text').val('');
        $('#avySearchDetailsTable').find('input:hidden').val('');
        $('#avySearchDetailsTable').find('.avyRDSliderValue').val('0');
        $('#avySearchDetailsTable').find('.avyRDSlider').slider('value', 0);
    });

    $('#avyReportStartDrawingButton').click(function() {
        view.currentReport.startDrawing();
    });
}

function wireLocationInputs(view) {
	$('.avyLocation').click(function(){
		$(this).select();
    });

    $('.avyLocation').autocomplete({
        source: function (request, response) {
            view.geocode(request.term, function(data) {
                var resourceSet = data.resourceSets[0];
                if (resourceSet && resourceSet.estimatedTotal > 0) {
                    response($.map(resourceSet.resources, function(resource) {
                        return {
                            label: resource.name,
                            value: resource.name
                        };
                    }));
                }
            },
            function(error) {
                console.log("Failed to geo-complete location field. Error: " + error.textStatus)
            })
        },
        minLength: 5
    });

    $('.avyLocation').keydown(function (event) {
        if (event.keyCode == 13) {
            var dialogParent = $(this).parents('.ui-dialog-content');
            if (dialogParent) {
                dialogParent.dialog('close');
            }
            if ($(this).val()) {
                view.geocodeAndFlyTo($(this).val(), GEOCODE_FLYTO_PITCH, GEOCODE_FLYTO_RANGE);
            }
            event.preventDefault();
        }
    });

	$('#avySearchLocation').blur(function(event) {
        view.geocodeAndFlyTo($(this).val(), GEOCODE_FLYTO_PITCH, GEOCODE_FLYTO_RANGE);
	});
}

function wireDialogs(view) {
	$('#multiDialog').dialog({
        width: 500,
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
	    width: 800,
	    height: 280,
        autoOpen: false,
        modal: false,
        resizable: false,
        draggable: false,
        closeOnEscape: true,
        dialogClass: "roAvyFormDialog"
	});

	$('#helpDialog').dialog({
        width: 750,
        height: 620,
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
        width: 700,
        height: 500,
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
                    view.geocodeAndFlyTo($('#avyReportInitLocation').val(), -60.0, 8000.0);
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
        width: 500,
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
                view.form.toggleWindDirectionFields(false);
                view.form.toggleTechnicalReportFields(false);
                view.form.displayReadWriteForm();
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
        width: 750,
        height: 620,
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
        width: 750,
        height: 620,
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

AvyEyesUI.prototype.raiseTheCurtain = function() {
    if ($('#loadingDiv').is(':visible')) {
        $('#loadingDiv').fadeOut(500);
    }
}

AvyEyesUI.prototype.showSearchDiv = function(delay) {
    if (delay > 0) {
        setTimeout(function() {
            $('#aeSearchControlContainer').slideDown("slow");
        }, delay);
    } else {
        $('#aeSearchControlContainer').slideDown("slow");
    }
}

AvyEyesUI.prototype.hideSearchDiv = hideSearchDiv;
function hideSearchDiv() {
    $('#aeSearchControlContainer').slideUp("slow");
}

return AvyEyesUI;
});