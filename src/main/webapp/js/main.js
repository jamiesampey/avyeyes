"use strict";

require.config({
  baseUrl: "/js"
});
    
var avyEyesView;

requirejs(["avyeyes.view",
           "//connect.facebook.net/en_US/all.js",
           "//platform.twitter.com/widgets.js"], function(AvyEyesView) {
    avyEyesView = new AvyEyesView();
});
