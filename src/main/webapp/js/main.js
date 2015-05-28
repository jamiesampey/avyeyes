'use strict';

require.config({
  baseUrl: '/js'
});
    
var avyEyesView;
var gmapsLoadedGate = $.Deferred();

function gmapsLoadCB() {
    gmapsLoadedGate.resolve();
}

//Start the main app logic.
requirejs(['avyeyes.ui', 'avyeyes.view', 'lib/gmaps-loader',
    'lib/facebook', '//platform.twitter.com/widgets.js', 'lib/analytics'],
    function (AvyEyesUI, AvyEyesView, gmapsAsyncLoad) {
        gmapsAsyncLoad('gmapsLoadCB');
        gmapsLoadedGate.done(function() {
            avyEyesView = new AvyEyesView(google.maps);
            AvyEyesUI.wire(avyEyesView);
            $('#avyInitLiftCallback').submit();
        });
    }
);
