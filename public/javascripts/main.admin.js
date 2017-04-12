'use strict';

requirejs(['jquery', 'datatables'], function($) {
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