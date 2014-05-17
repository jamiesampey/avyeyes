/** @license
 * RequireJS plugin for async dependency load like JSONP and Google Maps
 * Author: Miller Medeiros
 * Version: 0.1.1 (2011/11/17)
 * Released under the MIT license
 */
define(function(){

    var DEFAULT_CALLBACK_PARAM_NAME = 'callback',
        _uid = 0;

    function injectScript(src){
        var s, t;
        s = document.createElement('script'); s.type = 'text/javascript'; s.async = true; s.src = src;
        t = document.getElementsByTagName('script')[0]; t.parentNode.insertBefore(s,t);
    }

    function formatUrl(name, callbackFunc){
        var callbackParamRegex = /!(.+)/,
            url = name.replace(callbackParamRegex, ''),
            callbackParam = (callbackParamRegex.test(name))? name.replace(/.+!/, '') : DEFAULT_CALLBACK_PARAM_NAME;
        url = addParam(url, callbackParam, callbackFunc);
        if (url.indexOf('google') > -1) {
        	url = addParam(url, "key", GOOGLE_API_KEY);
        }
        return url;
    }
    
    function addParam(url, name, val) {
    	url += (url.indexOf('?') < 0)? '?' : '&';
        return url + name +'='+ val;
    }

    function uid() {
        _uid += 1;
        return '__async_req_'+ _uid +'__';
    }

    return{
        load : function(name, req, onLoad, config){
            if(config.isBuild){
                onLoad(null); //avoid errors on the optimizer
            }else{
                var id = uid();
                //create a global variable that stores onLoad so callback
                //function can define new module after async load
                window[id] = onLoad;
                injectScript(formatUrl(name, id));
            }
        }
    };
});
