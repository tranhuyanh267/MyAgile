function showForm(){
	var teamform = $('#show_team_form');
	if(teamform.attr('data-toggle')=='false' || teamform.attr('data-toggle')==null) {
		teamform.slideDown(200);
		teamform.attr('data-toggle','true');
	}
}

function closeForm(){
	var teamform = $('#show_team_form');
	teamform.slideUp(200);
	teamform.attr('data-toggle','false');
}

function handleCreateRequest(xhr,status,args){
	if(args.create){
		showForm();
	}
}

function handleEditRequest(xhr,status,args){
	if(args.edit){
		showForm();
	}
}

function initUpload(url){
	if(uploader) {
		uploader.destroy();
	}			
	var uploader = new plupload.Uploader({
			runtimes : 'html5, flash',
		browse_button: 'btnChangeLogo',
		max_file_size : '10mb',
		url : url,
		filters : [
			{title : "Image files", extensions : "jpg,png"}
		],
		multi_selection: false,
		multiple_queues : false
	});

	uploader.bind("Error", function(up, err) {
		$("#upload-error").show();
		$("#upload-error").html(err.message);
	});
	
	uploader.bind("Init", function(up) {
		$("#upload-error").hide();
	});
	
	uploader.init();
	
	uploader.bind("BeforeUpload", function(up, file) {
		 up.settings.multipart_params = {
					 upload_type : 'team-logo'
		};
	});				
	
	uploader.bind("FilesAdded", function() {
		teamLogoBlock.show();
		uploader.start();
	});
	
	uploader.bind("FileUploaded",function(up,file,res) {
		updateLogo([{ name : 'filename', value : file.name }]);
		teamLogoBlock.hide();
		$("#upload-error").hide();
	});
	
	uploader.bind("UploadProgress", function(up, file) {
		$(".upload-progress").html(file.percent+"%");
	});
}

function initMemberForm(){
	$(".member-avatar .ui-commandlink").hide();
	$("#sel").on("change", "select", function() {
		$("div[class='member-profile']").hide();
		$("div[name='" + $(this).val() + "']").show();
	});

	$(".member-profile").on("mouseover", function() {
		$(this).css('background-color','#F6F6F6');					
	});
	
	$(".member-profile").on("mouseout", function() {
		$(this).css('background-color','white');
	});
	
	$(".member-avatar").on("mouseover", function() {
		$(this).children("a").show();
	});
	
	$(".member-avatar").on("mouseout", function() {
		$(this).parent().css('border-color','none');
		$(this).parent().css('border-style','none');
		$(this).children("a").hide();
	});
}

function handleSave(xhr, status, args) {
	if (args.save == true) {
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



