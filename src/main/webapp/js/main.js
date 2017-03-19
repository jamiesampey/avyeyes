"use strict";

require.config({
  baseUrl: "/js"
});
    
var avyEyesView;

requirejs(["avyeyes.view",
           "//connect.facebook.net/en_US/all.js",
           "//platform.twitter.com/widgets.js"], function(AvyEyesView) {
    console.log("AvyEyes Load 0: Waiting for window onload to begin AvyEyes");
    window.onload = function() {
        if (!avyEyesView) {
            console.log("AvyEyes Load 1: Instantiating AvyEyes view");
            avyEyesView = new AvyEyesView();
        } else {
            console.error("AvyEyes view already instantiated");
        }
    };
});
