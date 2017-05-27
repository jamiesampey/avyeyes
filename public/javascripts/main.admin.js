'use strict';

require.config({
  baseUrl: "/assets/javascripts",
  paths: {
      'jquery': './lib/jquery',
      'datatables': './lib/jquery.datatables'
  }
});

requirejs(['jquery', 'datatables'], function($) { wireDataTable(); });

function wireDataTable() {
  var dataTableObj = $('#avyAdminDataTable');
  var dataTableAjaxFilterTimeout;
  var calcDataTableHeight = function() {
    return $(window).height() - 190;
  };

  var dataTableApi = dataTableObj.DataTable({
    "columnDefs": [
       { "name": "CreateTime", "targets": 0 },
       { "name": "UpdateTime", "targets": 1 },
       { "name": "ExtId", "targets": 2 },
       { "name": "Viewable", "targets": 3 },
       { "name": "AreaName", "targets": 4 },
       { "name": "SubmitterEmail", "targets": 5 }
     ],
     "pageLength": 25,
     "scrollY": calcDataTableHeight(),
     "scrollCollapse": true,
     "serverSide": true,
     "ajax": {
       "dataType": 'json',
       "type": 'GET',
       "url": '/avalanche/table'
      }
    }
  );

  $('.dataTables_filter input').unbind().bind('input', function(e) {
    clearTimeout(dataTableAjaxFilterTimeout);
    if(this.value.length >= 3 || e.keyCode == 13) {
        dataTableAjaxFilterTimeout = setTimeout(dataTableApi.search(this.value).draw, 500);
    }

    if(this.value == '') {
        dataTableApi.search('').draw();
    }
    return;
  });

  $(window).resize(function() {
    dataTableObj.dataTable().fnSettings().oScroll.sY = calcDataTableHeight();
    dataTableObj.dataTable().fnDraw();
  });

  $("#adminLogout").click(function() {
      var docCookie = "; " + document.cookie;
      var parts = docCookie.split("; csrfToken=");
      var csrfTokenFromCookie = parts.length == 2 ? parts.pop().split(";").shift() : "";

      $.ajax({
          url: "/logout?csrfToken=" + csrfTokenFromCookie,
          type: 'GET',
          success: function(result) {
              window.location.replace("/admin");
          },
          fail: function(jqxhr, textStatus, error) {
              console.error("AvyEyes logout failure. Error: " + error);
          }
      });
  });
}