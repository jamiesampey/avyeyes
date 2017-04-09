"use strict";

var avyEyesView;

requirejs(["lib/jquery",
           "avyeyes.view",
           "//connect.facebook.net/en_US/all.js",
           "//platform.twitter.com/widgets.js"], function($, AvyEyesView) {
    console.log("AvyEyes Load 0: Waiting for DOM load to begin AvyEyes");
    $(document).ready(function() {
        if (!avyEyesView) {
            console.log("AvyEyes Load 1: Instantiating AvyEyes view");
            avyEyesView = new AvyEyesView();
        } else {
            console.error("AvyEyes view already instantiated");
        }
    });
});