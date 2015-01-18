'use strict';

require.config({
  baseUrl: '/js'
});
    
//Start the main app logic.
requirejs(['lib/jquery.dataTables'], function() {
  if (adminLoggedIn()) {
    var tableOptions = {
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