$(document).ready(function(){
	//select userstory or un select
	$(document).on("click",".icon-select-userstory",function(){
		var iconCheckEmpty = $(this).find(".icon-check-empty");
		var iconCheck = $(this).find(".icon-check");
		
		if($(iconCheckEmpty).length > 0){
			//select userstory
			var idUserStory = $(iconCheckEmpty).attr("data-id-userstory");
			selectUserStory([{name:"idUserStory",value:idUserStory}]);
			//renew class
			$(iconCheckEmpty).removeClass("icon-check-empty").addClass("icon-check");
		}else if($(iconCheck).length > 0){
			//unselect userstory
			var idUserStory = $(iconCheck).attr("data-id-userstory");
			unSelectUserStory([{name:"idUserStory",value:idUserStory}]);
			//renew class
			$(iconCheck).removeClass("icon-check-empty").addClass("icon-check-empty");
			//uncheck all
			if($(".icon-select-unselect-all").hasClass("icon-check")){
				$(".icon-select-unselect-all").removeClass("icon-check").addClass("icon-check-empty");
			}
		}
	});//end select/unselect userstory
	
	//select all or unselect all
	$(document).on("click",".select-or-unselect-all-userstory",function(){
		selectOrUnSelectAllUserStory();
	});
});//end ready

function triggerClickExportPdf(){
	$("button[id*='exportPDFButton']").click();
}

function showUserStoryForm(){
	$('#fieldset-userStoryForm').slideDown(200);
	PrimeFaces.scrollTo('fieldset-projectSelector');
	PrimeFaces.ab({source:'',update: 'userStoryForm\:usDescTxt',global:'false'});
}

function hideUserStoryForm(){
	$('#fieldset-userStoryForm').slideUp(200,function(){
		PrimeFaces.ab({source:'',update: 'userStoryForm', global:'false'});
	});
}

function fillZeroTextInProgressBar() {
	jQuery(document).ready(function( $ ) {
		$( ".ui-progressbar-label" ).each(function( index  ) {
			if($(this).html().trim().length == 0 || $(this).html() == "0%") {
				$(this).html("0%").show();
			}
		});
	});
}

function initUploadAttachment(url) {
	if(uploader) {
		uploader.destroy();
	}			
	var uploader = new plupload.Uploader({
		runtimes : 'html5, flash',
		browse_button: 'pickfiles',
		url : url,
		max_file_size : '10mb',
		multi_selection: false,
		multiple_queues : false
	});
	
	uploader.bind("Init", function(up) {
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
	});	
	
	uploader.bind("Error", function(up, err) {
		if(("File size error.").localeCompare(err.message) == 0){
			pblfilesizeerror.show();
		}
	});
}

function handleCreateComplete(xhr, status, args) {
	if(args.success){
		PrimeFaces.ab({source:'',update: 'form-userStoryTable'});
	}
}

function removeError(id) {
	var parent = $('#userStoryForm\\:' + id);
	parent.removeClass('ui-state-error');
	parent.find('.ui-state-error').removeClass('ui-state-error');
}

//upload excel file to create user story
function initUploadExcelFile(idBtUpload, url,filename,projectID) {
	if (uploader) {
		uploader.destroy();
	}
	var uploader = new plupload.Uploader({
		runtimes : 'html5, flash',
		browse_button : idBtUpload,
		max_file_size : '10mb',
		url : url,
		filters : [{
			title : 'Excel files',
			extensions : 'xlsx,xls'
		}],
		multi_selection : false,
		multiple_queues : false
	});

	uploader.bind("Init", function(up) {
		$("#uploadExcelMessages").hide();
	});
	
	if(projectID!=0){
		uploader.init();
	}
	
	uploader.bind("BeforeUpload", function(up, file) {
		up.settings.multipart_params = {
			upload_type : 'attachment-file'
		};
	});
	
	uploader.bind("FilesAdded", function(up, Files) {
		for(var j in Files){
			var name="tempExcel"+ new Date().getTime();
            file=Files[j];
            file.name = name+"."+file.name.split('.').pop();
            Files[j]=file;
        }
		uploader.start();
	});
	
	uploader.bind("FileUploaded", function(up, file, res) {
		uploadExcelClient([ {
			name : filename,
			value : file.name
		} ]);
	});
	
	uploader.bind('Error', function(up, error) {
		changeMessageType($("#uploadExcelMessages"),2);
		$("#uploadExcelMessages span").eq(2).html("File extension error (Support: .xls,.xlsx)");
		$("#uploadExcelMessages").slideDown();
	});
}

//upload user stories by excel file
function handleResultExcelUpload(xhr, status, args){
	changeMessageType($("#uploadExcelMessages"),args.result);
	var moreInfo=" <a onclick='$(\"#excelMessToggleDiv\").toggle();'>" +
			"<span style='color:#C09853' " +
			"class='ui-button-icon-left ui-icon ui-c icon-double-angle-down'>" +
			"</span>" +
			"</a>";
	var htmlDisplay="<strong>"+args.numRows;
	if(args.messageExcelUpload)
	{
		htmlDisplay+=moreInfo+"</strong><br/>";
		var tararray = eval('('+ args.messageExcelUpload +')');
		htmlDisplay+="<div id='excelMessToggleDiv' style='display:none'><ul>";
	    for (var i=0;i<tararray.length;i++)
	    {
	    	htmlDisplay+="<li>";
	    	htmlDisplay+=tararray[i]+"<br/>";
	    	htmlDisplay+="</li>";
	    }
	    htmlDisplay+="</ul></div>";
	}
	else
	{
		htmlDisplay+="</strong><br/>";
		setTimeout(function(){$("#uploadExcelMessages").slideUp();},10000);
	}
	
	$("#uploadExcelMessages span").eq(2).html(htmlDisplay);
	$("#uploadExcelMessages").slideDown();
}

function changeMessageType(divMessage,type)
{
	var classDiv=$(divMessage).prop('class');
	if(type==2)
		$(divMessage).prop('class',classDiv.replace(classDiv.split(" ")[0],"ui-message-error"));
	else if(type==1)
		$(divMessage).prop('class',classDiv.replace(classDiv.split(" ")[0],"ui-message-info"));
	else if(type==3)
		$(divMessage).prop('class',classDiv.replace(classDiv.split(" ")[0],"ui-messages-warn"));
}

$("button").bind("click", function() {
	$("#uploadExcelMessages").hide();
});
