function checkNullEmail(){
	var user = $('#editProfile\\:username').val();
	if(user.indexOf("@") > 0 ){
		$('#editProfile\\:username').attr('readonly','true');
	}
}

var func = function() { 
	var email= $('#editProfile\\:username').val().trim();
	$('#editProfile\\:username').val(email);
	var regName = /^[\w-]+(?:\.[\w-]+)*@(?:[\w-]+\.)+[a-zA-Z]{2,7}$/;
	var htmlPrepareImage = '<img style="vertical-align: baseline !important;" src="resources/img/ajax_loader/spinner.gif" />';
	$("#loadSpinnerImage").fadeIn(100, 0);
	$("#loadSpinnerImage").html(htmlPrepareImage);
	$("#loadSpinnerImage").fadeOut(2500);
	if(regName.test(email)){
			checkMember([ {	name : 'username',value : email	} ]);
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

function handleEmail(xhr, status, args){
		if(args.emailExist == true){
			confirmDialog.show();
			}
		}

function handleConfirm(xhr, status, args){
	if(args.correctPass == false){
		$('#incorrectError').show();
	}
}

function handleOpenDialog(){
	$('#editProfile\\:password').show();
	$('#editProfile\\:labelCon').show();
	$('#editProfile\\:confirmBtn').show();
	$('#editProfile\\:YesBtn').hide();
}
function handleCloseDialog(){
	$('#editProfile\\:password').hide();
	$('#editProfile\\:password').val('');
	$('#editProfile\\:labelCon').hide();
	$('#editProfile\\:confirmBtn').hide();
	$('#editProfile\\:YesBtn').show();
	$('#editProfile\\:username').val('');
	$('#incorrectError').hide();
	confirmDialog.hide();
}
function toggleListEmail(){
	$('#editProfile\\:emaiListPane').toggle();
}
function handleAddEmail(){
	$('#editProfile\\:addEmailPane').toggle();
}

function handleAddMailComplete(xhr, status, args){
	if(args.success){
		removeValidate();
		$('#editProfile\\:addEmailPane').hide();
	}else{
		$('#editProfile\\:addEmailPane').show();
	}	
}

function removeValidate(){
	removeErrorClass("editProfile","addEmailInput");
}

function removeValidateMessage(){
	$("#editProfile\\:addNewEmailMsg").css("display","none");
}

function removeErrorClass(form,subject) {
	var parent = $('#'+form+'\\:' + subject);
	parent.removeClass('ui-state-error');
	parent.find('.ui-state-error').removeClass('ui-state-error');
	jQuery("#"+form+"\\:"+subject+"Msgs").css("display","none");	
}



	
	