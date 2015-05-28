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
requirejs(['avyeyes', 'avyeyes.view', 'lib/gmaps-loader',
    'lib/analytics', 'lib/facebook', '//platform.twitter.com/widgets.js'],
    function (AvyEyes, AvyEyesView, gmapsAsyncLoad) {
        gmapsAsyncLoad('gmapsLoadCB');
        gmapsLoadedGate.done(function() {
            avyEyesView = new AvyEyesView(google.maps);
            AvyEyes.wire(avyEyesView);
            $('#avyInitLiftCallback').submit();
        });
    }
);
