function pageLoading() {
	$('body')
			.append(
					"<span id='pop' class='ajaxPopUp'><img id='deleteProjImg' src='resources/img/ajax_loader/ajax_loader.gif' class='imgAjax' alt='ajax loader' /></span>");
}
function pageLoadingComplete() {
	$('#pop').remove();
	$("div[id*='modal']").remove();
}
function hideOtherToggledRow() {
	$('#panelAddProject').slideUp();
	var i = $(".ui-row-toggler.ui-icon-circle-triangle-s").length;

	if (i == 1) {
		return;
	} else {
		$(".ui-row-toggler.ui-icon-circle-triangle-s").trigger("click");
	}
}

function showPanel() {
	$('#panelAddProject').slideDown();
	$('#formParentSlide\\:slideFormNew').hide();
};

function hidePanel() {
	$('#panelAddProject').slideUp();
	$('#formParentSlide\\:slideFormNew').show();
};

function handleComplete(xhr, status, args) {
	if (args.saved) {
		$('#panelAddProject').slideUp();
	}
};

function handleListComplete(xhr, status, args) {
	if (args.flagEmpty) {
		$('#showMessageProjectName').show();
		$('#showMessageProjectName').html('Project name can not empty');
	}

	if (args.flagExist) {
		$('#showMessageProjectName').show();
		$('#showMessageProjectName').html('Project name already exists');
	}
	if (args.flagLength) {
		$('#showMessageProjectName').show();
		$('#showMessageProjectName').html(
				'Length is max than allowable maximum of 255');
	}

	if (args.flagLengthLess) {
		$('#showMessageProjectName').show();
		$('#showMessageProjectName').html(
				'Length is less than allowable minimum of 6');
	}

	if (args.flagLengthDesc) {
		$('#showMessageDesc').show();
		$('#showMessageDesc').html(
				'Length is max than allowable maximum of 500');
	}
};

var contextPath = 'upload';

var uploaderNew = new plupload.Uploader({
	runtimes : 'html5, flash',
	browse_button : 'btnChangeLogoNew',
	max_file_size : '10mb',
	url : contextPath,
	filters : [ {
		title : "Image files",
		extensions : "jpg,png"
	} ],
	multi_selection : false,
	multiple_queues : false
});

function initUploader(mode) {
	var path = 'upload';

	if (uploaderEdit) {
		uploaderEdit.destroy();
	}

	if (uploaderNew) {
		uploaderNew.destroy();
	}

	if (mode == 'edit') {

		var uploaderEdit = new plupload.Uploader({
			runtimes : 'html5, flash',
			browse_button : 'btnChangeLogo',
			max_file_size : '10mb',
			url : path,
			filters : [ {
				title : "Image files",
				extensions : "jpg,png"
			} ],
			multi_selection : false,
			multiple_queues : false
		});

		uploaderEdit.bind("Init", function() {
			$("#upload-alert").hide();
		});

		uploaderEdit.init();

		uploaderEdit.bind("Error", function(up, err) {
			$("#upload-alert-message").text(err.message);
			$("#upload-alert").show();
			uploaderEdit.refresh();

		});

		uploaderEdit.bind("Destroy", function() {
		});

		uploaderEdit.bind("BeforeUpload", function(up, file) {
			if (mode == 'edit') {
				up.settings.multipart_params = {
					upload_type : 'project-logo'
				};
			}
		});

		uploaderEdit.bind("FilesAdded", function(up, files) {
			projectLogoBlock.show();
			uploaderEdit.start();

		});
		uploaderEdit.bind("FileUploaded", function(up, file, res) {
			updateLogo([ {
				name : 'filename',
				value : file.name
			} ]);
			projectLogoBlock.hide();
			$("#upload-error").hide();
			uploaderEdit.refresh();
		});

		uploaderEdit.bind("UploadProgress", function(up, file) {
			$(".upload-progress").html(file.percent + "%");
		});

	}

	else if (mode == 'new') {

		uploaderNew.bind("Init", function() {
			$("#upload-add-alert").hide();
		});

		uploaderNew.init();

		uploaderNew.bind("Error", function(up, err) {
			$("#upload-add-alert-message").text(err.message);
			$("#upload-add-alert").show();
			uploaderNew.refresh();
		});

		uploaderNew.bind("BeforeUpload", function(up, file) {
			if (mode == 'new') {
				up.settings.multipart_params = {
					upload_type : 'project-logo'
				};
			}
		});

		uploaderNew.bind("FilesAdded", function() {
			projectLogoBlockNew.show();
			uploaderNew.start();
		});

		uploaderNew.bind("FileUploaded", function(up, file, res) {
			updateLogoNew([ {
				name : 'filenameNew',
				value : file.name
			} ]);
			projectLogoBlockNew.hide();
			$("#upload-add-alert").hide();
		});

		uploaderNew.bind("UploadProgress", function(up, file) {
			$(".upload-progress-new").html(file.percent + "%");
		});
	}
}

function handleSave(xhr, status, args) {
	if (args.deletes == true) {
		$('#DeleteSuccess').show(0).delay(10000).hide(0);
	} else {
		$('#DeleteFail').show(0).delay(10000).hide(0);
	}
}
var projectId;
function jsShowDialog(id) {
	projectId = id;
	confirmation.show();
}

function ParseParamToBean() {
	ToBean([{name:"projId", value:projectId}]);
}