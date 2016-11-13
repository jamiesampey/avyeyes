define(['lib/jquery.fancybox',
        "lib/jquery.fileupload",
        "lib/jquery.iframe-transport",
        "//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js"
        ], function() {

function AvyForm(avyEyesView) {
    this.view = avyEyesView;
}

AvyForm.prototype.displayReadOnlyForm = function(mousePos, a) {
    var title = a.date + ": " + a.areaName;

	$("#roAvyFormTitle").text(title);
	$("#roAvyFormSubmitterExp").text(a.submitterExp.label);

	$("#roAvyFormExtLink").attr("href", a.extUrl);
	$("#roAvyFormExtLink").text(a.extUrl);

    var fbContainer = $("#roAvyFormSocialFacebookContainer");
    fbContainer.empty();
    fbContainer.append("<div class='fb-share-button' data-layout='button' data-href='" + a.extUrl + "' />");

    var twttrContainer = $("#roAvyFormSocialTwitterContainer");
    twttrContainer.empty();
    twttrContainer.append("<a class='twitter-share-button' data-url='" + a.extUrl + "' data-text='" + title
      + "' href='http://twitter.com/share' data-count='horizontal' />");

	$("#roAvyFormElevation").text(a.slope.elevation + " m");
	$("#roAvyFormElevationFt").text(metersToFeet(a.slope.elevation) + " ft");
	$("#roAvyFormAspect").text(a.slope.aspect.label);
	$("#roAvyFormAngle").text(a.slope.angle);

 	setReadOnlySpinnerVal("#roAvyFormRecentSnow", a.weather.recentSnow, "cm");
   	setReadOnlyAutoCompleteVal("#roAvyFormRecentWindSpeed", a.weather.recentWindSpeed);
    if (a.weather.recentWindSpeed.value == 'empty' || a.weather.recentWindDirection.value == 'empty') {
        $("#roAvyFormRecentWindDirectionText").hide();
        $("#roAvyFormRecentWindDirection").hide();
    } else {
        $("#roAvyFormRecentWindDirection").text(a.weather.recentWindDirection.label);
        $("#roAvyFormRecentWindDirectionText").show();
        $("#roAvyFormRecentWindDirection").show();
    }

	setReadOnlyAutoCompleteVal("#roAvyFormType", a.classification.avyType);
	setReadOnlyAutoCompleteVal("#roAvyFormTrigger", a.classification.trigger);
	setReadOnlyAutoCompleteVal("#roAvyFormTriggerModifier", a.classification.triggerModifier);
	setReadOnlyAutoCompleteVal("#roAvyFormInterface", a.classification.interface);
	setReadOnlySliderVal("#roAvyFormRSize", a.classification.rSize);
	setReadOnlySliderVal("#roAvyFormDSize", a.classification.dSize);

	setReadOnlySpinnerVal("#roAvyFormNumCaught", a.humanNumbers.caught);
	setReadOnlySpinnerVal("#roAvyFormNumPartiallyBuried", a.humanNumbers.partiallyBuried);
	setReadOnlySpinnerVal("#roAvyFormNumFullyBuried", a.humanNumbers.fullyBuried);
	setReadOnlySpinnerVal("#roAvyFormNumInjured", a.humanNumbers.injured);
	setReadOnlySpinnerVal("#roAvyFormNumKilled", a.humanNumbers.killed);

	setReadOnlyAutoCompleteVal("#roAvyFormModeOfTravel", a.humanNumbers.modeOfTravel);

    var showComments = a.comments.length > 0;
    var showImages = a.images.length > 0;

	if (showComments) {
		$("#roAvyFormCommentsRow").show();
		$("#roAvyFormComments").val(a.comments);
        $("#roAvyFormCommentsLightboxDiv textarea").val(a.comments);
        $(".commentsFancybox").fancybox({padding: 0, openEffect: "fade", closeEffect: "fade"});
	} else {
	    $("#roAvyFormCommentsRow").hide();
	}

	if (showImages) {
	    $("#roAvyFormImageList").empty();
	    var s3Bucket = $("#s3ImageBucket").val();
		$("#roAvyFormImageRow").show();

        $.each(a.images, function(i, image) {
            var imgUrl = "//" + s3Bucket + ".s3.amazonaws.com/" + a.extId + "/" + image.filename;
            var caption = (typeof image.caption != "undefined") ? image.caption : "";
			$("#roAvyFormImageList").append("<li class='roAvyFormImageListItem'>"
			    + "<a href='" + imgUrl + "' class='roAvyFormImageAnchor' rel='roAvyFormImages'><img src='" + imgUrl + "' /></a>"
                + "<div class='captionContainer' style='display: none;'>" + caption + "</div></li>");
		});

        setImageFancyBox('.roAvyFormImageAnchor');
	} else {
	    $("#roAvyFormImageRow").hide();
	}

    $("#cesiumContainer").css("cursor", "default");
    $("#roAvyFormDialog").dialog("open");

    $("#roAvyFormDialog, .roAvyFormDialog").height(function (index, height) {
        var baseHeight = 280;
        var commentsHeight = showComments ? 200 : 0;
        var imagesHeight = showImages ? 135 : 0;
        return baseHeight + commentsHeight + imagesHeight;
    });

    $("#roAvyFormDialog").dialog("option", "position", {
        my: "center bottom-20",
        at: "center top",
        of: $.Event("click", {pageX: mousePos.x, pageY: mousePos.y}),
        collision: "fit"
    });

    FB.XFBML.parse(fbContainer[0]);
    twttr.widgets.load();
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
        $('#rwAvyFormOverlay').css('visibility', 'visible');
        $('#rwAvyFormAreaName').focus();
        return;
    }

    this.resetReadWriteImageUpload(a.extId);
    $('#rwAvyFormExtId').val(a.extId);

    $('#rwAvyFormAdminTd').find(':checkbox').attr('checked', a.viewable);

    $('#rwAvyFormSubmitterEmail').val(a.submitterEmail);
    this.setReadWriteAutocompleteVal('#rwAvyFormSubmitterExp', a.submitterExp);

    $('#rwAvyFormAreaName').val(a.areaName);
    $('#rwAvyFormDate').val(a.date);

    $('#rwAvyFormElevation').val(a.slope.elevation);
    $('#rwAvyFormElevationFt').val(metersToFeet(a.slope.elevation));
    this.setReadWriteAutocompleteVal('#rwAvyFormAspect', a.slope.aspect);
    $('#rwAvyFormAngle').val(a.slope.angle);

    this.setReadWriteSpinnerVal('#rwAvyFormRecentSnow', a.weather.recentSnow);
    this.setReadWriteAutocompleteVal('#rwAvyFormRecentWindSpeed', a.weather.recentWindSpeed);
    this.setReadWriteAutocompleteVal('#rwAvyFormRecentWindDirection', a.weather.recentWindDirection);

    this.setReadWriteAutocompleteVal('#rwAvyFormType', a.classification.avyType);
    this.setReadWriteAutocompleteVal('#rwAvyFormTrigger', a.classification.trigger);
    this.setReadWriteAutocompleteVal('#rwAvyFormTriggerModifier', a.classification.triggerModifier);
    this.setReadWriteAutocompleteVal('#rwAvyFormInterface', a.classification.interface);
    this.setReadWriteSliderVal('#rwAvyFormRsizeValue', a.classification.rSize);
    this.setReadWriteSliderVal('#rwAvyFormDsizeValue', a.classification.dSize);

    this.setReadWriteSpinnerVal('#rwAvyFormNumCaught', a.humanNumbers.caught);
    this.setReadWriteSpinnerVal('#rwAvyFormNumPartiallyBuried', a.humanNumbers.partiallyBuried);
    this.setReadWriteSpinnerVal('#rwAvyFormNumFullyBuried', a.humanNumbers.fullyBuried);
    this.setReadWriteSpinnerVal('#rwAvyFormNumInjured', a.humanNumbers.injured);
    this.setReadWriteSpinnerVal('#rwAvyFormNumKilled', a.humanNumbers.killed);
    this.setReadWriteAutocompleteVal('#rwAvyFormModeOfTravel', a.humanNumbers.modeOfTravel);
    
    $('#rwAvyFormComments').val(a.comments);
    
    $.each(a.images, function(i, image) {
        var imageCellId = getFileBaseName(image.filename);
        this.appendImageCellToReadWriteForm(imageCellId);
        this.setImageCellContent(imageCellId, a.extId, image);
    }.bind(this));

    $('#rwAvyFormDeleteBinding').val(a.extId);
    $('#rwAvyFormOverlay').css('visibility', 'visible');
    $("#cesiumContainer").css("cursor", "default");
}

AvyForm.prototype.resetReadWriteImageUpload = function(extId) {
    $('#rwAvyFormImageGrid').empty();

    var tempImageCellId = function(filename) {
        return extId + "-" + getFileBaseName(filename);
    }

    $('#rwAvyFormImageGrid').sortable({
        items: '> .rwAvyFormImageCell',
        update: function(event, ui) {
            $.ajax({
                type: "PUT",
                contentType : 'application/json',
                url: this.getImageRestUrl(extId),
                data: JSON.stringify({
                    "order": $('#rwAvyFormImageGrid').sortable('toArray')
                }),
                success: function(result) {
                    console.log("Successful image order call");
                },
                fail: function(jqxhr, textStatus, error) {
                    alert("Failed to set image order. Error: " + textStatus + ", " + error);
                }
            });
        }.bind(this)
    });

    $("#rwAvyFormImageUploadForm").fileupload({
        dataType:'json',
        url: this.getImageRestUrl(extId),
        dropZone:$('#rwAvyFormImageDropZone'),
        add: function (e, data) {
            if ($("#rwAvyFormImageGrid").find(".rwAvyFormImageCell").length >= 20) {
                alert("Image limit exceeded. There is a max of 20 images per report.");
            } else if (data.files[0].size && data.files[0].size > 5000000) {
                alert("Image " + data.files[0].name + " is too big. Images must be less than 5 MB.");
            } else {
                this.appendImageCellToReadWriteForm(tempImageCellId(data.files[0].name));
                setTimeout(function() { data.submit(); }, 800);
            }
        }.bind(this),
        done: function(e, data) {
            var newImageCellId = getFileBaseName(data.result.filename);
            $("#" + tempImageCellId(data.result.origFilename)).attr("id", newImageCellId);
            this.setImageCellContent(newImageCellId, extId, data.result);
        }.bind(this),
        fail: function(e, data) {
            console.log("Error", data.errorThrown);
        }
    });
}

AvyForm.prototype.appendImageCellToReadWriteForm = function(filenameBase) {
    $('#rwAvyFormImageGrid').append("<div id='" + filenameBase + "' class='rwAvyFormImageCell'>"
        + "<span style='display: inline-block; height: 100%; vertical-align: middle;'></span>"
        + "<img src='/images/spinner-image-upload.gif' style='vertical-align: middle;'/></div>");
    $('#rwAvyFormImageGrid').sortable('refresh');
}

AvyForm.prototype.setImageCellContent = function(imageCellId, extId, image) {
    var imageUrl = this.getSignedImageUrl(extId, image.filename)
    var imageAnchorId = imageCellId + "-anchor";
    var imageEditIconId = imageCellId + "-edit";
    var imageDeleteIconId = imageCellId + "-delete";
    var existingCaption = (typeof image.caption != "undefined") ? image.caption : "";

    $("#" + imageCellId).empty();
    $("#" + imageCellId).append("<div class='rwAvyFormImageWrapper'>"
        + "<a id='" + imageAnchorId + "' href='" + imageUrl + "' rel='rwAvyFormImages'><img class='rwAvyFormImage' src='" + imageUrl + "' /></a>"
        + "<div class='captionContainer' style='display: none;'>" + existingCaption + "</div>"
        + "<img id='" + imageEditIconId + "' class='rwAvyFormImageEditIcon' src='/images/img-edit-icon.png' />"
        + "<img id='" + imageDeleteIconId + "' class='rwAvyFormImageDeleteIcon' src='/images/img-delete-icon.png' />"
        + "</div>");

    setImageFancyBox('#' + imageAnchorId);

    $("#" + imageEditIconId).click(function() {
        var caption = prompt("Enter the caption", existingCaption);
        if (caption != null) {
            $.ajax({
                type: "PUT",
                contentType : 'application/json',
                url: this.getImageRestUrl(extId, image.filename),
                data: JSON.stringify({
                    "caption": caption
                }),
                success: function(result) {
                    existingCaption = caption;
                    $("#" + imageCellId).find(".captionContainer").html(caption);
                    setImgFancyBox('#' + imageAnchorId);
                },
                fail: function(jqxhr, textStatus, error) {
                    alert("Failed to edit image caption. Error: " + textStatus + ", " + error);
                }
            });
        }
    }.bind(this));

    $('#' + imageDeleteIconId).click(function() {
        if (confirm('Are you sure you want to delete the image?')) {
            $.ajax({
                url: this.getImageRestUrl(extId, image.filename),
                type: 'DELETE',
                success: function(result) {
                    $('#rwAvyFormImageGrid').find('#' + imageCellId).remove();
                    $('#rwAvyFormImageGrid').sortable('refresh');
                },
                fail: function(jqxhr, textStatus, error) {
                    alert('Failed to delete ' + image.filename + '. Error: ' + textStatus + ", " + error);
                }
            });
        }
    }.bind(this));
}

function setImageFancyBox(selector) {
    $(selector).fancybox({
        padding: 0,
        openEffect: "elastic",
        closeEffect: "elastic",
        beforeShow: function() {
            var captionHtml = $(this.element).next(".captionContainer").html();
            if (captionHtml.length) {
                this.title = "<div style='max-width:" + $(this.element).data("width")
                    + ";white-space: pre-wrap;text-align:left;'>" + captionHtml + "</div>";
            }
        }
    });
}

var s3Client;
AvyForm.prototype.getSignedImageUrl = function(extId, filename) {
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

AvyForm.prototype.getImageRestUrl = function(extId, filename) {
  var editKeyParam = this.view.getRequestParam("edit");
  var imageUrl = '/rest/images/' + extId;

  if (filename) imageUrl += '/' + getFileBaseName(filename);
  if (editKeyParam) imageUrl += "?edit=" + editKeyParam;

  return imageUrl;
}

function getFileBaseName(filename) {
    return filename.substring(0, filename.lastIndexOf('.'));
}

AvyForm.prototype.wireReadWriteFormAdminControls = function(view) {
    $("#rwAvyFormDeleteButton").show();
    $("#rwAvyFormAdminTd").show();
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
	$('#rwAvyFormDiv').find('input:text, input:hidden, textarea').val('');
	$('#rwAvyFormDiv').find('.avyRDSliderValue').val('0');
	$('#rwAvyFormDiv').find('.avyRDSlider').slider('value', 0);
	$('#rwAvyFormImageGrid').empty();
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

AvyForm.prototype.closeReportForm = function() {
    $('#rwAvyFormOverlay').css('visibility', 'hidden');
}

AvyForm.prototype.toggleWindDirectionFields = function(value) {
    if (value === 'empty' || value.length == 0) {
        $('#rwAvyFormRecentWindDirection').val('');
        $('#rwAvyFormRecentWindDirection').siblings('.avyDirectionAutoComplete').val('');
        $('#rwAvyFormRecentWindDirectionText').css('color', 'gray');
        $('#rwAvyFormWeather .avyDirectionAutoComplete').prop('disabled', true);
    } else {
        $('#rwAvyFormRecentWindDirectionText').css('color', 'white');
        $('#rwAvyFormWeather .avyDirectionAutoComplete').prop('disabled', false);
    }
}

AvyForm.prototype.toggleTriggerCauseFields = function(category) {
    if (!this.fullTriggerModifierSource) {
        Object.defineProperty(this, "fullTriggerModifierSource", {
            value: $('.avyTriggerModifierAutoComplete').avycomplete('option', 'source')
        });
    }

    var enableTriggerModifierFields = function() {
        $('.avyTriggerModifierAutoComplete').prop('disabled', false);
        $('label[for="rwAvyFormTriggerModifier"]').css('color', 'white');
    }

    if (category && (category.startsWith('Natural') || category.endsWith('Explosive'))) {
        $('.avyTriggerModifierAutoComplete').avycomplete('option', 'source', this.fullTriggerModifierSource.slice(0,2));
        enableTriggerModifierFields();
    } else if (category && category.endsWith('Human')) {
        $('.avyTriggerModifierAutoComplete').avycomplete('option', 'source', this.fullTriggerModifierSource);
        enableTriggerModifierFields();
    } else {
        $('#rwAvyFormTriggerModifier').val('');
        $('.avyTriggerModifierAutoComplete').val('');
        $('.avyTriggerModifierAutoComplete').prop('disabled', true);
        $('label[for="rwAvyFormTriggerModifier"]').css('color', 'gray');
    }
}

function setReadOnlyAutoCompleteVal(inputElem, obj) {
    if (obj.value == 'empty') {
        $(inputElem).css('color', 'gray');
        $(inputElem).text('unspecified');
    } else {
        $(inputElem).css('color', 'white');
        $(inputElem).text(obj.label);
    }
}

function setReadOnlySliderVal(inputElem, value) {
    if (value <= 0) {
        $(inputElem).css('color', 'gray');
        $(inputElem).text('unspecified');
    } else {
        $(inputElem).css('color', 'white');
        $(inputElem).text(value);
    }
}

function setReadOnlySpinnerVal(inputElem, value, unit) {
    if (value == -1) {
        $(inputElem).css('color', 'gray');
        $(inputElem).text('unspecified');
    } else {
        $(inputElem).css('color', 'white');
        if (unit) $(inputElem).text(value + " " + unit);
        else $(inputElem).text(value);
    }
}

AvyForm.prototype.setReadWriteAutocompleteVal = function(hiddenSibling, obj) {
  if (obj.value == 'empty') {
      $(hiddenSibling).val('');
      $(hiddenSibling).siblings('.avyAutoComplete').val('');
  } else {
      $(hiddenSibling).val(obj.value);
      $(hiddenSibling).siblings('.avyAutoComplete').val(obj.label);
  }
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