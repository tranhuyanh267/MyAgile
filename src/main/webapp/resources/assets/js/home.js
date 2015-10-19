/////Product Backlog////
function controlEditUS(id){
	$('#editedUsSession'+id).show();	
	$('#showedUsSession'+id).hide();
}	

function controlAdd(){			
	removeErrorClass("addNewForm","usName");
	$('#addNewForm').slideDown("200");
	$('#frmEditUs').hide();
}

function closeAdd(){
	$('#addNewForm').slideUp("200",function(){
		hideMessage();
		$('#addBtn').show();
		removeErrorClass("addNewForm","usName");
		resetAddForm();
	});
}		

function toggleUs(size){
	var linkToggle=$("[id$=linkToggle]");
	var typeOfToggle=1;
	if(linkToggle.html().indexOf("Collapse All")>=0){
		linkToggle.html('<span class="ui-button-icon-left ui-icon ui-c icon-resize-full"></span>'
				+'<span class="ui-button-text ui-c">Expand All</span>');
	}else{
		linkToggle.html('<span class="ui-button-icon-left ui-icon ui-c icon-resize-small"></span>'
				+'<span class="ui-button-text ui-c">Collapse All</span>');
		typeOfToggle=0;
	}
	for (var x=0;x < size;x++)							
	{ 		
		var panel = $("a#usDetailForm\\:toogleLink\\:"+x+"\\:panel_toggler");
		var html = panel.html();
		if(typeOfToggle==0){
			if(html && panel.html().indexOf('ui-icon-minusthick')>=0) continue;						
		}
		if(typeOfToggle==1){
			if(html && panel.html().indexOf('ui-icon-plusthick')>=0)	continue;					
		}
		panel.click();	
	}		
	return false;
}

function handleComplete(xhr, status, args) {
	if(args.success){
		$("#addNewForm\\:successMessage").css('display','inline');
		$("#addNewForm\\:successMessage").show(0).delay(5000).hide(0);
	}else{
		$("#addNewForm\\:UnsuccessMessage").css('display','inline');
		$("#addNewForm\\:UnsuccessMessage").show(0).delay(5000).hide(0);
	}			
	
}

function hideMessage(){
	$("#addNewForm\\:messages").css('display','none');
}

function fnc(inputComponent) {
  var length = inputComponent.value.trim().length;
  var css=$(inputComponent).attr('class');
  if(length!=0)
  		$(inputComponent).attr('class', css.replace("ui-state-error",""));
  else
	  $(inputComponent).attr('class', css+" ui-state-error");
 }

function handleCompleteUpdate(xhr, status, args){
	if(args.error!=3){
		var id = args.userStoryId;
		$('#msn'+id).hide();
		$('#ms2n'+id).hide();
		$('#ms3n'+id).hide();
		
		switch(args.error){
			case 1: {
				$('#msn'+id).show(); break;
			}
			case 2: {
				$('#ms2n'+id).show(); break;
			}
			case 4: {
				$('#ms3n'+id).show(); break;
			}
			case 0: {
				$('#msn'+id).hide();
				$('#ms2n'+id).hide();
				$('#ms3n'+id).hide();
				PrimeFaces.ab({
					source:'',
					update:'pnlEditUs'
				});
				return false;
			}
		}
	}
	if(args.success){
		PrimeFaces.ab({source:'',update: 'pnlEditUs'});
	}
}
function handleProgressBar(progressList){
	var header=$('div:regex(id,usDetailForm.*panel_header$)');	
	var string= getVendorPrefix().charAt(0).toLowerCase() + getVendorPrefix().slice(1);
	var doneColor='#0E8B43';
	var notDoneColor='#27ae60';		
	$.each(header, function( index, value ) {		
		$(this).css({
 			background:  '-'+string+'-linear-gradient(left,'+doneColor+' '+progressList[index]+'%,'+notDoneColor+' '+progressList[index]+'% )'
 		});		
	});
}		

//////////////////////////////////////////////////////

////Impediment////
function clearForm(){
	$('#impeForm\\:description').val('');
	$('.error-desc-edit').hide();
	$('.error-desc').hide();
	
}

function closeImpForm(){
	$('#impeForm').slideUp('200', function(){$('#btnNewImpediment').show();clearForm();});
}

function handleSaveImpe(){
	var impeForm = $('#impeForm');
	if(impeForm.find('textarea').val().trim().length==0){
		impeForm.find('.error-desc').show() ; 
		return true;
	} 
	else if(impeForm.find('textarea').val().trim().length>255){
		$('.error-desc-length').show(); 
		return true;
	}
	return false;
}

function controlNewImpe(){
		$('#impeForm').slideDown("200");
		$('#btnNewImpediment').hide();
}

function closeForm(){
	$('#impeForm').slideUp("200",function(){
		$('#btnNewImpediment').show();
	});
}

function editUs(id){
	$('.editUsSession'+id).show();	
	$('.primeUsDetail'+id).hide();
}
function editRetro(id){
	$('.editRetroSession'+id).show();	
	$('.primeRetroDetail'+id).hide();
}
function closeUs(id){
	$('.editUsSession'+id).hide();	
	$('.editUsSession'+id).find('textarea').val($('.primeUsDetail'+id).find('span').html());
	$('.primeUsDetail'+id).show();
}
function closeRetroEdit(id){
	$('.editRetroSession'+id).hide();	
	$('.editRetroSession'+id).find('textarea').val($('.primeRetroDetail'+id).find('span').html());
	$('.primeRetroDetail'+id).show();
}
function testDesc(dt){				
	if($(dt).parent().find('textarea').val()=='') {$('.error-desc').show(); 
	return false;}
	if($(dt).parent().find('textarea').val().length > 255 ) {$('.error-desc-length').show(); 
	return false;}
	return true;
	}
function checkEditDesc(dt){
	if($(dt).parent().find('textarea').val()==''){
		$(dt).parent().find('.error-desc-edit').show(); 
		return false;
		}
	return false;
	}

function onSaveNew(th){
	if($(th).parent().find('textarea').val().trim().length==0){
		$('.error-desc').show(); return false;
		} 
	else if($(th).parent().find('textarea').val().trim().length>255){
		$('.error-desc-length').show(); 
		return false;
		}
}

// ========== Edit US ===========

function removeError(id) {
	var parent = $('#frmEditUs\\:' + id);
	parent.removeClass('ui-state-error');
	parent.find('.ui-state-error').removeClass('ui-state-error');
}

function controlEdit(){	
	$('#addNewForm').hide();
	$('#pnlEditUs').slideDown("200");
}

function closeEditForm(){
		document.getElementById("frmEditUs").reset();
		$('#pnlEditUs').hide();
		removeErrorClass("frmEditUs","usEditName");
}

function initUploadAttachmentEdit(url) {
	if(uploader) {
		uploader.destroy();
	}			
	var uploader = new plupload.Uploader({
		runtimes : 'html5, flash',
		browse_button: 'pickFileEdit',
		max_file_size : '10mb',
		filters : [{
			title : 'extensions files',
			extensions : 'zip,rar,jpeg,png,pdf,doc,xls,xlsx,txt,ppt,pptx,docx,jpg,xml,html,xhtml,java,php,asp,aspx,css,war,groovy,gsp,jsp,rb,csv,csv'
		}],
		url : url,
		multi_selection: false,
		multiple_queues : false
	});
	
	uploader.bind("Init", function(up) {
		$("#upload-alert-add-file-edit").hide();
	});
	
	uploader.init();
	
	uploader.bind("BeforeUpload", function(up, file) {
		up.settings.multipart_params = {
			upload_type : 'attachment-file'
		};
	});
					
	uploader.bind("FilesAdded", function() {
		uploadAttEditBlock.show();
		uploader.start();
	});
	
	uploader.bind("FileUploaded",function(up,file,res) {
		updateAttEdit([{ name : 'filename', value : file.name }]);
		uploadAttEditBlock.hide();
	});
	
	uploader.bind("UploadProgress", function(up, file) {
		$(".upload-progress").html(file.percent+"%");
		$("#upload-alert-add-file-edit").hide();
	});	
	
	uploader.bind("Error", function(up, err) {
		if(("File size error.").localeCompare(err.message) == 0){
			pblfilesizeerror.show();
		}else{
			$("#upload-alert-add-file-icon-edit").addClass("ui-message-error-icon").show();
			$("#upload-alert-add-file-message-edit").addClass("ui-message-error-detail").text("File extension error.");
			$("#upload-alert-add-file-edit").addClass("ui-message-error").show();
		}
		uploader.refresh();	
	});
	
}

function removeErrorClass(form,subject) {
	var parent = $('#'+form+'\\:' + subject);
	parent.removeClass('ui-state-error');
	parent.find('.ui-state-error').removeClass('ui-state-error');
	jQuery("#"+form+"\\:"+subject+"Msgs").css("display","none");	
}