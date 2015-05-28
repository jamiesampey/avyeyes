'use strict';

require.config({
  baseUrl: '/js'
});
    
//Start the main app logic.
requirejs(['lib/jquery.dataTables'], function() {
  if (adminLoggedIn()) {
    wireDataTable();
  }
});

function adminLoggedIn() {
  var adminEmailSpan = $('#avyAdminLoggedInEmail');
  return adminEmailSpan.length > 0 && adminEmailSpan.text().length > 0;
}

function wireDataTable() {
  var dataTableObj = $('#avyAdminDataTable');
  var dataTableAjaxFilterTimeout;
  var calcDataTableHeight = function() {
    return $(window).height() - 190;
  };

  var dataTableApi = dataTableObj.DataTable({
    "columnDefs": [
       { "name": "createTime", "targets": 0 },
       { "name": "updateTime", "targets": 1 },
       { "name": "extId", "targets": 2 },
       { "name": "viewable", "targets": 3 },
       { "name": "areaName", "targets": 4 },
       { "name": "submitterEmail", "targets": 5 }
     ],
     "pageLength": 25,
     "scrollY": calcDataTableHeight(),
     "scrollCollapse": true,
     "serverSide": true,
     "ajax": {
       "dataType": 'json',
       "type": 'GET',
       "url": '/rest/admintable'
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
}