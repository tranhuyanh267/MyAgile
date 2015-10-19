function showForm() {
	$('#show-sprint-form').slideDown(200);
}

function closeForm() {
	$('#show-sprint-form').slideUp(200);
}

function handleCreateRequest(xhr, status, args) {
	if (args.create)
		showForm();
}

function handleEditRequest(xhr, status, args) {
	if (args.edit)
		showForm();
}

function handleSaveRequest(xhr, status, args) {
	showForm();
}
function handleSave(xhr, status, args){
	if(args.save == true){
		$('#SaveMsgs').show(0).delay(3000).hide(0);
	}else{
		$('#SaveMsgs').hide();
	}
	if(args.edit == true){
		$('#EditMsgs').show(0).delay(3000).hide(0);
	}else{
		$('#EditMsgs').hide();
	}
}