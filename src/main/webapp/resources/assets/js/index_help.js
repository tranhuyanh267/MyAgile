function initEditDescription() {
	$(".description-editor").summernote({
		  height:140,
		  toolbar: [
          ['color', ['color']],
		    ['style', ['bold', 'italic', 'underline', 'clear']],
		    ['fontsize', ['fontsize']],
		    ['para', ['ol', 'paragraph']],
		    ['fullscreen', ['fullscreen']],
		    ['help', ['help']] 
		  ]
	});
}

function showmessage() {
	$(".message-thank").removeClass("display-none");
	setTimeout(function(){
		$(".cl-close-dialog").trigger("click");
		$(".message-thank").addClass("display-none");
	},3000);
}

function beforeSendFeedback() {
	 $(".td-for-description").find("input[type='hidden']").val($(".description-editor").code());
}

function initUploadAttachment(url) {
	if (uploader) {
		uploader.destroy();
	}
	var uploader = new plupload.Uploader({
		runtimes : 'html5, flash',
		browse_button : 'addFile',
		max_file_size : '10mb',
		url : url,
		multi_selection : false,
		multiple_queues : false
	});
	uploader.bind("Init", function(up) {
		$("#upload-error").hide();
	});

	uploader.init();

	uploader.bind("BeforeUpload", function(up, file) {
		up.settings.multipart_params = {
			upload_type : 'attachment-file'
		};
	});

	uploader.bind("FilesAdded", function() {
		teamLogoBlock.show();
		uploader.start();
	});

	uploader.bind("FileUploaded", function(up, file, res) {
		console.log(res);
		if(res.status == "200") {
			uploadFile([{
				name : 'filename',
				value : file.name
			}]);
		}
		teamLogoBlock.hide();
		$("#upload-error").hide();
	});

	uploader.bind("UploadProgress", function(up, file) {
		$(".upload-progress").html(file.percent + "%");
	});
	uploader.bind("Error", function(up, err) {
		if(("File size error.").localeCompare(err.message) == 0){
			filesizeerror.show();
		}
	});
}