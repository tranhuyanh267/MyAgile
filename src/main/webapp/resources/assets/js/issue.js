var lengthDes=10000;
var lengthSubject=255;

function removeErrorClass(form,subject) {
	var parent = $('#'+form+'\\:' + subject);
	parent.removeClass('ui-state-error');
	parent.find('.ui-state-error').removeClass('ui-state-error');
	jQuery("#"+form+"\\:"+subject+"Msgs").css("display","none");	
}
			
function checkSubject(form,subject)
{
	if(jQuery("#"+form+"\\:"+subject).val().trim()=="")
		return false;
	else
		return true;
}
				
function handleDescriptionAndSubject(form,subject,desc)
{
	var typeSubject=checkSubject(form,subject);
	jQuery("#"+form+"\\:"+desc).find("iframe").contents().find('body').on( "keyup", function(event ) {
		var content=$(this).html();
		if (jQuery.browser.chrome) {
			content=content.replace(/\<div>/g,"<br><div>");
		}
		else if (jQuery.browser.msie) {
			content=content.replace(/\<p>/g,"<br><p>");
		}
		var listSplitBR=content.split("<br>");
		if(listSplitBR.length>=1)
		{
			if(!typeSubject)
			{
				var j=0;
				var tempHTMLContent=$("<div>"+listSplitBR[0]+"</div>").text().trim();
				while(tempHTMLContent=="")
				{
					tempHTMLContent=$("<div>"+listSplitBR[j]+"</div>").text().trim();
					j++;
					if(j>=listSplitBR.length)
						break;
				}
				removeErrorClass(form,subject);
				tempHTMLContent=tempHTMLContent.substring(0, lengthSubject);
				jQuery("#"+form+"\\:"+subject).val(tempHTMLContent);
			}
		}
	});
	
	jQuery("#"+form+"\\:"+subject).on( "keyup", function() {
		typeSubject=checkSubject(form,subject);
	});
}

function handleDescriptionAndSubjectForJavascript(form,subject,desc,controls)
{
	var editor = $("#"+form+"\\:"+desc).cleditor({ 
		controls: controls,
		width: '100%',
		height: '200px'
	}); 
	
	var typeSubject=checkSubject(form,subject);
	jQuery("#"+form+"\\:"+desc).parent().find("iframe").contents().find('body').unbind("keyup");
	jQuery("#"+form+"\\:"+desc).parent().find("iframe").contents().find('body').on( "keyup", function(e ) {
		var content=$(this).html();
		if (jQuery.browser.chrome) {
			content=content.replace(/\<div>/g,"<br><div>");
		}
		else if (jQuery.browser.msie) {
			content=content.replace(/\<p>/g,"<br><p>");
		}
		var listSplitBR=content.split("<br>");
		if(listSplitBR.length>=1)
		{
			if(!typeSubject)
			{
				var j=0;
				var tempHTMLContent=$("<div>"+listSplitBR[0]+"</div>").text().trim();
				while(tempHTMLContent=="")
				{
					tempHTMLContent=$("<div>"+listSplitBR[j]+"</div>").text().trim();
					j++;
					if(j>=listSplitBR.length)
						break;
				}
				removeErrorClass(form,subject);
				tempHTMLContent=tempHTMLContent.substring(0, lengthSubject);
				jQuery("#"+form+"\\:"+subject).val(tempHTMLContent);
			}
		}
	});
	jQuery("#"+form+"\\:"+subject).unbind("keyup");
	jQuery("#"+form+"\\:"+subject).on( "keyup", function() {
		typeSubject=checkSubject(form,subject);
	});
	
	//paste event handle
	jQuery("#"+form+"\\:"+desc).parent().find("iframe").contents().find('body').on( "paste", function(e ) {
		e.preventDefault();
		appendTextIntoEditorCleditor(editor[0],e);
		//editor[0].execCommand('inserthtml','Here some dynamic text');
	});
}


function showForm() {
	var issueForm = $('#show-issue-form');
	PrimeFaces.ab({
		source : '',
		update : 'issueForm',
		global:'false'
	});
	if (issueForm.attr('data-toggle') == 'false'
			|| issueForm.attr('data-toggle') == null) {
		issueForm.slideDown('slow');
		issueForm.attr('data-toggle', 'true');
	}
}

function closeForm() {
	var issueForm = $('#show-issue-form');
	issueForm.slideUp();
	issueForm.attr('data-toggle', 'false');
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

function handleSaveIssue(xhr, status, args) {
	if (args.edited)
		closeForm();
}

function hideOtherToggledRow() {
	$('#show-issue-form').slideUp();
	var i = $(".ui-row-toggler.ui-icon-circle-triangle-s").length;

	if (i == 1) {
		return;
	} else {
		$(".ui-row-toggler.ui-icon-circle-triangle-s").trigger(
				"click");
	}
}

function handleExportRequest(xhr, status, args) {
	if(args.isEmpty) {
		exportError.show();
	} else {
		if(args.exportType=="PDF")
			jQuery("button[id*='exportPDFButton']").click();
		else if(args.exportType=="Excel")
		{
			exportExcelChoose.show();
		}	
	}
}

function exportExcel(type) {
	jQuery("button[id*='exportExcelButton']").click();
	exportExcelChoose.hide();
}

function removeError(id) {
	var parent = $('#issueForm\\:' + id);
	parent.removeClass('ui-state-error');
	parent.find('.ui-state-error').removeClass('ui-state-error');
}


//var pathArray = window.location.pathname.split( '/' );
//var contextPath = '/'+pathArray[1]+'/upload';
function initUploadAttachment(url) {
	if (uploader) {
		uploader.destroy();
	}
	
	var uploader = new plupload.Uploader({
		runtimes : 'html5, flash',
		browse_button : 'btnChangeLogo',
		
		url : url,
		multi_selection : false,
		multiple_queues : false,
		 max_file_size : '10mb'

		
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
		updateLogo([ {
			name : 'filename',
			value : file.name
		} ]);
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

function attachEventForDataTableSelection() {
//	var checkboxHammer = $(".ui-chkbox-box").hammer(); 
	
	var parserResult = new UAParser().getResult();
	if(parserResult.os.name == 'iOS' && parserResult.os.version[0] >= 7) {
		$("tbody  .ui-chkbox-box").on("tapone", function(event) {
			event.stopPropagation();
			var me = this;
			if(!$(this).hasClass("ui-state-active")) {
				window.setTimeout(function() {
					$(me).trigger("click");
				},400);
			} else {
				$(me).trigger("click");
			}
			
		});
		
		$(".ui-datatable-data > tr > td:not(.ui-selection-column)").on("tapone", function(event) {
			var me = this;
			$(me).trigger("click");
		});
	}
	
}
