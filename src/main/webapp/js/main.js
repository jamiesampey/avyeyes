'use strict';

require.config({
  baseUrl: '/js'
});
    
var avyEyesView;

//Start the main app logic.
requirejs(['avyeyes.ui',
           'avyeyes.view',
           'lib/facebook',
           '//platform.twitter.com/widgets.js',
           'lib/analytics'],
    function (AvyEyesUI, AvyEyesView) {
        avyEyesView = new AvyEyesView();
        AvyEyesUI.wire(avyEyesView);
        $('#avyInitLiftCallback').submit();
    }
);
