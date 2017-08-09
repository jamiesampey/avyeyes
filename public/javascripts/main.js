"use strict";

var avyEyesView;

require.config({
  baseUrl: "/assets/javascripts",
  paths: {
    jquery: "lib/jquery",
    jqueryui: "lib/jquery-ui",
    fileupload: "lib/jquery.fileupload",
    fancybox: "lib/jquery.fancybox",
    notify: "lib/jquery.notify"
  }
});

requirejs(["jquery",
           "avyeyes-view",
           "//connect.facebook.net/en_US/all.js",
           "//platform.twitter.com/widgets.js"], function($, AvyEyesView) {
    console.log("Waiting for DOM to load before starting AvyEyes");
    $(document).ready(function() {
        if (!avyEyesView) {
            console.log("Instantiating AvyEyes view");
            avyEyesView = new AvyEyesView();
        } else {
            console.error("AvyEyes view already instantiated");
        }
    });
});