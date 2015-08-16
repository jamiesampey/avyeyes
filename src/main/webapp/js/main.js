'use strict';

require.config({
  baseUrl: '/js'
});
    
var avyEyesView;

//Start the main app logic.
requirejs(['avyeyes.view',
           'lib/facebook',
           '//platform.twitter.com/widgets.js',
           'lib/analytics'],
    function (AvyEyesView) {
        avyEyesView = new AvyEyesView();
    }
);
