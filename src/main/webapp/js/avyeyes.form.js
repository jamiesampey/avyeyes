define(['lib/lightbox',
        'lib/jquery.fileupload',
        'lib/jquery.iframe-transport'
        ], function() {

var AvyForm = {};

AvyForm.displayReadOnlyForm = function(mousePos, a) {
    var title = a.avyDate + ': ' + a.areaName;

	$('#roAvyFormTitle').text(title);
	$('#roAvyFormSubmitterExp').text(a.submitterExp.label);

	$('#roAvyFormExtLink').attr('href', a.extUrl);
	$('#roAvyFormExtLink').text(a.extUrl);

    var fbContainer = $('#roAvyFormSocialFacebookContainer');
    fbContainer.empty();
    fbContainer.append('<div class="fb-share-button" data-layout="button_count" data-href="' + a.extUrl + '">');

    var twttrContainer = $('#roAvyFormSocialTwitterContainer');
    twttrContainer.empty();
    twttrContainer.append('<a class="twitter-share-button" data-url="' + a.extUrl + '" data-text="' + title
      + '" href="http://twitter.com/share" data-count="horizontal">');

	$('#roAvyFormElevation').text(a.elevation);
	$('#roAvyFormElevationFt').text(metersToFeet(a.elevation));
	$('#roAvyFormAspect').text(a.aspect.label);
	$('#roAvyFormAngle').text(a.angle);
	  
	$('#roAvyFormType').text(a.avyType.label);
	$('#roAvyFormTrigger').text(a.avyTrigger.label);
	$('#roAvyFormInterface').text(a.avyInterface.label);
	$('#roAvyFormRSize').text(a.rSize);
	$('#roAvyFormDSize').text(a.dSize);

	$('#roAvyFormSky').text(a.sky.label);
	$('#roAvyFormPrecip').text(a.precip.label);
	
	setReadOnlySpinnerVal('#roAvyFormNumCaught', a.caught);
	setReadOnlySpinnerVal('#roAvyFormNumPartiallyBuried', a.partiallyBuried);
	setReadOnlySpinnerVal('#roAvyFormNumFullyBuried', a.fullyBuried);
	setReadOnlySpinnerVal('#roAvyFormNumInjured', a.injured);
	setReadOnlySpinnerVal('#roAvyFormNumKilled', a.killed);
	$('#roAvyFormModeOfTravel').text(a.modeOfTravel.label);
	
	if (a.comments.length > 0) {
		$('#roAvyFormCommentsRow').show();
		$('#roAvyFormComments').html('<pre>' + a.comments.trim() + '</pre>');
	}

	if (a.images.length > 0) {
		$('#roAvyFormImageRow').show();
        $.each(a.images, function(i, image) {
			var imgUrl = getImageRestUrl(a.extId, image.filename);
			$('#roAvyFormImageList').append('<li class="roAvyFormImageListItem"><a href="' + imgUrl 
				+ '" data-lightbox="roAvyFormImages"><img src="' + imgUrl + '" /></a></li>');
		});
	}
	
	$('#roAvyFormDialog').dialog('option', 'position', {
    my: 'center bottom-20', 
    at: 'center top', 
    of: $.Event('click', {pageX: mousePos.x, pageY: mousePos.y}),
    collision: 'fit'
  });

  $('#roAvyFormDialog').dialog('open');

  FB.XFBML.parse(fbContainer[0]);
  twttr.widgets.load();
}

AvyForm.hideReadOnlyForm = function() {
    if($('#roAvyFormDialog').is(':visible')) {
        $('#roAvyFormCommentsRow').hide();
        $('#roAvyFormImageList').empty();
        $('#roAvyFormImageRow').hide();
        $('#roAvyFormDialog').dialog('close');
	}
}

AvyForm.displayReadWriteForm = function(a) {
    if (!a) { // new report case
        resetReadWriteImageUpload($('#rwAvyFormExtId').val());
        $('#rwAvyFormDialog').dialog('open');
        return;
    } else { // admin view case
        resetReadWriteImageUpload(a.extId);
    }

    $('#rwAvyFormExtId').val(a.extId);
    
    $('#rwAvyFormViewableTd').children(':checkbox').attr('checked', a.viewable);
    $('#rwAvyFormViewableTd').children(':checkbox').trigger('change');
    
    $('#rwAvyFormSubmitterEmail').val(a.submitterEmail);
    setReadWriteAutocompleteVal('#rwAvyFormSubmitterExp', a.submitterExp);
    
    $('#rwAvyFormAreaName').val(a.areaName);
    $('#rwAvyFormDate').val(a.avyDate);
    setReadWriteAutocompleteVal('#rwAvyFormSky', a.sky);
    setReadWriteAutocompleteVal('#rwAvyFormPrecip', a.precip);
    
    setReadWriteAutocompleteVal('#rwAvyFormType', a.avyType);
    setReadWriteAutocompleteVal('#rwAvyFormTrigger', a.avyTrigger);
    setReadWriteAutocompleteVal('#rwAvyFormInterface', a.avyInterface);
    setReadWriteSliderVal('#rwAvyFormRsizeValue', a.rSize);
    setReadWriteSliderVal('#rwAvyFormDsizeValue', a.dSize);
    
    $('#rwAvyFormElevation').val(a.elevation);
    $('#rwAvyFormElevationFt').val(metersToFeet(a.elevation));
    setReadWriteAutocompleteVal('#rwAvyFormAspect', a.aspect);
    $('#rwAvyFormAngle').val(a.angle);
    
    setReadWriteSpinnerVal('#rwAvyFormNumCaught', a.caught);
    setReadWriteSpinnerVal('#rwAvyFormNumPartiallyBuried', a.partiallyBuried);
    setReadWriteSpinnerVal('#rwAvyFormNumFullyBuried', a.fullyBuried);
    setReadWriteSpinnerVal('#rwAvyFormNumInjured', a.injured);
    setReadWriteSpinnerVal('#rwAvyFormNumKilled', a.killed);
    setReadWriteAutocompleteVal('#rwAvyFormModeOfTravel', a.modeOfTravel);
    
    $('#rwAvyFormComments').val(a.comments);
    
    $.each(a.images, function(i, image) {
        appendImageToReadWriteForm(a.extId, image.filename, image.size);
    });
    
    $('#rwAvyFormDeleteBinding').val(a.extId);
    $('#rwAvyFormDialog').dialog('open');
}

function resetReadWriteImageUpload(extId) {
    $('#rwAvyFormImageGrid').empty();

    var imgUploadUrl = '/rest/images/' + extId;
    $("#rwAvyFormImageUploadForm").fileupload({
        dataType:'json',
        url:imgUploadUrl,
        dropZone:$('#rwAvyFormImageDropZone'),
        fail: function(e, data) {
            console.log("Error", data.errorThrown);
        },
        done: function(e, data) {
            appendImageToReadWriteForm(extId, data.result.filename, data.result.size);
        }
    });
}

function appendImageToReadWriteForm(extId, filename, size) {
    var imageUniqueId = getImageUniqueId(extId, filename);
    var imgUrl = getImageRestUrl(extId, filename);

    var imageTableData = '<div id=\'' + imageUniqueId + '\' class=\'rwAvyFormImageCell\'>'
        + '<div class=\'rwAvyFormImageWrapper\'><a href=\'' + imgUrl + '\' data-lightbox=\'rwAvyFormImages\' data-title=\'' + filename + ' - '
        + bytesToFileSize(size) + '\'><img class=\'rwAvyFormImage\' src=\'' + imgUrl + '\' /></a>'
        + '<img id=\'' + getImageDeleteIconUniqueId(imageUniqueId) + '\' class=\'rwAvyFormImageDeleteIcon\''
        + 'src=\'/images/img-delete-icon.png\' /></div></div>';

    var lastTableRow = $('#rwAvyFormImageGrid .rwAvyFormImageRow:last');
    if(!lastTableRow.length || lastTableRow.find('.rwAvyFormImageCell').length >= 4) {
        $('#rwAvyFormImageGrid').append('<div class=\'rwAvyFormImageRow\'>');
    }

    $('#rwAvyFormImageGrid .rwAvyFormImageRow:last').append(imageTableData);
    setImageDeleteOnClick(extId, filename);
}

function removeImageFromReadWriteForm(imageUniqueId) {
    $('#rwAvyFormImageGrid').find('#' + imageUniqueId).remove();
    $('#rwAvyFormImageGrid .rwAvyFormImageRow').each(function(i) {
        var images = $(this).find('.rwAvyFormImageCell');
        if (images.length < 4) {
            var nextImageRow = $(this).next();
            if (!nextImageRow.length) return;

            var nextImage = nextImageRow.find('.rwAvyFormImageCell').first();
            $(this).append(nextImage.clone(true));
            nextImage.remove();

            if (nextImageRow.find('.rwAvyFormImageCell').length == 0) {
                nextImageRow.remove();
            }
        }
    });
}

function setImageDeleteOnClick(extId, filename) {
    var imageUniqueId = getImageUniqueId(extId, filename);
    $('#' + getImageDeleteIconUniqueId(imageUniqueId)).click(function() {
        if (confirm('Delete image ' + filename + '?')) {
            $.ajax({
                url: getImageRestUrl(extId, filename),
                type: 'DELETE',
                success: function(result) {
                    removeImageFromReadWriteForm(imageUniqueId);
                },
                fail: function(jqxhr, textStatus, error) {
                    alert('Failed to delete ' + filename + '. Error: ' + textStatus + ", " + error);
                }
            });
        }
    });
}

function getImageRestUrl(extId, filename) {
    return '/rest/images/' + extId + '/' + filename;
}

function getImageUniqueId(extId, filename) {
    return extId + '-' + filename;
}

function getImageDeleteIconUniqueId(imageUniqueId) {
    return imageUniqueId + "-delete";
}

AvyForm.wireReadWriteFormAdminControls = function(view) {
    $('#rwAvyFormViewableTd').children(':checkbox').change(function(){
        if ($(this).is(':checked')) {
            $('#rwAvyFormViewableTd').css('background', 'rgba(0, 255, 0, 0.3)');
        } else {
            $('#rwAvyFormViewableTd').css('background', 'rgba(255, 0, 0, 0.3)');
        }
    });

    var reportDialogButtons = $('#rwAvyFormDialog').dialog("option", "buttons");
    if (reportDialogButtons.length > 3) return; // already wired admin fields

    reportDialogButtons.push({
        text: "Delete",
        click: function(event, ui) {
            if (confirm("Are you sure you want to delete report " + $('#rwAvyFormExtId').val())) {
                $('#rwAvyFormDeleteBinding').click();
                view.resetView();
            }
        }
    });
    $('#rwAvyFormDialog').dialog('option', 'buttons', reportDialogButtons);

    $('#rwAvyFormViewableTd').css('display', 'table-cell');
}

AvyForm.highlightReportErrorFields = function(errorFields) {
    resetReportErrorFields();
    $.each(errorFields, function(i, fieldId) {
        if (fieldId === 'rwAvyFormAngle') {
            $('#' + fieldId).parent().css('border', '1px solid red');
        } else {
            $('#' + fieldId).css('border', '1px solid red');
        }
    });
}

function resetReportErrorFields() {
    $('#rwAvyFormSubmitterEmail').css('border', '1px solid #555555');
    $('#rwAvyFormSubmitterExpAC').css('border', '1px solid #555555');
    $('#rwAvyFormAspectAC').css('border', '1px solid #555555');
    $('#rwAvyFormAreaName').css('border', '1px solid #555555');
    $('#rwAvyFormDate').css('border', '1px solid #555555');
    $('#rwAvyFormAngle').parent().css('border', '1px solid #555555');
}

AvyForm.clearReportFields = function() {
    resetReportErrorFields();
    setReportDrawingInputs('', '', '', '', '', '');
	$('#rwAvyFormDialog').find('input:text, input:hidden, textarea').val('');
	$('#rwAvyFormDialog').find('.avyRDSliderValue').val('0');
	$('#rwAvyFormDialog').find('.avyRDSlider').slider('value', 0);
	$('#rwAvyFormImageGrid').empty();
	$('#rwAvyFormDrawButtonContainer').css('visibility', 'hidden');
}

AvyForm.setReportDrawingInputs = setReportDrawingInputs;
function setReportDrawingInputs(lng, lat, elevation, aspect, angle, coordStr) {
	$('#rwAvyFormLng').val(lng);
	$('#rwAvyFormLat').val(lat);
	$('#rwAvyFormElevation').val(elevation);
	$('#rwAvyFormElevationFt').val(metersToFeet(elevation));
	$('#rwAvyFormAspectAC').val(aspect);
	$('#rwAvyFormAspect').val(aspect);
	$('#rwAvyFormAngle').val(angle);
	$('#rwAvyFormCoords').val(coordStr);
}

AvyForm.closeReportDialogs = function() {
    $('.avyReportDrawDialog, .rwAvyFormDialog')
        .children('.ui-dialog-content').dialog('close');
}

AvyForm.toggleTechnicalReportFields = function(enabled) {
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

function setReadOnlySpinnerVal(inputElem, value) {
    if (value == -1) {
        $(inputElem).text('Unknown');
    } else {
        $(inputElem).text(value);
    }
}

function setReadWriteAutocompleteVal(hiddenSibling, enumObj) {
  $(hiddenSibling).val(enumObj.value);
  $(hiddenSibling).siblings('.avyAutoComplete').val(enumObj.label);
}

function setReadWriteSliderVal(inputElem, value) {
  $(inputElem).val(value);
  $(inputElem).siblings('.avyRDSlider').slider('value', value);
}

function setReadWriteSpinnerVal(inputElem, value) {
  if (value == -1) {
    $(inputElem).val('');
  } else {
    $(inputElem).val(value);
  }
}

function metersToFeet(meters) {
    return Math.round(meters * 3.28084);
}

function bytesToFileSize(numBytes) {
    var thresh = 1000;
    if(numBytes < thresh) return numBytes + ' B';

    var units = ['KB','MB','GB'];
    var u = -1;

    do {
        numBytes /= thresh;
        ++u;
    } while(numBytes >= thresh);
    return numBytes.toFixed(1) + ' ' + units[u];
}

return AvyForm;
});