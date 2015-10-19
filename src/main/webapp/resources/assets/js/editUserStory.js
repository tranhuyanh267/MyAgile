function initUploadAttachment(url) {
	if (uploader) {
		uploader.destroy();
	}
	var uploader = new plupload.Uploader({
		runtimes : 'html5, flash',
		browse_button : 'addFile',
		max_file_size : '10mb',
		filters : [{
			title : 'extensions files',
			extensions : 'zip,rar,jpeg,png,pdf,doc,xls,xlsx,txt,ppt,pptx,docx,jpg,xml,html,xhtml,java,php,asp,aspx,css,war,groovy,gsp,jsp,rb,csv,csv'
		}],
		url : url,
		multi_selection : false,
		multiple_queues : false
	});
	uploader.bind("Init", function(up) {
		$("#upload-error").hide();
		$("#upload-alert-add-file").hide();
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
		uploadFile([ {
			name : 'filename',
			value : file.name
		},{
			name:'editedUserStoryId',
			value: $("#editUSForm\\:userStoryId").val()
		} ]);
		teamLogoBlock.hide();
		$("#upload-error").hide();
	});

	uploader.bind("UploadProgress", function(up, file) {
		$(".upload-progress").html(file.percent + "%");
		$("#upload-alert-add-file").hide();
	});
	uploader.bind("Error", function(up, err) {
		if(("File size error.").localeCompare(err.message) == 0){
			filesizeerror.show();
		}else{
			$("#upload-alert-add-file-icon").addClass("ui-message-error-icon").show();
			$("#upload-alert-add-file-message").addClass("ui-message-error-detail").text("File extension error.");
			$("#upload-alert-add-file").addClass("ui-message-error").show();
		}
		uploader.refresh();	
	});
}

$(document).ready(function(){
	$("#deleteBtn").click(function(){
		deleteFile([{
			name:'editedUserStoryId',
			value: $("#editUSForm\\:userStoryId").val()
		},{
			name:'attachmentId',
			value: $("#editUSForm\\:attId").val()
		},]);
	});
});
function sendAttId(attId){
	$("#editUSForm\\:attId").val(attId);
}

function backToViewDetail(){
	setTimeout(function(){
		viewDetail([{
			name:'editedUserStoryId',
			value: $("#editUSForm\\:userStoryId").val()
		}
		          ]);
		}, 600);
	
}