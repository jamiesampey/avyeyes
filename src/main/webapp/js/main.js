"use strict";

require.config({
  baseUrl: "/js"
});
    
var avyEyesView;

//Start the main app logic.
requirejs(["avyeyes.view",
           "//connect.facebook.net/en_US/all.js",
           "//platform.twitter.com/widgets.js"], function(AvyEyesView) {

        FB.init({
            appId: "541063359326610",
            xfbml: true,
            version: "v2.3"
        });

        avyEyesView = new AvyEyesView();
    }
);
