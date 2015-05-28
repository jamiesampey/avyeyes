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
        $.each(a.images, function(i) {
			var imgUrl = '/rest/images/' + a.extId + '/' + a.images[i].filename;
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
    resetImageUploadForm();

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
    
    $.each(a.images, function(i) {
    var imgUrl = '/rest/images/' + a.extId + '/' + a.images[i].filename;
    $('#rwAvyFormImageTable').append('<tr id="' + a.images[i].filename + '">' 
    + '<td><a href="' + imgUrl + '" target="_blank">' + a.images[i].filename 
    + '</a></td><td>' + bytesToFileSize(a.images[i].size) + '<div class="rwAvyFormImageDeleteWrapper">'
    + '<input type="button" value="Delete" onclick="avyEyesView.currentReport.deleteImage(\''
    + a.extId + '\',\'' + a.images[i].filename + '\')"/></div></td></tr>');
    });
    
    $('#rwAvyFormDeleteBinding').val(a.extId);
    $('#rwAvyFormDialog').dialog('open');
}

function resetImageUploadForm() {
  $('#rwAvyFormImageTable > tbody').empty();

  var imgUploadUrl = '/rest/images/' + $('#rwAvyFormExtId').val();
  $("#rwAvyFormImageUploadForm").fileupload({dataType:'json', url:imgUploadUrl, dropZone:$('#rwAvyFormImageDropZone'),
      fail: function(e, data) {
        console.log("Error", data.errorThrown);
      },
      done: function(e, data) {
        $('#rwAvyFormImageTable').append('<tr><td>' + data.result.fileName + '</td><td>'
          + bytesToFileSize(data.result.fileSize) + '</td></tr>');
      }
  });
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
            $('#rwAvyFormDeleteConfirmDialog').dialog('open');
        }
    });
    $('#rwAvyFormDialog').dialog('option', 'buttons', reportDialogButtons);

    $('#rwAvyFormImageTable').show();

    $('#rwAvyFormViewableTd').css('display', 'table-cell');
    $('#rwAvyFormDeleteConfirmDialog').css('visibility', 'visible');

    $('#rwAvyFormDeleteConfirmDialog').dialog({
        title: "Confirm",
        minWidth: 500,
        autoOpen: false,
        modal: true,
        resizable: false,
        draggable: false,
        closeOnEscape: false,
        beforeclose: function(event, ui) {
            return false;
        },
        dialogClass: 'rwAvyFormDetailsDialog',
        open: function() {
            $('#rwAvyFormDeleteConfirmNo').focus();
        },
        buttons: [{
            text: 'Yes',
            click: function(event, ui) {
                $('#rwAvyFormDeleteBinding').click();
                view.resetView();
            }
        },
        {
            id: 'rwAvyFormDeleteConfirmNo',
            text: 'No',
            click: function(event, ui) {
                $(this).dialog('close');
            }
        }]
    });
}

AvyForm.deleteImage = function(extId, filename) {
  $.ajax({
    url: '/rest/images/' + extId + '/' + filename,
    type: 'DELETE',
    success: function(result) {
      $('#rwAvyFormImageTable').find('#' + filename).remove();
    },
    fail: function(jqxhr, textStatus, error) {
      var err = textStatus + ", " + error;
      alert('Failed to delete ' + filename + '. Error: ' + err);
    }
  });
}

AvyForm.highlightReportErrorFields = function(errorFields) {
    resetReportErrorFields();
    $.each(errorFields, function(i) {
        if (errorFields[i] === 'avyReportAngle') {
            $('#' + errorFields[i]).parent().css('border', '1px solid red');
        } else {
            $('#' + errorFields[i]).css('border', '1px solid red');
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
	$('#rwAvyFormImageTable > tbody').empty();
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