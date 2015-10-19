// <![CDATA[        
        function handleMessage(facesmessage) {
        	try{
        		var json = JSON.parse(facesmessage);
    			var skype=json.account;
    			$(document.getElementById('Skype_'+skype)).removeClass();
    			$(document.getElementById('Skype_'+skype)).addClass(json.status+" "+"icon-skype");
             	if(json.notice=='true'){
             		if(document.getElementById('Skype_'+skype)!=null){
             			notification.renderMessage({summary: "Skype", detail: json.detail});  
             		}
             	}             
        	}catch(e){
        		notification.renderMessage({summary: "My Agile", detail: facesmessage});        	
            }        	
		}        
        $( document ).ready(function() {
        	setTimeout(function(){
        		try{
        		notification.renderMessage=function (messageJSON){
            		var growlDiv='<div class="notification ui-growl-item-container ui-state-highlight ui-corner-all ui-helper-hidden ui-shadow" aria-live="polite">';
            		growlDiv+='<div class="ui-growl-item">';
            		growlDiv+='<span class="ui-growl-image ui-growl-image-i"/>';
            		growlDiv+='<div class="ui-growl-message">';
            		growlDiv+='<span class="ui-growl-title"/>';
            		growlDiv+="<p></p>";
            		growlDiv+='</div><div style="clear: both;"></div></div></div>';
            		var growl=$(growlDiv),titleDiv=growl.find("span.ui-growl-title"),detailDiv=titleDiv.next();        
            		titleDiv.html(messageJSON.summary);
            		detailDiv.html(messageJSON.detail);        		
            		this.bindEvents(growl);
            		growl.prependTo(this.jq).fadeIn();
        		};
        		}catch(e){}
            	        		
        	}, 200);         	
        });
        $(document).ready(function () {
	        setOnline();
	        setInterval(function () {
	            setOnline();
	        }, 28000);
	        window.onunload = function () {
	            setOffline();
	        };
	        function setOnline() {
	            if ($("#chattingIcon").is(":visible")) {
	                online();
	            }
	        }
	        function setOffline() {
	            offline;
	        }
	    });
// ]]>