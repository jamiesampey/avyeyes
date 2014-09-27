define(['jquery-ui'], function() {

function AvyAdmin(avyEyesView) {
  this.view = avyEyesView;
  this.addReportAdminControls(avyEyesView);
}

AvyAdmin.prototype.displayDetails = function(a) {
  $('#avyReportExtId').val(a.extId);

  $('#avyReportViewableTd').children(':checkbox').attr('checked', a.viewable);
  $('#avyReportViewableTd').children(':checkbox').trigger('change');
    
  $('#avyReportSubmitterEmail').val(a.submitterEmail);
  this.setAutocomplete('#avyReportSubmitterExp', a.submitterExp);
    
  $('#avyReportAreaName').val(a.areaName);
  $('#avyReportDate').val(a.avyDate);
  this.setAutocomplete('#avyReportSky', a.sky);
  this.setAutocomplete('#avyReportPrecip', a.precip);
    
  this.setAutocomplete('#avyReportType', a.avyType);
  this.setAutocomplete('#avyReportTrigger', a.trigger);
  this.setAutocomplete('#avyReportBedSurface', a.bedSurface);
  this.setSlider('#avyReportRsizeValue', a.rSize);
  this.setSlider('#avyReportDsizeValue', a.dSize);
  
  $('#avyReportElevation').val(a.elevation);
  $('#avyReportElevationFt').val(this.view.metersToFeet(a.elevation));
  this.setAutocomplete('#avyReportAspect', a.aspect);
  $('#avyReportAngle').val(a.angle);
  
  this.setSpinner('#avyReportNumCaught', a.caught);
  this.setSpinner('#avyReportNumPartiallyBuried', a.partiallyBuried);
  this.setSpinner('#avyReportNumFullyBuried', a.fullyBuried);
  this.setSpinner('#avyReportNumInjured', a.injured);
  this.setSpinner('#avyReportNumKilled', a.killed);
  this.setAutocomplete('#avyReportModeOfTravel', a.modeOfTravel);
  
  $('#avyReportComments').val(a.comments);
  
  $('#avyReportDeleteBinding').val(a.extId);
  $('#avyReportDialog').dialog('open');
}

AvyAdmin.prototype.setAutocomplete = function(hiddenSibling, enumObj) {
  $(hiddenSibling).val(enumObj.value);
  $(hiddenSibling).siblings('.avyAutoComplete').val(enumObj.label);
}

AvyAdmin.prototype.setSlider = function(inputElem, value) {
  $(inputElem).val(value);
  $(inputElem).siblings('.avyRDSlider').slider('value', value);
}

AvyAdmin.prototype.setSpinner = function(inputElem, value) {
  if (value == -1) {
    $(inputElem).val('');
  } else {
    $(inputElem).val(value);
  }
}

AvyAdmin.prototype.addReportAdminControls = function(view) {
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
        view.resetView();
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

return AvyAdmin;
});