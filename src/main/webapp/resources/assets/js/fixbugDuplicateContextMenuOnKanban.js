PrimeFaces.createWidget = function(widgetConstructor, widgetVar, cfg, resource) {            
    if(PrimeFaces.widget[widgetConstructor]) {
        if(window[widgetVar])
            window[widgetVar].refresh(cfg);                                     //ajax spdate
        else {
        	var jqId = PrimeFaces.escapeClientId(cfg.id),
            instances = $(jqId);
            if(instances.length > 1) {
                $(document.body).children(jqId).remove();
            }
            window[widgetVar] = new PrimeFaces.widget[widgetConstructor](cfg);  //page init
        }
    }
    else {
        var scriptURI = $('script[src*="/javax.faces.resource/primefaces.js"]').attr('src').replace('primefaces.js', resource + '/' + resource + '.js'),
        cssURI = $('link[href*="/javax.faces.resource/primefaces.css"]').attr('href').replace('primefaces.css', resource + '/' + resource + '.css'),
        cssResource = '<link type="text/css" rel="stylesheet" href="' + cssURI + '" />';

        //load css
        $('head').append(cssResource);

        //load script and initialize widget
        PrimeFaces.getScript(location.protocol + '//' + location.host + scriptURI, function() {
            setTimeout(function() {
                window[widgetVar] = new PrimeFaces.widget[widgetConstructor](cfg);
            }, 100);
        });
    }
}