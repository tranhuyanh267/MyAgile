$(document).ready(function(){
	$(document).on("click","#msg-save-inform",function(){
		$(this).slideUp("fast",function(){
			setBoardHeight();
		});
	});
});

function paddingTopColumn(){
	if($("#boardKanban tbody tr").length == 1){
		$("#boardKanban tbody tr .column").addClass("padding-top-50");
	}
	else{
		$("#boardKanban tbody tr .column").removeClass("padding-top-50");
	}
}

function showUserStoryForm(){
	$('#fieldset-userStoryForm').slideToggle(200);
	//PrimeFaces.scrollTo('fieldset-projectSelector');
	PrimeFaces.ab({source:'',update: 'userStoryForm\:usNoteTxt userStoryForm\:usDescTxt',global:'false'});
}

function hideform(){
	$('#fieldset-userStoryForm').hide();
	return false;
}


function resetForm(){
	$("#userStoryForm\\:usNameTxt").val("");
	$("#userStoryForm\\:usValueSpner_input").val("");
	spinValue.value = 0;
	
	$("#userStoryForm\\:j_id_31_input").val("");
	spinRisk.value = 0;
	
	$("#userStoryForm\\:estimate").val("");
	
	selpriority.selectValue("NONE");
	editorDescription.clear();
	editorNote.clear();
}

function hideUserStoryForm(){
	$('#fieldset-userStoryForm').slideUp(200,function(){
		PrimeFaces.ab({source:'',update: 'userStoryForm', global:'false'});
	});
}

function removeError(id) {
	var parent = $('#userStoryForm\\:' + id);
	parent.removeClass('ui-state-error');
	parent.find('.ui-state-error').removeClass('ui-state-error');
}
