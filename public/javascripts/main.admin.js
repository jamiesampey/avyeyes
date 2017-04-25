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
}