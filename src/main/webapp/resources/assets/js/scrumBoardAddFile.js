function initUploadAttachment(url) 
{
	if(uploader) {
	uploader.destroy();
	}
	
	var uploader = new plupload.Uploader({
		runtimes : 'html5, flash',
		browse_button: 'pickfiles',
		max_file_size : '10mb',
		filters : [{
			title : 'extensions files',
			extensions :'zip,rar,jpeg,png,pdf,doc,xls,xlsx,txt,ppt,pptx,docx,jpg,xml,html,xhtml,java,p hp,asp,aspx,css,war,groovy,gsp,jsp,rb,csv'
		}],
		url : url,
		multi_selection: false,
		multiple_queues : false
	});

	uploader.bind("Init", function(up) {
		$("#upload-alert-add-file").hide();
	});
	
	uploader.init();
	uploader.bind("BeforeUpload", function(up, file) {
		up.settings.multipart_params = {
		upload_type : 'attachment-file'
		};
	});
	
	uploader.bind("FilesAdded", function() {
		uploadAttBlock.show();
		uploader.start();
	});
	
	uploader.bind("FileUploaded",function(up,file,res) {
		updateAtt([{ name : 'filename', value : file.name }]);
		uploadAttBlock.hide();
	});
	
	uploader.bind("UploadProgress", function(up, file) {
		$(".upload-progress").html(file.percent+"%");
		$("#upload-alert-add-file").hide();
	});
	
	uploader.bind("Error", function(up, err) {
		if(("File size error.").localeCompare(err.message) == 0){
			pblfilesizeerror.show();
		}else{
			$("#upload-alert-add-file-icon").addClass("ui-message-erroricon").show();
			$("#upload-alert-add-file-message").addClass("ui-message-errordetail").text("File extension error.");
			$("#upload-alert-add-file").addClass("ui-message-error").show();
		}
		uploader.refresh();
	});
}