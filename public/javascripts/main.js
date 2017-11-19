"use strict";

var avyEyesView;

require.config({
  baseUrl: "/assets/javascripts",
  paths: {
    jquery: "lib/jquery",
    jqueryui: "lib/jquery-ui",
    fileupload: "lib/jquery.fileupload",
    fancybox: "lib/jquery.fancybox",
    notify: "lib/jquery.notify",
    facebook: "//connect.facebook.net/en_US/all",
    twitter: "//platform.twitter.com/widgets"
  }
});

requirejs(["jquery", "avyeyes-view", "facebook", "twitter"], function($, AvyEyesView) {
    $(document).ready(function() { if (!avyEyesView) avyEyesView = new AvyEyesView(true); });
}, function(err) {
    var failedModule = err.requireModules[0];
    if ((failedModule === "facebook" || failedModule === "twitter") && !avyEyesView) {
        requirejs.undef("facebook");
        requirejs.undef("twitter");
        requirejs(["jquery", "avyeyes-view"], function($, AvyEyesView) {
            $(document).ready(function() { if (!avyEyesView) avyEyesView = new AvyEyesView(false); });
        });
    }
});
