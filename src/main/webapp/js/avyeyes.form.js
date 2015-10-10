define(['lib/jquery.fancybox',
        "lib/jquery.fileupload",
        "lib/jquery.iframe-transport",
        "//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js"
        ], function() {

function AvyForm(){};

AvyForm.prototype.displayReadOnlyForm = function(mousePos, a) {
    var title = a.date + ": " + a.areaName;

	$("#roAvyFormTitle").text(title);
	$("#roAvyFormSubmitterExp").text(a.submitterExp.label);

	$("#roAvyFormExtLink").attr("href", a.extUrl);
	$("#roAvyFormExtLink").text(a.extUrl);

    var fbContainer = $("#roAvyFormSocialFacebookContainer");
    fbContainer.empty();
    fbContainer.append("<div class='fb-share-button' data-layout='button_count' data-href='" + a.extUrl + "' />");

    var twttrContainer = $("#roAvyFormSocialTwitterContainer");
    twttrContainer.empty();
    twttrContainer.append("<a class='twitter-share-button' data-url='" + a.extUrl + "' data-text='" + title
      + "' href='http://twitter.com/share' data-count='horizontal' />");

	$("#roAvyFormElevation").text(a.slope.elevation);
	$("#roAvyFormElevationFt").text(metersToFeet(a.slope.elevation));
	$("#roAvyFormAspect").text(a.slope.aspect.label);
	$("#roAvyFormAngle").text(a.slope.angle);
	  
	$("#roAvyFormType").text(a.classification.avyType.label);
	$("#roAvyFormTrigger").text(a.classification.trigger.label);
	$("#roAvyFormInterface").text(a.classification.interface.label);
	$("#roAvyFormRSize").text(a.classification.rSize);
	$("#roAvyFormDSize").text(a.classification.dSize);

	$("#roAvyFormSky").text(a.scene.skyCoverage.label);
	$("#roAvyFormPrecip").text(a.scene.precipitation.label);
	
	setReadOnlySpinnerVal("#roAvyFormNumCaught", a.humanNumbers.caught);
	setReadOnlySpinnerVal("#roAvyFormNumPartiallyBuried", a.humanNumbers.partiallyBuried);
	setReadOnlySpinnerVal("#roAvyFormNumFullyBuried", a.humanNumbers.fullyBuried);
	setReadOnlySpinnerVal("#roAvyFormNumInjured", a.humanNumbers.injured);
	setReadOnlySpinnerVal("#roAvyFormNumKilled", a.humanNumbers.killed);
	$("#roAvyFormModeOfTravel").text(a.humanNumbers.modeOfTravel.label);

    var showComments = a.comments.length > 0;
    var showImages = a.images.length > 0;

	if (showComments) {
		$("#roAvyFormCommentsRow").show();
		$("#roAvyFormComments").val(a.comments);
        $("#roAvyFormCommentsLightboxDiv textarea").val(a.comments);
        $(".commentsFancybox").fancybox({padding: 0, openEffect: "fade", closeEffect: "fade"});
	}

	if (showImages) {
	    var s3Bucket = $("#s3ImageBucket").val();
		$("#roAvyFormImageRow").show();

        $.each(a.images, function(i, image) {
            var imgUrl = "//" + s3Bucket + ".s3.amazonaws.com/" + a.extId + "/" + image.filename;
			$("#roAvyFormImageList").append("<li class='roAvyFormImageListItem'><a href='"
                + imgUrl + "' class='imgFancybox' rel='roAvyFormImages'><img src='" + imgUrl + "' /></a></li>");
		});

        setImgFancyBox();
	}

	$("#roAvyFormDialog").dialog("option", "position", {
        my: "center bottom-20",
        at: "center top",
        of: $.Event("click", {pageX: mousePos.x, pageY: mousePos.y}),
        collision: "fit"
    });

    $("#cesiumContainer").css("cursor", "default");
    $("#roAvyFormDialog").dialog("open");
    $("#roAvyFormDialog, .roAvyFormDialog").height(360);

    FB.XFBML.parse(fbContainer[0]);
    twttr.widgets.load();

    $("#roAvyFormDialog, .roAvyFormDialog").height(function (index, height) {
        if (showComments) height += 200;
        if (showImages) height += 120;
        return height;
    });
}

AvyForm.prototype.hideReadOnlyForm = function() {
    if($('#roAvyFormDialog').is(':visible')) {
        $('#roAvyFormCommentsRow').hide();
        $('#roAvyFormImageList').empty();
        $('#roAvyFormImageRow').hide();
        $('#roAvyFormDialog').dialog('close');
	}
}

AvyForm.prototype.displayReadWriteForm = function(a) {
    if (!a) { // new report case
        this.resetReadWriteImageUpload($('#rwAvyFormExtId').val());
        $('#rwAvyFormDialog').dialog('open');
        return;
    } else { // admin view case
        this.resetReadWriteImageUpload(a.extId);
    }

    $('#rwAvyFormExtId').val(a.extId);
    
    $('#rwAvyFormViewableTd').children(':checkbox').attr('checked', a.viewable);
    $('#rwAvyFormViewableTd').children(':checkbox').trigger('change');
    
    $('#rwAvyFormSubmitterEmail').val(a.submitterEmail);
    this.setReadWriteAutocompleteVal('#rwAvyFormSubmitterExp', a.submitterExp);

    $('#rwAvyFormAreaName').val(a.areaName);
    $('#rwAvyFormDate').val(a.date);

    this.setReadWriteAutocompleteVal('#rwAvyFormSky', a.scene.skyCoverage);
    this.setReadWriteAutocompleteVal('#rwAvyFormPrecip', a.scene.precipitation);
    
    this.setReadWriteAutocompleteVal('#rwAvyFormType', a.classification.avyType);
    this.setReadWriteAutocompleteVal('#rwAvyFormTrigger', a.classification.trigger);
    this.setReadWriteAutocompleteVal('#rwAvyFormInterface', a.classification.interface);
    this.setReadWriteSliderVal('#rwAvyFormRsizeValue', a.classification.rSize);
    this.setReadWriteSliderVal('#rwAvyFormDsizeValue', a.classification.dSize);

    $('#rwAvyFormElevation').val(a.slope.elevation);
    $('#rwAvyFormElevationFt').val(metersToFeet(a.slope.elevation));
    this.setReadWriteAutocompleteVal('#rwAvyFormAspect', a.slope.aspect);
    $('#rwAvyFormAngle').val(a.slope.angle);

    this.setReadWriteSpinnerVal('#rwAvyFormNumCaught', a.humanNumbers.caught);
    this.setReadWriteSpinnerVal('#rwAvyFormNumPartiallyBuried', a.humanNumbers.partiallyBuried);
    this.setReadWriteSpinnerVal('#rwAvyFormNumFullyBuried', a.humanNumbers.fullyBuried);
    this.setReadWriteSpinnerVal('#rwAvyFormNumInjured', a.humanNumbers.injured);
    this.setReadWriteSpinnerVal('#rwAvyFormNumKilled', a.humanNumbers.killed);
    this.setReadWriteAutocompleteVal('#rwAvyFormModeOfTravel', a.humanNumbers.modeOfTravel);
    
    $('#rwAvyFormComments').val(a.comments);
    
    $.each(a.images, function(i, image) {
        var imageCellId = getFileBaseName(image.filename);
        appendImageCellToReadWriteForm(imageCellId);
        this.setImageCellContent(imageCellId, a.extId, image.filename);
    }.bind(this));

    setImgFancyBox();

    $('#rwAvyFormDeleteBinding').val(a.extId);
    $('#rwAvyFormDialog').dialog('open');
    $("#cesiumContainer").css("cursor", "default");
}

AvyForm.prototype.resetReadWriteImageUpload = function(extId) {
    $('#rwAvyFormImageGrid').empty();

    var tempImageCellId = function(filename) {
        return extId + "-" + getFileBaseName(filename);
    }

    $("#rwAvyFormImageUploadForm").fileupload({
        dataType:'json',
        url: "/rest/images/" + extId,
        dropZone:$('#rwAvyFormImageDropZone'),
        add: function (e, data) {
            appendImageCellToReadWriteForm(tempImageCellId(data.files[0].name));
            data.submit();
        },
        done: function(e, data) {
            var newImageCellId = getFileBaseName(data.result.filename);
            $("#" + tempImageCellId(data.result.origFilename)).attr("id", newImageCellId);
            this.setImageCellContent(newImageCellId, extId, data.result.filename);
        }.bind(this),
        fail: function(e, data) {
            console.log("Error", data.errorThrown);
        }
    });
}

function appendImageCellToReadWriteForm(imageCellId) {
    var lastTableRow = $('#rwAvyFormImageGrid .rwAvyFormImageRow:last');
    if(!lastTableRow.length || lastTableRow.find('.rwAvyFormImageCell').length >= 4) {
        $('#rwAvyFormImageGrid').append("<div class='rwAvyFormImageRow'>");
    }

    $('#rwAvyFormImageGrid .rwAvyFormImageRow:last')
        .append("<div id='" + imageCellId + "' class='rwAvyFormImageCell'>"
            + "<span style='display: inline-block; height: 100%; vertical-align: middle;'></span>"
            + "<img src='/images/spinner-image-upload.gif' style='vertical-align: middle;'/></div>");
}

AvyForm.prototype.setImageCellContent = function(imageCellId, extId, filename) {
    var imageUrl = getSignedImageUrl(extId, filename)
    var imageDeleteIconId = getImageDeleteIconUniqueId(imageCellId);

    $("#" + imageCellId).empty();
    $("#" + imageCellId).append("<div class='rwAvyFormImageWrapper'><a href='" + imageUrl
        + "' class='imgFancybox' rel='rwAvyFormImages'><img class='rwAvyFormImage' src='" + imageUrl
        + "' /></a><img id='" + imageDeleteIconId + "' class='rwAvyFormImageDeleteIcon' "
        + "src='/images/img-delete-icon.png' /></div>");

    $('#' + imageDeleteIconId).click(function() {
        if (confirm('Delete image ' + filename + ' from avalanche ' + extId + '?')) {
            $.ajax({
                url: getImageRestUrl(extId, filename),
                type: 'DELETE',
                success: function(result) {
                    removeImageFromReadWriteForm(imageCellId);
                },
                fail: function(jqxhr, textStatus, error) {
                    alert('Failed to delete ' + filename + '. Error: ' + textStatus + ", " + error);
                }
            });
        }
    });
}

function removeImageFromReadWriteForm(imageUniqueId) {
    $('#rwAvyFormImageGrid').find('#' + imageUniqueId).remove();
    $('#rwAvyFormImageGrid .rwAvyFormImageRow').each(function(i) {
        var images = $(this).find('.rwAvyFormImageCell');
        if (images.length < 4) {
            var nextImageRow = $(this).next();
            if (!nextImageRow.length) return;

            var nextImage = nextImageRow.find('.rwAvyFormImageCell').first();
            $(this).append(nextImage.detach());

            if (nextImageRow.find('.rwAvyFormImageCell').length === 0) {
                nextImageRow.remove();
            }
        }
    });
}

var s3Client;
function getSignedImageUrl(extId, filename) {
    if (!s3Client) {
        s3Client = new AWS.S3({
            accessKeyId: 'AKIAIGF6JECD4PNKHYOQ',
            secretAccessKey: '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'
        });
    }

    var s3Bucket = $("#s3ImageBucket").val();

    return s3Client.getSignedUrl('getObject', {
        Bucket: s3Bucket, Key: extId + '/' + filename});
}

function getImageRestUrl(extId, filename) {
    return '/rest/images/' + extId + '/' + getFileBaseName(filename);
}

function getFileBaseName(filename) {
    return filename.substring(0, filename.lastIndexOf('.'));
}

function getImageDeleteIconUniqueId(imageCellId) {
    return imageCellId + "-delete";
}

function setImgFancyBox() {
    $(".imgFancybox").fancybox({padding: 0, openEffect: 'elastic', closeEffect: 'elastic'});
}

AvyForm.prototype.wireReadWriteFormAdminControls = function(view) {
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

AvyForm.prototype.highlightReportErrorFields = function(errorFields) {
    this.resetReportErrorFields();
    $.each(errorFields, function(i, fieldId) {
        if (fieldId === 'rwAvyFormAngle') {
            $('#' + fieldId).parent().css('border', '1px solid red');
        } else {
            $('#' + fieldId).css('border', '1px solid red');
        }
    });
}

AvyForm.prototype.resetReportErrorFields = function() {
    $('#rwAvyFormSubmitterEmail').css('border', '1px solid #7D7D7D');
    $('#rwAvyFormSubmitterExpAC').css('border', '1px solid #7D7D7D');
    $('#rwAvyFormAspectAC').css('border', '1px solid #7D7D7D');
    $('#rwAvyFormAreaName').css('border', '1px solid #7D7D7D');
    $('#rwAvyFormDate').css('border', '1px solid #7D7D7D');
    $('#rwAvyFormAngle').parent().css('border', '1px solid #7D7D7D');
}

AvyForm.prototype.clearReportFields = function() {
    this.resetReportErrorFields();
    this.setReportDrawingInputs('', '', '', '', '', '');
	$('#rwAvyFormDialog').find('input:text, input:hidden, textarea').val('');
	$('#rwAvyFormDialog').find('.avyRDSliderValue').val('0');
	$('#rwAvyFormDialog').find('.avyRDSlider').slider('value', 0);
	$('#rwAvyFormImageGrid').empty();
	$('#avyReportDrawButtonContainer').css('visibility', 'hidden');
}

AvyForm.prototype.setReportDrawingInputs = function setReportDrawingInputs(lng, lat, elevation, aspect, angle, coordStr) {
	$('#rwAvyFormLng').val(lng);
	$('#rwAvyFormLat').val(lat);
	$('#rwAvyFormElevation').val(elevation);
	$('#rwAvyFormElevationFt').val(metersToFeet(elevation));
	$('#rwAvyFormAspectAC').val(aspect);
	$('#rwAvyFormAspect').val(aspect);
	$('#rwAvyFormAngle').val(angle);
	$('#rwAvyFormCoords').val(coordStr);
}

AvyForm.prototype.closeReportDialogs = function() {
    $('.avyReportDrawDialog, .rwAvyFormDialog')
        .children('.ui-dialog-content').dialog('close');
}

AvyForm.prototype.toggleTechnicalReportFields = function(enabled) {
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

AvyForm.prototype.setReadWriteAutocompleteVal = function(hiddenSibling, enumObj) {
  $(hiddenSibling).val(enumObj.value);
  $(hiddenSibling).siblings('.avyAutoComplete').val(enumObj.label);
}

AvyForm.prototype.setReadWriteSliderVal = function(inputElem, value) {
  $(inputElem).val(value);
  $(inputElem).siblings('.avyRDSlider').slider('value', value);
}

AvyForm.prototype.setReadWriteSpinnerVal = function(inputElem, value) {
  if (value == -1) {
    $(inputElem).val('');
  } else {
    $(inputElem).val(value);
  }
}

function metersToFeet(meters) {
    return Math.round(meters * 3.28084);
}

return AvyForm;
});