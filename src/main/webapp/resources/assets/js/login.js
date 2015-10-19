var isExisted = true;
function prepareProcess(){
    showLoginInputs();
    hideSignUpInputs();
    $('#forgetPassword').hide();
}
function processLogin(){
	$('#invalidEmail').hide();
	$('#password').show();
	$('#forgetPassword').show();
}
var requirement = {
	
	browser : [ {
		name : "Chrome",
		version : 28
	}, {
		name : "Firefox",
		version : 15
	}, {
		name : "IE",
		version : 10
	}, {
		name : "Opera",
		version : 12
	}, {
		name : "Safari",
		version : 4
	}, {
		name : "Mobile Safari",
		version : 4
	} ],
	
	deviceType : [{
		name : 'Desktop',
		supported: true,
		min : true
	},{
		name : 'Mobile',
		supported: false,
		min : true
	},{
		name : 'Tablet',
		supported: false, 
		min : true
	}]
};

function checkBrowserCompatibility() {
	$(".details").click(function() {
		$("#req-info").toggle();
		return false;
	});
	
	var browserInfo = new UAParser().getResult();
	var partialCompatible = false;
	var notCompatible = false;
	
	for(var i = 0;i < requirement.browser.length; i++) {
		var eachBrowser = requirement.browser[i];
		if(eachBrowser.name == browserInfo.browser.name) {
			if(browserInfo.browser.major < eachBrowser.version) {
				notCompatible = true;
				$("#your-browser").html("<span style='color: red;'>"+browserInfo.browser.name + " " + browserInfo.browser.major+"</span>");
			} else {
				$("#your-browser").html(browserInfo.browser.name + " " + browserInfo.browser.major);
			}
			
			$("#min-browser, #best-browser").html(eachBrowser.name + " " + eachBrowser.version+"+");
		}
	}
	
	// Fill data to info table
	var deviceType = (typeof browserInfo.device.type == 'undefined') ? 'Desktop' : browserInfo.device.type;
	var minDevice = new Array();
	var bestSupport = new Array();
	for(var i = 0;i < requirement.deviceType.length; i++) {
		if(requirement.deviceType[i].min) {
			minDevice.push(requirement.deviceType[i].name);
		}
		
		if(requirement.deviceType[i].supported) {
			bestSupport.push(requirement.deviceType[i].name);
		}
	}
	$("#your-device").html(deviceType);
	
	if(!notCompatible) {
		if(jQuery.inArray(deviceType, bestSupport) == -1) {
			$("#your-device").html("<span style='color: orange;'>"+deviceType+"</span>");
			partialCompatible = true;
		}
	}
	
	$("#min-device").html(minDevice.join());
	$("#best-device").html(bestSupport.join());
	
	if(notCompatible) {
		$("#req-info").addClass("error-info");
		$("#req-error").show();
		$("#tip").html("We detected that your browser's version is below our requirement. Please consider upgrade your browser to latest version or use another cool browser such as Chrome.");
	} else if(partialCompatible) {
		$("#req-info").addClass("warn-info");
		$("#req-warn").show();
		$("#tip").html("We detected that you are using a device is not fully supported in our application. Some features such as Kanban interaction will not work at the moment. We are trying our best to deliver best support for all devices.");
	}
}
/***Functions of login******************************************************************/
function hideErrors(){
	$('.ui-message-error').hide();
}

function handleLogin(xhr, status, args) {
	$('#urlLocal').attr('data-url-local',args.urlLocal);
	isExisted = args.memberExist;
    if (args.memberExist == true) {
        hideSignUpInputs();
        showLoginInputs();        
        var password = $('#password').get(0);
        var length = password.value.length;
        password.selectionStart = length;
        password.selectionEnd = length;
        password.focus();
        handleLdap();
    } else {
        $('#errorNotExists').show();
        var regName = /^[\w-]+(?:\.[\w-]+)*@(?:[\w-]+\.)+[a-zA-Z]{2,7}$/;
        var username = $('#username').val().trim();
        if (regName.test(username)) {
            $('#errorNotExists').hide();	
            hideLoginInputs();
            showSignUpInputs();
            hideLdapButton();
        }
       
    }
}

function handleLock(xhr, status, args) {
    if (args.memberLocked == true) {
    	$("#lockedUser").show();
    	hideSignUpInputs();
    	showLoginInputs();
    }
}

function validateEmail(){
	var regName = /^[\w-]+(?:\.[\w-]+)*@(?:[\w-]+\.)+[a-zA-Z]{2,7}$/;
	var username = $('#username').val().trim();
	if(regName.test(username) == false){
		$('#invalidEmail').show();
	}
}

var func = function() { 
	var containMail= $('#username').val().trim();
		var regName = /^[\w-]+(?:\.[\w-]+)*@(?:[\w-]+\.)+[a-zA-Z]{2,7}$/;
		var username = $('#username').val().trim();
		if(regName.test(username)){
			$('#invalidEmail').hide();
			var htmlPrepareImage = '<img src="resources/img/ajax_loader/spinner.gif" />';
			$("#loadSpinnerImage").fadeIn(100, 0);
			$("#loadSpinnerImage").html(htmlPrepareImage);
			$("#loadSpinnerImage").fadeOut(2500);
			checkMember([ {	name : 'username',value : containMail	} ]);
			checkMemberLocked( [{ name: 'username', value: containMail }] );
		} else {
			
		}
	};

var timeoutReference;
function handleChanged(evt){
	var key = evt.which || evt.charCode || evt.keyCode || 0;
	if(key == 13){
		if (timeoutReference)
		clearTimeout(timeoutReference);
		timeoutReference = setTimeout(func,1000);
	}
	if (timeoutReference)
		clearTimeout(timeoutReference);
		timeoutReference = setTimeout(func,1000);
}

function callSetParams(){
    if (!validatePassword() || !validateConfirmPassword() || !validateFirstName() || !validateLastName()) {
        return false;
    }
	$('#blockUi').show();
	var username = $('#newusername').val();
	var firstName = $('#firstName').val();
    var lastName = $('#lastName').val();
    var password = $('#newPassword').val();
	processParams([{name:'email',value: username},{name:'firstName',value:firstName},{name:'lastName',value:lastName},{name:'password',value:password}]);
}

function validateFirstName(){
	$("#errorEmptyFirstName").hide();
	$("#errorLengthFirstName").hide();
	var firstName = $("#firstName").val().trim();
	
	if(firstName == ""){
		$("#errorEmptyFirstName").show();
		return false;
	}
	else if(firstName.length > 255){
		$("#errorLengthFirstName").show();
		return false;
	}
	return true;
}

function validateLastName(){
	$("#errorEmptyLastName").hide();
	$("#errorLengthLastName").hide();
	var lastName = $("#lastName").val().trim();
	
	if(lastName == ""){
		$("#errorEmptyLastName").show();
		return false;
	}
	else if(lastName.trim().length > 255){
		$("#errorLengthLastName").show();
		return false;
	}
	return true;
}

function validatePassword() {
	hidePasswordValidationMessages();
    if ($.trim($('#newPassword').val()).length == 0) {
        $('#errorEmptyPassword').show();
        return false;
    }else if($.trim($('#newPassword').val()).length < 10 || !isStrongPassword($.trim($('#newPassword').val()))){
    	$('#errorPasswordIsNotStrong').show();
    	return false;
    }
    return true;
}

function validateConfirmPassword() {
    if ($('#newPassword').val() != $('#newConfirmPassword').val()) {
    	$('#errorPasswordIsNotStrong').hide();
        $('#errorPasswordDoesNotMatch').show();
        return false;
    } else if ($('#newPassword').val() == $('#newConfirmPassword').val()) {
        $('#errorPasswordDoesNotMatch').hide();
        return true;
    }
    return false;
}

function hidePasswordValidationMessages() {
	$('#errorEmptyPassword').hide();
	$('#errorPasswordDoesNotMatch').hide();
	$('#errorPasswordIsNotStrong').hide();
} 

function isStrongPassword(pass){
	var regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/;//at least uppercase, lowercase and digit
	return regex.test(pass);
}

function handleSignup(xhr, status, args){
	var username = $('#newusername').val();
	var password = $('#newPassword').val();
	doLogin(username, password);
}

function hideSignUpInputs() {
    $('#blockUi').hide();
    $('#newusername').hide();
    $('#signupBtn').hide();
    $('#lastName').hide();
    $('#firstName').hide();
    $('#newPassword').hide();
    $('#newConfirmPassword').hide();
    $('#errorPasswordDoesNotMatch').hide();
    $('#errorEmptyPassword').hide();
}

function showSignUpInputs() {
	$('#login-link').removeClass("highlight");
	$("#signup-link").addClass("highlight");
    $('title').text('Sign up new account');
    if(isExisted == false){
    $('#newusername').show().val($("#username").val());
    }else{
    	$('#newusername').show().val('');
    }
    $('#signupBtn').show();
    $('#firstName').show().val('');
    $('#lastName').show().val('');
    $('#newPassword').show().val('');
    $('#newConfirmPassword').show().val('');
}

function hideLoginInputs() {
	$("#username").hide();
    $("#password").hide();
    $("#forgetPassword").hide();
    $("#rememberLogin").hide();
    $("#submitLogin").hide();
}

function showLoginInputs() {
    $('#login-link').addClass("highlight");
    $("#signup-link").removeClass("highlight");
    $('title').text('Login to My Agile');
    $("#username").show();
    $("#password").show();
    $("#forgetPassword").show();
    $("#rememberLogin").show();
    $("#submitLogin").show();
    $("#loginLdap").css("margin-left", 0);
}

function showLdapButton(){
	$('#loginLdap').show();
}

function hideLdapButton(){
	$('#loginLdap').hide();
}

function signup(){
	isExisted = true;
}

function handleRedirecLdapPage(xhr, status, args){
	var username = args.username;
	var email = $('#username').val();
	$('form').attr('action', 'https://myuser.axonactive.vn/index.php');
	$('form').attr('method','POST');
	$('<input />').attr('type','hidden')
				  .attr('name', 'username')
				  .attr('value', username)
				  .appendTo('form');
	$('<input />').attr('type','hidden')
				  .attr('name', 'email')
				  .attr('value', email)
				  .appendTo('form');
	$('form').submit();
}

function handleLdap(){
    var $pinger = $('<img/>');
    $pinger.on('load', function () {
    	if(this.width > 0){
    		showLdapButton();
    	}
    });
    $pinger.attr('src', 'https://myuser.axonactive.vn/ping.jpg?time=' + new Date().getTime());
		
}
//init tooltip for password
$(document).ready(function(){
	$(document).on("blur focusin","#newPassword",function(){
		if(!isStrongPassword($.trim($('#newPassword').val()))){
			$("#newPassword").tooltip({trigger:'focus',animation:false});
			$("#newPassword").tooltip("show");
		}else{
			$("#newPassword").tooltip("destroy");
		}
	});
	$("#forgetPassword").click(function(){
		gotoForgotPassword([ {name : 'username',value : $("#username").val()}]);
	});
});

/*************************/
