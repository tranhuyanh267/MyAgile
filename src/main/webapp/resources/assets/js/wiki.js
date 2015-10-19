$(document).ready(function() {
	$(".attach-file").on("click",function(event){
		event.preventDefault();
	});
	
	$(document).on("click","#all-contents a.linkWiki",function(e){
		var idTopic = $(this).attr("data-id-topic"),
			titleTopic = $(this).text(); 
		sendData([{name:'idTopic',value:idTopic}, {name: "titleTopic", value: titleTopic}]);
		e.preventDefault();
	});
});

function initUploadAttachment(url) {
	if (uploader) {
		uploader.destroy();
	}
	var uploader = new plupload.Uploader({
		runtimes : 'html5, flash',
		browse_button : 'btnAddNewFile',
		max_file_size : '10mb',
		filters : [{
			title : 'extensions files',
			extensions :'zip,rar,jpeg,png,pdf,doc,xls,xlsx,txt,ppt,pptx,docx,jpg,xml,html,xhtml,java,php,asp,aspx,css,war,groovy,gsp,jsp,rb,csv'
		}],
		url : url,
		multi_selection : false,
		multiple_queues : false
	});

	uploader.bind("Error", function(up, err) {
		$("#upload-error").show();
		$("#upload-error").html(err.message);
		$("#upload-alert-add-file-icon").addClass("ui-message-error-icon").show();
		$("#upload-alert-add-file-message").addClass("ui-message-error-detail").text("File extension error.");
		$("#upload-alert-add-file").addClass("ui-message-error").show();
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
		updateLogo([ {
			name : 'filename',
			value : file.name
		} ]);
		teamLogoBlock.hide();
		$("#upload-error").hide();
	});

	uploader.bind("UploadProgress", function(up, file) {
		$(".upload-progress").html(file.percent + "%");
		$("#upload-alert-add-file").hide();
	});
}

/*
 * function initUploadAttachment(url) { if (uploader) { uploader.destroy(); }
 * var uploader = new plupload.Uploader({ runtimes : 'html5, flash',
 * browse_button : 'btnAddNewFile', max_file_size : '10mb', url : url,
 * multi_selection : false, multiple_queues : false });
 * 
 * uploader.bind("Error", function(up, err) { $("#upload-error").show();
 * $("#upload-error").html(err.message); });
 * 
 * uploader.bind("Init", function(up) { $("#upload-error").hide(); });
 * 
 * uploader.init();
 * 
 * uploader.bind("BeforeUpload", function(up, file) {
 * up.settings.multipart_params = { upload_type : 'attachment-file' }; });
 * 
 * uploader.bind("FilesAdded", function() { teamLogoBlock.show();
 * uploader.start(); });
 * 
 * uploader.bind("FileUploaded", function(up, file, res) { updateLogo([ { name :
 * 'filename', value : file.name } ]); console.log("update logo");
 * teamLogoBlock.hide(); $("#upload-error").hide(); });
 * 
 * uploader.bind("UploadProgress", function(up, file) {
 * $(".upload-progress").html(file.percent + "%"); }); }
 */
