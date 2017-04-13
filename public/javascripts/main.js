"use strict";

var avyEyesView;

require.config({
  baseUrl: "/assets/javascripts",
  paths: {
    'jquery': './lib/jquery',
    'jquery-ui': './lib/jquery-ui',
    'file-upload': './lib/jquery.fileupload',
    'fancybox': './lib/jquery.fancybox'
  }
});

requirejs(["jquery",
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