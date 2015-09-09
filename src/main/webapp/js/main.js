"use strict";

require.config({
  baseUrl: "/js"
});
    
var avyEyesView;

//Start the main app logic.
requirejs(["avyeyes.view",
           "//www.google-analytics.com/analytics.js",
           "//connect.facebook.net/en_US/all.js",
           "//platform.twitter.com/widgets.js"], function(AvyEyesView) {

        ga("create", "UA-45548947-3");
        ga("send", "pageview");

        FB.init({
            appId: "541063359326610",
            xfbml: true,
            version: "v2.3"
        });

        avyEyesView = new AvyEyesView();
    }
);
