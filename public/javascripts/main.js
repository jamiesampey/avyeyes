"use strict";

var avyEyesView;

requirejs.config({
    paths: {
        'jquery': ['../lib/jquery/jquery'],
        'jquery-ui': ['../lib/jquery-ui/jquery-ui'],
        'file-upload': ['../lib/jquery-file-upload/js/jquery.fileupload'],
        'fancybox': ['../lib/jquery.fancybox.pack/jquery.fancybox.pack']
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