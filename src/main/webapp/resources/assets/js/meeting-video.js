function initMeetingVideoUploader(contextPath, params) {

	var videoUploader = new plupload.Uploader({
		runtimes : 'html5,flash',
		browse_button : 'meeting-choosefile',
		url : contextPath+'/upload',
		max_file_size : '10gb',
		filters : [{
			title : 'Video Files',
			extensions : 'mp4'
		}],
		multiple_queues : false,
	    multi_selection : false
	});
	
	videoUploader.bind('Init', function(up, params) {
		$("#upload-container").hide();
	});
	
	
	videoUploader.init();
	
	videoUploader.bind('BeforeUpload', function(up,file) {
		$("#"+file.id).find("#upload-status").addClass("text-info").text("Uploading");
	});
	
	videoUploader.bind('FilesAdded', function(up, files) {
		$("#upload-alert").hide();
		if(videoUploader.files.length > 1) {
			videoUploader.removeFile(videoUploader.files[0]);
			videoUploader.refresh();
		}
		$.each(files, function(i,file) {
		
			var divUploadProgress = $('<div id="'+file.id+'" class="upload-progress"></div>');
			
			// Upload Info, Progress Bar, Remove
			var divUploadInfo = $('<div class="row"><div id="file-name" style="width:auto;" class="text-left span4">'+file.name+' (<a id="remove-'+file.id+'" href="#"><i class="icon-remove"></i></a>)</div><div id="upload-status" style="float:left !important;" class="text-right pull-right span1">Ready</div></div>');
			var divProgressBar = $('<div class="progress"><div id="progress-bar" class="bar bar-info"></div></div>');
	
			// Add them to upload-progress wrapper
			$(divUploadProgress).append(divUploadInfo).append(divProgressBar);
	
			// Add upload-progress wrapper to upload-container
			$("#upload-container").append(divUploadProgress);
		
		});
		$("#upload-container").show();
		toggleUploadButton();
	});
	
	videoUploader.bind('BeforeUpload', function(up,file) {
		$("#"+file.id).find("#upload-status").addClass("text-info").text("Uploading");
	});
	
	// Uploader progress
	videoUploader.bind('UploadProgress', function(up,file) {
		// Update Progress
		$("#"+file.id).find("#file-name").html(file.name+" ("+file.percent+"%)");
		$("#"+file.id).find("#progress-bar").css("width",file.percent+"%");
	});
	
	videoUploader.bind('FileUploaded', function(up,file, result) {
		updateMeetingVideo([{ name : 'filename', value : file.name }, 
		                    {name : 'teamId', value : params.teamId }, 
		                    {name : 'sprintId', value : params.sprintId}]);
		$("#"+file.id).find("#upload-status").removeClass("text-info").addClass("text-success").text("Finished");
		
	});
	
	videoUploader.bind('FilesRemoved',function(up,files) {
		// Fade out and remove file item in upload container
		$("a[id^='remove-"+files[0].id+"']").parents(".upload-progress").fadeOut(function(){
			$(this).remove();
			toggleUploadButton();
		});
	
		if(videoUploader.files.length == 0)
		{
			if($("#upload-alert").hasClass("ui-message-error"))
			{
				$("#upload-alert-icon").removeClass("ui-message-error-icon").addClass("ui-message-info-icon").show();
				
			}
			$("#upload-alert-message").removeClass("ui-message-error-detail").addClass("ui-message-info-detail").text("File extension allowed: .mp4");
			$("#upload-alert").removeClass("ui-message-error").addClass("ui-message-info").show();
		}
		
	});
	
	videoUploader.bind('Error', function(up, err) {
		if(("File size error.").localeCompare(err.message) == 0){
			videofilesizeerror.show();
		}else{
			$("#upload-alert-icon").removeClass("ui-message-info-icon").addClass("ui-message-error-icon").show();
			$("#upload-alert-message").removeClass("ui-message-info-detail").addClass("ui-message-error-detail").text(err.message);
			$("#upload-alert").removeClass("ui-message-info").addClass("ui-message-error").show();
		}
	});
	
	$("#upload-container").on("click","a[id^='remove-']", function() {
		// Find file ID
		var fileID = $(this).attr("id").split("-")[1];
		// Remove file
		videoUploader.removeFile(videoUploader.getFile(fileID));
		return false;
	});
	
	videoUploader.bind("UploadComplete", function(up,files) {
		//$(location).attr("href","");
		$("button[id^='meeting-']").not("#meeting-submitfile").prop("disabled",false);
		$("div[id^='"+files[0].id+"']").fadeOut(function(){
			$(this).remove();
		});
		$("#upload-alert-icon").removeClass("ui-message-error-icon").addClass("ui-message-info-icon").show();
		$("#upload-alert-message").removeClass("ui-message-error-detail").addClass("ui-message-info-detail").text("File Uploaded Successfully.");
		$("#upload-alert").removeClass("ui-message-error").addClass("ui-message-info").show();
	});
	
	$("#meeting-submitfile").bind("click", function() {
		if(videoUploader.files.length != 0 && $("#no-meeting-video").length == 0)
		{
			cfmUpdateVideo.show();
		} else {
			startMeetingVideoUpload();
		}
		
		return false;
	});
	
	$("#btnYesUpload").bind("click", function() {
		cfmUpdateVideo.hide();
		startMeetingVideoUpload();
		return false;
	});
	$("#btnNoUpload").bind("click", function() {
		cfmUpdateVideo.hide();
		return false;
	});
	
	function startMeetingVideoUpload() {
		// Disable all input fields
		$("button[id^='meeting-']").prop("disabled",true);

		videoUploader.settings.multipart_params = params;
		videoUploader.start();
	}

	
	function toggleUploadButton() {
		switch($("#meeting-submitfile").is(":disabled")) {
			case true:
				$("#meeting-submitfile").prop("disabled", false);
				break;
			case false:
				$("#meeting-submitfile").prop("disabled",true);
				break;
		}
	}
}