$(document).ready(function(){
//	  var wrap=$(document).height();
//	  var resize=wrap-$(".searchboxInput").height()-$("#footer").height()-$(".navbar").height()
//	  				-$("#footer").css("padding-top").substring(0, $("#footer").css("padding-top").length-2)-3;
		resizeChattingLayout(1);		
});

function resizeChattingLayout(flag){
	try{
		var wrap = $(window).height();	
		var limit = $('#chatting-pane').height() + $('#chatting-pane').offset().top; 
		var resize = 0;
		if(flag == 1){					
			resize = wrap - $(".navbar").height() - $(".searchboxInput").height() + $(window).scrollTop();					
		} else {			
			resize = wrap - $(".searchboxInput").height();
		}
		$('.flyoutBody').css("height",resize-3);
	}catch(e){}
}

function setFixChattingLayout(){
	var winTop = $(window).scrollTop();
	var chattingLayoutTop = $(".navbar").height();
	if(winTop >= chattingLayoutTop){
		$('#chatting-pane').css({
			"position" : "fixed",
			"top" : "0"});
		resizeChattingLayout(2);
	} else {
		$('#chatting-pane').css({
			"position" : "absolute",
			"top" : $(".navbar").height() + 'px'});
		resizeChattingLayout(1);
	}
	
}

function toggleChattingForm(){
    var moveConverstationToRight = function () {$("#conversation-wrapper").stop().animate({right : "0"});};
    var moveConverstationToLeft = function () {$("#conversation-wrapper").stop().animate({right : "260"});};
    var toggleChatList = function () {$('#chatting-pane').toggle();};

    if($("#chatting-pane").is(":visible")) {
        toggleChatList();
        moveConverstationToRight();
    } else {
        moveConverstationToLeft();
        toggleChatList();
    }
	return false;
}

$(function () {
    $('#searchFriend').keyup(function (event) {
    	searchFriends();
    });
});

function searchFriends() {
    var members = $('.chat-friendlist');
    var filterValue = $.trim($('#searchFriend').val());
    if (filterValue != ''){
    	members.find(".chat-item .name:icontains('" + filterValue + "')")
    		.each(function () {
    			$(this).parent().parent().parent().show();
    		});
    	members.find(".chat-item .name:not(:icontains('" + filterValue + "'))")
    		.each(function () {
    			$(this).parent().parent().parent().hide();
    		});
    }else{
    	members.find(".chat-item .name:not(:icontains('" + filterValue + "'))")
		.each(function () {
			$(this).parent().parent().parent().show();
		});
    }
}

function encodeVietnamese2English(str){
	 str=str.toLowerCase();
	 str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, "a");
     str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, "e");
     str = str.replace(/ì|í|ị|ỉ|ĩ/g, "i");
     str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, "o");
     str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, "u");
     str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, "y");
     str = str.replace(/đ/g, "d");
     return str;
}
$.expr[":"].icontains = $.expr.createPseudo(function (arg) {
    return function (elem) {
        return encodeVietnamese2English($(elem).text()).indexOf(encodeVietnamese2English(arg)) >= 0;
    };
});

function receiveMemberStatus(data) {
    var jsonMemberStatus = $.parseJSON(data);
    var memberId = jsonMemberStatus.memberId;
    var status = jsonMemberStatus.status;
    if (status == "online") {
        $(".chatStatusIcon i#member-" + memberId).css({"color": "#7fba00"});
    } else {
        $(".chatStatusIcon i#member-" + memberId).css({"color": "#a8a8a8"});
    }
}

/*-------------------- Conversation --------------------*/
function fitTextAreaToContent(textarea) {
    var lineHeight = parseFloat($(textarea).css("line-height").substring(0, $(textarea).css("line-height").length - 2));
    if ($(textarea).outerHeight() < 62) {
        $(textarea).css("overflow", "hidden");
    } else {
        $(textarea).css("overflow", "auto");
    }
    while ($(textarea).outerHeight() < textarea.scrollHeight
        + parseFloat($(textarea).css("borderTopWidth"))
        + parseFloat($(textarea).css("borderBottomWidth")) && $(textarea).outerHeight() < 62) {
        $(textarea).height($(textarea).height() + lineHeight);
        $(textarea).parent().find(".conversationContent").height($(textarea).parent().find(".conversationContent").height()  - lineHeight);
    }
}

function sendConversationMessage(conversation) {
    var $input = $(conversation).find(".inputMessage");
    var msg = new Object();
    
    if ($input.val().length > 1024) {
    	msg.content = "Length must less than 1024";
    	msg.groupDate = '';
    	addMessageToConversation(conversation.attr("id"), msg, 'new');
    }
    else{
    	var timestamp = new Date().getTime();
    	var sendFn = "sendMessageTo" + conversation.attr("id") + "([{name: \"content\", value: \"" + encodeURIComponent($input.val()) + "\"}, " +
    			"{name: \"timestamp\", value: \"" + timestamp + "\"}]);";
    	eval(sendFn);
    	msg.content = encodeURIComponent($input.val());
    	msg.sender = $("#loggedMemberId").val();
    	msg.groupDate = '';
    	msg.isRead = true;
    	addMessageToConversation(conversation.attr("id"), msg, 'new');
    	
	}
    $input.val("");
}

function receiveMessage(data) {
	var jsonMessage = $.parseJSON(data);
    	    var conversationId = jsonMessage.sender == $("#loggedMemberId").val() ? jsonMessage.recipient : jsonMessage.sender;
    	    var $conversation = $("#" + conversationId + ".conversation");
    	    var $content = $conversation.find(".system-message");
    	    if(jsonMessage.msgType == "chat") {
    		    addMessageToConversation(conversationId, jsonMessage, 'new');
    		    if (!$conversation.is(":visible") && !jsonMessage.isRead && jsonMessage.sender == conversationId) {
    		        $conversation.show();
    		        maximize($conversation);
    		    }
    		    if ($conversation.hasClass("minimized")) {
    		        $conversation.find(".titlebar").addClass("unread");
    		    }

    	    } else if(jsonMessage.msgType == "typing") {
    	        addSystemMessageToConversation(conversationId, jsonMessage);
    	    } else if(jsonMessage.msgType == "old-chat"){
    	    	addMessageToConversation(conversationId, jsonMessage, 'old');
    	    }
    	    else if(jsonMessage.msgType == 'list-chat'){
    	    	 var listJsonMessage = jsonMessage.content;
    	    	 for(var i = 0; i < listJsonMessage.length; i++){
    	    	    addMessageToConversation(conversationId, listJsonMessage[i], 'old');
    	    	 }
    	    	 if($content.hasClass('loading-message')){
     		    	hideLoadingMessage(conversationId);
     		     }
    	    }
}

function receiveOldMessage(data) {
	
	if (data == 'end') {
		$('#chatVar').attr('data-load-old-msg', 'false');
	}
	if ($('#chatVar').attr('data-load-old-msg') == 'true') {
		receiveMessage(data);
	}
}


function addMessageToConversation(conversationId, jsonMessage, flagMessage) {
    var messageClass, messageWrapperClass;
    
    if (jsonMessage.sender) {
        messageClass = jsonMessage.sender == $("#loggedMemberId").val() ? "my-message" : "friend-message";
        messageWrapperClass = jsonMessage.sender == $("#loggedMemberId").val() ? "wrapper-my-message" : "wrapper-friend-message";
    } else {
        messageClass = "system-message";
        messageWrapperClass = "wrapper-system-message";
    }
    var $conversation = $("#" + conversationId + ".conversation");
	var $content = $("#" + conversationId + ".conversation").find(".conversationContent");
    var content = $content.get(0);
    var message = document.createElement("div");
    var groupDate = $("<div class='group-Date'><span>" + jsonMessage.groupDate + "</span></div>");
    var unRead = $("<span class='unreadIcon img-circle'></span>");
    if(jsonMessage.sendDate) {
        $(message).attr("title", jsonMessage.sendDate);
    }
    $(message).addClass(messageClass);
    $(message).text(decodeURIComponent(jsonMessage.content));
    
    var messageWrapper = $("<div class='messageWrapper " + messageWrapperClass + "'></div>");
    
    if (messageClass == "friend-message") {
        var $image = $conversation.find(".friend-image-original").clone();
        $image.removeClass("friend-image-original");
        $image.addClass("friend-image");
        $image.appendTo(messageWrapper);
        $image.show();
    }
    
    if(!jsonMessage.isRead){
	    if(messageWrapperClass == 'wrapper-friend-message'){
	      	$(message).appendTo(messageWrapper);
	    	$(unRead).appendTo(messageWrapper);
	    }
    	else{
	    	$(message).appendTo(messageWrapper);
    	}
    }else{
    	$(message).appendTo(messageWrapper);
    }
    
    
    if(jsonMessage.groupDate != ''){
    	$(groupDate).appendTo($content);
    }

    if(flagMessage = 'new'){
    	messageWrapper.appendTo($content);
    } else if(flagMessage = 'old'){
      	messageWrapper.prependTo($content);
    }
    
    $content.scrollTop(content.scrollHeight); 
    return messageWrapper;
}

function addSystemMessageToConversation(conversationId, jsonMessage) {
    if (jsonMessage.msgType == "typing") {
        if (jsonMessage.content) {
            showTypingMessage(conversationId);
            clearTimeout(removeTypingMessage[conversationId]);
            removeTypingMessage[conversationId] = setTimeout(function() {
                hideTypingMessage(conversationId);
            }, 2000);
        } else {
            hideTypingMessage(conversationId);
        }
    }
}

function showLoadingMessage(conversationId) {
    var $conversation = $("#" + conversationId + ".conversation");
    var $content = $conversation.find(".conversationContent");
    var $typingMessage = $('<div class="system-message loading-message">Loading message...</div>');
    $typingMessage.appendTo($content);
}

function hideLoadingMessage(conversationId) {
	var $conversation = $("#" + conversationId + ".conversation");
    $conversation.find('.loading-message').remove();
}

function showTypingMessage(conversationId) {
    var $conversation = $("#" + conversationId + ".conversation");
    var $content = $conversation.find(".conversationContent");
    var content = $content.get(0);
    var friendName = $conversation.find('.titleText').text();
    var $typingMessage = $conversation.find('.typing-status');
    if ($typingMessage.length == 0) {
        $typingMessage = $('<div class="system-message typing-status hide"></div>');
        $typingMessage.text(friendName + ' is typing...');
    }
    $typingMessage.appendTo($content);
    $typingMessage.fadeIn(400);
    $content.scrollTop(content.scrollHeight);
}

function hideTypingMessage(conversationId) {
    var $conversation = $("#" + conversationId + ".conversation");
    $conversation.find('.typing-status').remove();
}

function maximize(conversation) {
    var $content = conversation.find(".conversationContent");
    var content = $content.get(0);
    conversation.stop().animate({
        "bottom": "0",
        "width": "260px"
    });
    conversation.removeClass("minimized");
    conversation.find(".titlebar").removeClass("unread");
    $content.scrollTop(content.scrollHeight);
    var browserInfo = new UAParser().getResult();
    if(browserInfo.os.name != "iOS") {
	    setTimeout(function () {
	        conversation.children(".inputMessage").focus();
	    }, 500);
    }
}

function minimize(conversation) {
    conversation.stop().animate({
        "bottom": "-259px",
        "width": "100px"
    });
    conversation.addClass("minimized");
}

/* BEGIN - Handle typing status ********************/
var typingTimeout;
var removeTypingMessage = new Array();
function sendStartTypingStatus(conversationId) {
    var date = new Date();
    var currentTime = date.getTime();
    if (!typingTimeout || (currentTime - typingTimeout) > 1000) {
        typingTimeout = currentTime;
        var msgType = "typing";
        var sender = $("#loggedMemberId").val();
        var recipient = conversationId;
        var content = true;
        sendSystemMessage([
            {name: "msgType", value: msgType},
            {name: "sender", value: sender},
            {name: "recipient", value: recipient},
            {name: "content", value: content}
        ]);
    }
}
function sendStopTypingStatus(conversationId) {
    var msgType = "typing";
    var sender = $("#loggedMemberId").val();
    var recipient = conversationId;
    var content = false;
    sendSystemMessage([
        {name: "msgType", value: msgType},
        {name: "sender", value: sender},
        {name: "recipient", value: recipient},
        {name: "content", value: content}
    ]);
}
/* END - Handle typing status ********************/
