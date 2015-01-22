'use strict';

require.config({
  baseUrl: '/js'
});
    
//Start the main app logic.
requirejs(['lib/jquery.dataTables'], function() {
  if (adminLoggedIn()) {
    var tableOptions = {
      "columnDefs": [
        { "name": "createTime", "targets": 0 },
        { "name": "updateTime", "targets": 1 },
        { "name": "extId", "targets": 2 },
        { "name": "viewable", "targets": 3 },
        { "name": "areaName", "targets": 4 },
        { "name": "submitterEmail", "targets": 5 }
      ],
      "pageLength": 25,
      "scrollY": "675px",
      "scrollCollapse": true,
      "serverSide": true,
      "ajax": {
        "dataType": 'json',
        "type": 'GET',
        "url": '/rest/admintable'
      }
    };

    var adminTable = $('#avyAdminDataTable').dataTable(tableOptions);
  }
});

function adminLoggedIn() {
  var adminEmailSpan = $('#avyAdminLoggedInEmail');
  return adminEmailSpan.length > 0 && adminEmailSpan.text().length > 0;
}