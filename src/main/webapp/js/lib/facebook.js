define(['http://connect.facebook.net/en_US/all.js'], function () {
    FB.init({
        appId: FACEBOOK_APP_ID,
        xfbml: true,
        version: 'v2.1'
    });
});