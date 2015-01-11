'use strict';

require.config({
  baseUrl: '/js',
  paths: {
    'jquery-datatables': 'lib/jquery.dataTables'
  }
});
    
//Start the main app logic.
requirejs(['jquery-datatables'], function () {
  $('.avyAdminDataTable').dataTable();
});
