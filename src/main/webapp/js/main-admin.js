'use strict';

require.config({
  baseUrl: '/js'
});
    
//Start the main app logic.
requirejs(['lib/jquery.dataTables'], function() {
  if (adminLoggedIn()) {
    var tableOptions = {
      "columnDefs": [
        { "name": "CreateTime", "targets": 0 },
        { "name": "UpdateTime", "targets": 1 },
        { "name": "ExternalId", "targets": 2 },
        { "name": "Viewable", "targets": 3 },
        { "name": "AreaName", "targets": 4 },
        { "name": "SubmitterEmail", "targets": 5 }
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