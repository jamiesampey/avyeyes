define(['jquery-ui'], function() {

function AvyEyesUI() {};

AvyEyesUI.prototype.wire = function(view) {
    wireMainMenu(view);
    wireTooltips();
    wireAutoCompletes(view);
    wireDatePickers();
    wireSliders();
    wireSpinners();
    wireButtons(view);
    wireLocationInputs(view);
    wireDialogs(view);
    raiseTheCurtain();
}

function wireMainMenu(view) {
    $('#aeControlsMenu').menu().position({
        my: 'left top',
        at: 'right top',
        of: $('#aeControlsMenuButtonContainer'),
        collision: 'none none'
    });

    var menuBlurTimer;

    $('#aeControlsMenu').on('menufocus', function(event, ui) {
        clearTimeout(menuBlurTimer);
    });

    $('#aeControlsMenu').on('menublur', function(event, ui) {
        menuBlurTimer = setTimeout(function() {
            $('#aeControlsMenu').hide('slide', 400);
        }, 200);
    });

    $('#aeControlsMenu').on('menuselect', function(event, ui){
        $('#aeControlsMenu').hide('slide', 400);
    });

    $('#aeControlsMenu').hide();

    $('#aeControlsMenuButton').click(function(){
        $('#aeControlsMenu').show('slide', 400);
        menuBlurTimer = setTimeout(function() {
            $('#aeControlsMenu').hide('slide', 400);
        }, 4000);
    });

    $('#searchMenuItem').parent("li").click(function(e){
        e.preventDefault();
        view.resetView();
    });

    $('#reportMenuItem').parent("li").click(function(e){
        e.preventDefault();
        view.doReport();
    });

    $('#aboutMenuItem').parent("li").click(function(e){
        e.preventDefault();
        view.showHelp(2);
    });

    $('#howItWorks').click(function(){
        view.showHelp(0);
    });

    $('#helpOverlayText').tabs();

    $("#rwAvyFormAccordian").accordion({heightStyle: "fill"});
    $("#rwAvyFormAccordian .ui-accordion-content").css("overflow", "hidden");
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

  $.widget("custom.avycomplete", $.ui.autocomplete, {
      _create: function() {
        this._super();
        this.widget().menu( "option", "items", "> :not(.ui-autocomplete-category)" );
      },
      _renderMenu: function(ul, items) {
          var self = this;
          var currentCategory = "";

           $.each(items, function(index, item) {
              if (item.category && item.category != currentCategory) {
                  ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
                  currentCategory = item.category;
              }

              self._renderItemData(ul, item);
          });
      },
      _renderItem: function( ul, item ) {
          var re = new RegExp('(' + this.element.val() + ')', 'gi');
          var html = item.label.replace(re, '<span style="background-color: #1978AB">$&</span>');
          return $( "<li></li>" )
              .data( "item.autocomplete", item )
              .append( $("<a></a>").html(html) )
              .appendTo( ul );
      }
  });

	$('.avyAutoComplete').avycomplete({
		minLength: 0,
		delay: 0,
		select: function(event, ui) {
			$(this).siblings(':hidden').val(ui.item.value);
			$(this).val(ui.item.label);
			if ($(this).hasClass('avyWindSpeedAutoComplete')) view.form.toggleWindDirectionFields(ui.item.value);
			if ($(this).hasClass('avyTriggerAutoComplete')) view.form.toggleTriggerCauseFields(ui.item.category)
			return false;
		}
	});

	$('#rwAvyFormDiv .avyAutoComplete').avycomplete('option', 'appendTo', '#rwAvyFormDiv');
   	$('#rwAvyFormDiv .avyAutoComplete').focus(function() {
   	    var containerScrollTop = $("#rwAvyFormDiv").scrollTop();
   	    var dropDownBottom = $(this).position().top + containerScrollTop + 300;
   	    var containerBottom = containerScrollTop + $("#rwAvyFormDiv").height();
   	    var unseenDropdown = dropDownBottom - containerBottom;
   	    if (unseenDropdown > 0) {
   	        $('#rwAvyFormDiv').scrollTop(containerScrollTop + unseenDropdown);
   	    }
    });

	$('.avyAutoComplete').change(function() {
		$(this).val('');
		$(this).siblings(':hidden').val('');
		if ($(this).hasClass('avyWindSpeedAutoComplete')) view.form.toggleWindDirectionFields('');
		if ($(this).hasClass('avyTriggerAutoComplete')) view.form.toggleTriggerCauseFields('');
	});

	$('.avyAutoComplete').focus(function() {
		$(this).avycomplete("search");
    });

	$('.avyAutoComplete').click(function() {
		$(this).select();
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
    $('.avyRSlider').slider({
        min: 0,
        max: 5,
        step: 1,
        range: 'min',
        create: function (event, ui) {
            $(this).siblings(".avyRDSliderValue").val(0);
        },
        slide: function (event, ui) {
            $(this).siblings(".avyRDSliderValue").val(ui.value);
        }
    });

    $('.avyDSlider').slider({
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

    $("#avyReportInitLocationButton").click(function() {
        $("#avyReportStep1").hide("slide", {"direction":"down"}, 600, function() {
            $("#avyReportStep2").slideDown("slow");
        });
    });

    $('#avyReportStartDrawingButton').click(function() {
        view.currentReport.startDrawing();
        $("#avyReportStep2").hide("slide", {"direction":"down"}, 600, function() {
            $("#avyReportStep3").slideDown("slow");
        });
    });

    $('#avyReportAcceptDrawingButton').click(function() {
        view.hideControls().then(function() {
            view.uploadCesiumScreenshot();
            view.form.toggleWindDirectionFields('');
            view.form.toggleTriggerCauseFields('');
            view.form.displayReadWriteForm();
        });
    });

    $('#avyReportRedrawButton').click(function() {
        view.currentReport.clearDrawing();
        $("#avyReportStep4").hide("slide", {"direction":"down"}, 600, function() {
            $("#avyReportStep2").slideDown("slow");
        });
    });

    $("#rwAvyFormSubmitButton").click(function() {
        $('#rwAvyFormDiv').children("form").submit();
    });

    $("#rwAvyFormCancelButton").click(function() {
        view.resetView();
    });

    $("#rwAvyFormDeleteButton").click(function() {
        if (confirm("Are you sure you want to delete report " + $('#rwAvyFormExtId').val())) {
            $('#rwAvyFormDeleteBinding').click();
            view.resetView();
        }
    });
}

function wireLocationInputs(view) {
    $('.avyLocation').avycomplete({
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
        minLength: 8
    });

    $('.avyLocation').keydown(function (event) {
        if (event.keyCode == 13) {
            $(this).blur();
            event.preventDefault();
        }
    });

	$('.avyLocation').blur(function(event) {
        if ($(this).siblings("#avyReportInitLocationButton").length) {
            $("#avyReportInitLocationButton").click();
        }
        view.geocodeAndFlyTo($(this).val(), -70.0, 6300.0);
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
        dialogClass: "multiDialog",
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
}

AvyEyesUI.prototype.raiseTheCurtain = function() {
    if ($('#loadingDiv').is(':visible')) {
        $('#loadingDiv').fadeOut(500);
    }
}

return AvyEyesUI;
});