var idGlobalColumnKanbanBackLog = "";
$(document).ready(function() {
	//click save seeting form
	$(document).on("click",".save-setting-button",function(e) {
		e.preventDefault();
		var error = false;
		//check status name
		$(".txtNameStatus").each(function(index, val) {
			if($.trim($(val).val()) == ""){
				$(val).focus();
				$(val).parent(".form-inline").next().find(".error-kanban-statusname").removeClass("display-none");
				error = true;
				return false;
			}
		});
		//check swimline name
		$(".txtNameSwimline").each(function(index,val){
			if($.trim($(val).val()) == ""){
				$(val).focus();
				$(val).parent(".form-inline").next().find(".error-kanban-swimlinename").removeClass("display-none");
				error = true;
				return false;
			}
		});
		
		if(!error) {
			$(".save-setting").trigger("click");
		}
		
		
	});
	
	//validate immedately status name
	$(document).on("keyup",".txtNameStatus",function() {
		var parent = $(this).parents(".form-inline");
		var nextOfParent = $(parent).next();
		var errorElement = $(nextOfParent).find(".error-kanban-statusname");
		var errorOfPrimeface = $(nextOfParent).find(".ui-message-error:not(.error-kanban-statusname)");
		if($(this).val() == "") {
			$(errorElement).removeClass("display-none");
		} else {
			$(this).removeClass("ui-state-error");
			$(errorElement).addClass("display-none");
			if($(errorOfPrimeface).length > 0) {
				$(errorOfPrimeface).remove();
			}
		}
	});
	
	//validate immedately swimline name
	$(document).on("keyup",".txtNameSwimline",function() {
		var parent = $(this).parents(".form-inline");
		var nextOfParent = $(parent).next();
		var errorElement = $(nextOfParent).find(".error-kanban-swimlinename");
		var errorOfPrimeface = $(nextOfParent).find(".ui-message-error:not(.error-kanban-swimlinename)");
		if($(this).val() == "") {
			$(errorElement).removeClass("display-none");
		} else {
			$(this).removeClass("ui-state-error");
			if($(errorOfPrimeface).length > 0) {
				$(errorOfPrimeface).remove();
			}
			$(errorElement).addClass("display-none");
		}
	});
	
	//click save backlog
	$(document).on("click",".btnButtonSaveBackLog",function(){
		var description = $(".usDescriptionTxt").next().find(".note-editable").html();
		saveBackLog([{name:"descriptionBackLog",value:description}]);
		return false;
	});
	//click edit backlog
	$(document).on("click",".btnButtonEditBackLog",function(){
		var description = $(".usEditDescriptionTxt").next().find(".note-editable").html();
		editBackLog([{name:"descriptionBackLog",value:description}]);
		return false;
	});
	
	//click save task
	$(document).on("click",".btnSaveTask",function(){
		var formAddTask = $(this).parents(".add-task-form");
		var description = $(formAddTask).find(".descriptionAddTask").next().find(".note-editable").html();
		var note = $(formAddTask).find(".noteAddTask").next().find(".note-editable").html();
		var idKanbanBackLog = $(this).parents(".kanban-column-backlog").attr("id");
		console.log("debug: -------add task--------");
		console.log("description:" + description);
		console.log("note:" + note);
		console.log("id kanban column backlog:" + idKanbanBackLog);
		console.log("debug: -------end add task--------");
		saveTask([{name:"descriptionTask",value:description},{name:"noteTask",value:note},{name:"idKanbanBackLog",value:idKanbanBackLog}]);
		return false;
	});
	
	//click eidt task
	$(document).on("click",".btnEditTask",function(){
		var formEditTask = $(this).parents(".edit-task-form");
		var description = $.trim($(formEditTask).find(".descriptionEditTask").next().find(".note-editable").html());
		var note = $.trim($(formEditTask).find(".noteEditTask").next().find(".note-editable").html());
		var subject = $.trim($(formEditTask).find(".txtSubjectEditIssue").val());
		var idKanbanBackLog = $(this).parents(".kanban-column-backlog").attr("id");
		console.log("debug: -------edit task--------");
		console.log("description:" + description);
		console.log("note:" + note);
		console.log("id kanban column backlog:" + idKanbanBackLog);
		console.log("debug: -------end edit task--------");
		
		//check validate data
		if(subject == ""){
			//show error 
			$(formEditTask).find(".txtSubjectEditIssue").focus();
			$(formEditTask).find(".subject-require").show();
		}else{
			editTask([{name:"subjectTask",value:subject},{name:"descriptionTask",value:description},{name:"noteTask",value:note},{name:"idKanbanBackLog",value:idKanbanBackLog}]);
		}
		
		return false;
	});
	
	//click delete userstory
	$(document).on("click",".icon-delete-backlog",function(){
		var idKanbanBackLog = $(this).parents(".kanban-column-backlog").attr("id");
		console.log("debug id kanban backlog:" + idKanbanBackLog);
		idGlobalColumnKanbanBackLog = idKanbanBackLog;
	});
	
	//click delete task
	$(document).on("click",".icon-delete-task",function(){
		var idKanbanBackLog = $(this).parents(".kanban-column-backlog").attr("id");
		console.log("debug id kanban backlog:" + idKanbanBackLog);
		idGlobalColumnKanbanBackLog = idKanbanBackLog;
	});
	
	
	$(document).on("focusin",".txtSubjectEditIssue",function(){
		$(".subject-require").hide();
	});
	
	scrollUserStoryOfProject();
	
	
	//show full detail on backlog
	$(document).on("dblclick",".box-product-backlog",function(){
		if($(this).find(".content-product-backlog").hasClass("display-none")){
			$(this).find(".content-product-backlog").removeClass("display-none");
		}else{
			$(this).find(".content-product-backlog").addClass("display-none");
		}
	});
	//show full detail on issue
	$(document).on("dblclick",".box-issue",function(){
		if($(this).find(".box-issue-content").hasClass("display-none")){
			$(this).find(".box-issue-content").removeClass("display-none");
		}else{
			$(this).find(".box-issue-content").addClass("display-none");
		}
	});
	//close msg
	$(document).on("click",".info-msg",function(){
		$(this).remove();
	});
	//show form task in userstory
	$(document).on("click",".icon-add-task",function(){
		var boxBacklogParent = $(this).parents(".box-product-backlog");
		var formAddTask = $(boxBacklogParent).find(".frm-add-task");
		
		if($(formAddTask).hasClass("visibility-hidden")){
			$(".frm-add-task").addClass("visibility-hidden");
			$(".frm-add-task").addClass("height-0-overflow-hidden");
			$(".box-product-backlog").find(".icon-add-task").switchClass('icon-minus', 'icon-plus');
			
			$(formAddTask).find(".add-task").val("");
//			$(formAddTask).find('textarea').val("");
			$(formAddTask).find('iframe').contents().find('body').html("");
			
			$(formAddTask).removeClass("visibility-hidden");
			$(formAddTask).removeClass("height-0-overflow-hidden");
			$(boxBacklogParent).find(".icon-add-task").switchClass('icon-plus', 'icon-minus');
			//clear data
			
			//init editoreditor-kanban
			var editorDescription = $(boxBacklogParent).find(".descriptionAddTask");
			initEditor(editorDescription);
			var editorNote = $(boxBacklogParent).find(".noteAddTask");
			initEditor(editorNote);
			
			//focus on subject
			$(boxBacklogParent).find(".add-task").focus();
		}else{
			$(formAddTask).addClass("visibility-hidden");
			$(formAddTask).addClass("height-0-overflow-hidden");
			$(boxBacklogParent).find(".icon-add-task").switchClass('icon-minus', 'icon-plus');
		}
	});
	
	
	
	$(document).on("click",".task-cancel", function(){
		var task = $(this).parents(".frm-add-task");
		$(".box-product-backlog").find(".icon-add-task").switchClass('icon-minus', 'icon-plus');
		$(task).addClass("visibility-hidden");
		$(task).addClass("height-0-overflow-hidden");
	});
	
	$(document).on("click",".task-edit-cancel", function(){
		
		var frmEditTask = $(this).parents(".frm-edit-task");
		$(frmEditTask).addClass("visibility-hidden");
		$(frmEditTask).addClass("height-0-overflow-hidden");
		
		//orgial data form edit
		var subjectOrginal = $(frmEditTask).find(".data-orginal-subject").html();
		var descriptionOrginal = $(frmEditTask).find(".descriptionEditTask").html();
		var noteOrginal = $(frmEditTask).find(".noteEditTask").html();
		console.log("debug orginal form edit:");
		console.log("subject:" + subjectOrginal);
		console.log("description:" + descriptionOrginal);
		console.log("note:" + noteOrginal);
		//reset form eidt
		$(frmEditTask).find(".descriptionEditTask").next().find(".note-editable").html(descriptionOrginal);
		$(frmEditTask).find(".noteEditTask").next().find(".note-editable").html(noteOrginal);
		$(frmEditTask).find(".txtSubjectEditIssue").val(subjectOrginal);
	});
	
	$(document).on("click",".icon-edit-task",function(){
		var boxBacklogParent = $(this).parents(".box-product-backlog");
		var task = $(this).parents(".sub-task");
		var editTask = $(task).find(".frm-edit-task");
		if($(editTask).hasClass("visibility-hidden")){
			$(editTask).removeClass("visibility-hidden");
			$(editTask).removeClass("height-0-overflow-hidden");
			
			//init editoreditor-kanban
			var editorDescription = $(boxBacklogParent).find(".descriptionEditTask");
			initEditor(editorDescription);
			var editorNote = $(boxBacklogParent).find(".noteEditTask");
			initEditor(editorNote);
			//focus on subject edit task
			$(editTask).find(".txtSubjectEditIssue").focus();
		} else {
			$(editTask).addClass("visibility-hidden");
			$(editTask).addClass("height-0-overflow-hidden");
		}
	});
	
	$(".cancel-edit-userStory").click(function(){
		$("#panel-edit-backlog-form").clear();
		$("#panel-edit-backlog-form").addClass("visibility-hidden");
	});
	
	$(".icon-edit-backlog").click(function(){
		$("#panel-edit-backlog-form").removeClass("visibility-hidden");
	});
	
	$(document).on("keyup",".no-input-number-less-0-edit-us input",function(){
		if(isNaN($(this).val()) || parseInt($(this).val()) < 0){
			spinValue.value = 0;
			$(this).val(0);
		}
	});
});

function addHoverForBoxIssue(){
	$(".data-tooltip").each(function(index, val){
		var dataTooltip = $(val).html();
		$(val).next().attr("title",dataTooltip);
	});
}

function focusOnNewField() {
	$(".txtNameStatus").each(function(index,val){
		if($.trim($(val).val()) == ""){
			$(val).focus();
		}
	});
}

function focusOnNewSwimline() {
	$(".txtNameSwimline").each(function(index,val){
		if($.trim($(val).val()) == ""){
			$(val).focus();
		}
	});
}

function firstClickToSelectAllText() {
	$(".txtEstimateAndRemain input").one("click",function() {
		console.log("one click");
		var id = $(this).attr("id");
		document.getElementById(id).selectionStart = 0;
		document.getElementById(id).selectionEnd = $(this).val().length;
	});
}

function loadDataIntoEditFormTask(id){
	//hide all form with prefix equal frm-edit-task
	$(".frm-edit-task").addClass("visibility-hidden");
	$(".frm-edit-task").addClass("height-0-overflow-hidden");
	//show current form
	$(".parent-form-edit-task-"+id).removeClass("visibility-hidden");
	$(".parent-form-edit-task-"+id).removeClass("height-0-overflow-hidden");
	//forcus on input subject
	$(".clTaskEditNameTxt"+id).focus();
}

function showToolTip(){
	//$(".kanban-tooltip").tooltip();
}
function getColorFromJson(datacolor,id){
	for(var i = 0;i<datacolor.length;i++){
		if(datacolor[i].id == id){
			return datacolor[i].color;
		}
	}
}
function setColorProject(){
	var colorString = ["#F3B200","#77B900","#2572EB","#AD103C","#632F00","#B01E00","#C1004F","#7200AC","#4617B4","#006AC1","#008287","#199900","#00C13F","#FF981D","#FF2E12","#FF1D77","#AA40FF","#1FAEFF","#56C5FF","#00D8CC","#91D100","#E1B700","#FF76BC","#00A3A3","#FE7C22"];
	var colorArry = [];
	var optionProject = $("#liListProject li");
	$(optionProject).each(function(index,val){
		colorArry.push({
			id:$.trim($(val).text()),
			color:colorString[index]
		});
	});
	//lop project and set color
	$(".angle-color-project").each(function(index,val){
		//get id 
		var idProject = $(val).attr("data-id-project");
		$("div[data-id-project = '"+idProject+"']").css({"border-top-color":getColorFromJson(colorArry,idProject),"border-left-color":getColorFromJson(colorArry,idProject)});
	});
}
function scrollTopZero(){
	$(".td-scrollable").scrollTop(0);
}

function resetFormAdd(){
	$("#add-backlog-form\\:usNameTxt").val("");
	spinValue.value = 0;
	$("#add-backlog-form\\:usValueSpner_input").val("0");
	$(".usDescriptionTxt").next().find(".note-editable").html("");
	$("#add-backlog-form\\:usNameTxt").focus();
}
function initValueEditUs(){
	spinValue.value = 1;
	$("#edit-backlog-form\\:usEditValueSpner_input").val("");
}
function resetFormEditUs(){
	$("#edit-backlog-form\\:usEditNameTxt").val("");
	spinValue.value = 0;
	$("#edit-backlog-form\\:usEditValueSpner_input").val("");
	editorDescription.clear();
}

function resetFormAddTask(){
	$(".add-task").val("");
	editorAddDescription.clear();
	editorNote.clear();
	
}

function scrollTopZeroFocusAddForm(){
	$("#form-kanban").scrollTop(0);
	$("#add-backlog-form\\:usNameTxt").focus();
}

function hideFormAddBacklog(){
	$("#add-backlog-form").addClass("display-none");
	$(".td-content-backlog").removeClass("fix-padding-when-show-form");
	return false;
}

function toggleSettingForm() {
    $('#setting-form').toggle("slide", {
	direction : "right"
    }, 300);
    return false;
}

function hideSettingForm() {
    $('#setting-form').hide("slide", {
	direction : "right"
    }, 300);
}


function handleSaveRequestFormSetting(xhr, status, args) {
    if (args.save) {
    	hideSettingForm();
    }
}

function handleUpdateSaveTask(xhr, status, args) {
	console.log("debug id kanban column backlog:" + args.idUpdate);
	PrimeFaces.ab({
	    source : '',
	    update :args.idUpdate + " " + "script",
		    global : true
		});
}

function handlesaveDragAndDropBacklog(xhr, status, args) {
	console.log("debug id kanban column backlog:" + args.idColumnKanbanBackLog);
	PrimeFaces.ab({
	    source : '',
	    update :args.idColumnKanbanBackLog + " " + "script",
		    global : true
		});
}


function handleSaveDragAndDropTask(xhr, status, args) {
	console.log("begin handleSaveDragAndDropTask..");
	var idBacklogColumn = $.trim(args.idColumnKanbanBackLog);
	if(idBacklogColumn != ""){
		PrimeFaces.ab({
	    source : '',
	    update :idBacklogColumn + " " + "script",
		    global : true
		});
	}
	
}

function handleDeleteUserstory(xhr, status, args) {
	//global idColumnKanbanBackLog: 
	PrimeFaces.ab({
	    source : '',
	    update :"render-userstory-of-project script",
		    global : true
		});
}

function handleDeleteTask(xhr, status, args) {
	//global idColumnKanbanBackLog: 
	PrimeFaces.ab({
	    source : '',
	    update :idGlobalColumnKanbanBackLog + " " + "script",
		    global : true
		});
}

function handleUpdateEditTask(xhr, status, args) {
	console.log("debug id kanban column backlog:" + args.idUpdate);
	PrimeFaces.ab({
	    source : '',
	    update :args.idUpdate + " " + "script",
		    global : true
	});
}
function toggleShowAcceptedStatus(context) {
    if ($(context).find('.ui-icon').hasClass('icon-eye-close')) {
	$('.acceptedStatusText').removeAttr("disabled");
	showAcceptedStatus.check();
	$(context).find('.ui-icon').removeClass('icon-eye-close').addClass('icon-eye-open');
    } else {
	$('.acceptedStatusText').prop('disabled', true);
	showAcceptedStatus.uncheck();
	$(context).find('.ui-icon').removeClass('icon-eye-open').addClass('icon-eye-close');
    }
}

//drap and drop task in view
function dragDropTask(){
	var dragBacklogIntoKanban = false;
	var idColumnKanbanBackLog = "";
	var moveTaskToKanban = false;
	var idColumnSender = "";
	$(".kanban-column").sortable({
		connectWith: ".kanban-column",
		placeholder: "placeholder",
		containment: 'document',
		cancel: ".disableDragBL",
		helper: 'clone',
//		zIndex:10000,
		appendTo: "body",
		start : function(event, ui) {
			if($(ui.item).parent(".kanban-column").hasClass("kanban-column-backlog")){
				
				//check move task
				if($(ui.item).hasClass("background-yellow")){
					//move task to kanban
					moveTaskToKanban = true;
					idColumnKanbanBackLog = $(ui.item).parents(".kanban-column-backlog").attr("id");
				}else{
					//drag backlog into kanban
					dragBacklogIntoKanban = true;
					idColumnKanbanBackLog = $(ui.item).parents(".kanban-column-backlog").attr("id");
					console.log("id back log:"+idColumnKanbanBackLog);
				}
			}else{
				
				//move task in kanban
				dragBacklogIntoKanban = false;
				idColumnSender = $(ui.item).parents(".kanban-column").attr("id");
			}
		},
		receive : function(event, ui) {
			var idIssue = $(ui.item).find(".hidden-issue-id").val();
			var idBacklog = $(ui.item).find(".hidden-backlog-id").val();
			var dataWIP = $(ui.item).parents(".table-content-cell").attr("data-wip");
			if(dataWIP == ""){
				dataWIP = "0";
			}
			var idKanbanStatus = $(ui.item).parents(".table-content-cell").attr("data-id-status");
			var idKanbanSwimline = $(ui.item).parents(".table-content-cell").attr("data-id-swimline");
			var idContainer = $(ui.item).parents(".kanban-column").attr("id");
			console.log("debug id cell:" + idContainer);
			var isChildDone = "false";
			if($(ui.item).parent(".kanban-column").hasClass("column-done")){
				isChildDone = "true";
			}
			//allow drop
				//move taks
				if(!dragBacklogIntoKanban){
					//move into bl to delete issue
					if($(ui.item).parent(".kanban-column").hasClass("kanban-column-backlog")){
						deleteIssueDropInBL([
								 {name:"idKanbanIssue",value:idIssue}
						]);
					}else{
						saveDragAndDropTask([
								 {name:"idKanbanStatus",value:idKanbanStatus},
								 {name:"idKanbanIssue",value:idIssue},
								 {name:"idKanbanSwimline", value: idKanbanSwimline},
								 {name:"isChildDone",value:isChildDone},
								 {name:"idContainer",value:idContainer},
								 {name:"idColumnKanbanBackLog",value:idColumnKanbanBackLog},
								 {name:"dataWIP",value:dataWIP},
								 {name:"idColumnSender",value:idColumnSender}
								 
								 
						]);
					}
				}else{
					if(dragBacklogIntoKanban){
						console.log("debug move BL to Kanban:");
						console.log("id back log:"+idColumnKanbanBackLog);
						console.log("id container log:"+idContainer);
						//drag backlog into kanban
						saveDragAndDropBacklog([
								 {name:"idKanbanStatus",value:idKanbanStatus},
								 {name:"idBacklog",value:idBacklog},
								 {name: "idKanbanSwimline", value: idKanbanSwimline},
								 {name:"isChildDone",value:isChildDone},
								 {name:"idContainer",value:idContainer},
								 {name:"idColumnKanbanBackLog",value:idColumnKanbanBackLog},
								 {name:"dataWIP",value:dataWIP}
						]);
					}
				}
		}
	});
}

function dragDropTeamMember(){
	$(".box-issue").droppable({
		accept: ".items",
		drop : function(ev, ui) {
			var issueId = $(this).find(".hidden-issue-id").val();
			var memberId = $(ui.draggable).find(".hidden-member-id").val();
			var idBoxIssue = $(this).attr("id");
			console.log("debug box issue:" + idBoxIssue);
			passMemberAndIssueId([
			                      {name:"ISSUE_ID",value:issueId},
			                      {name:"MEMBER_ID",value:memberId},
			                      {name:"BOXISSUE_ID",value:idBoxIssue}
			]);
		}
	});
	
	$(".items").draggable({
		cursor:"pointer",
		helper: "clone",
        revert: "invalid"
	}).disableSelection();
}

function setScrollableForAssignMenu(selector) {
    $(selector.jqId).mCustomScrollbar("destroy");
    $(selector.jqId).mCustomScrollbar({
	scrollInertia : 700,
	autoDraggerLength : false,
	autoHideScrollbar : true,
	theme : "light-thin",
	advanced : {
	    updateOnContentResize : true
	}
    });
}

function setScrollablePointRemainHistory(selector) {
    $(selector.jqId).mCustomScrollbar("destroy");
    $(selector.jqId).mCustomScrollbar({
	scrollInertia : 700,
	autoDraggerLength : false,
	autoHideScrollbar : true,
	theme : "light-thin",
	advanced : {
	    updateOnContentResize : true
	}
    });
}

function updateFormKanbanWithOutError(varClass){
	var idBoxIssue = $(varClass).parents(".box-issue").attr("id");
	console.log("debug get box issue when update estimate point:" + idBoxIssue);
	if(!$(varClass).hasClass("ui-state-error")){
		PrimeFaces.ab({
	    source : '',
	    update : idBoxIssue + " " + "script",
		    global : true
		});
	}
}

function updateComponent(id, issueId, start) {
    id.replace('issue_sticker', 'avatar-wrapper');
    calculateProgressRemote([ {
	name : 'issueId',
	value : issueId
    }, {
	name : 'id',
	value : id
    }, {
	name : 'start',
	value : start
    } ]);
}

function handleListComplete(xhr, status, args) {
    var id = args.id;
    var prid = id.replace('issue_sticker', 'chartPanel');
    
    PrimeFaces.ab({
    	source : '',
    	update : prid,
    	global : false
        });
    
    if (args.start) {
		var idEstimate = id.replace('issue_sticker', 'ajaxInplacePointRemain');
		var idEstimate_reorder = id.replace('issue_sticker', 'reorder');
		PrimeFaces.ab({
		    source : '',
		    update : idEstimate + ' ' + idEstimate_reorder,
		    global : false
		});
    }
}

function unassignMemberIssue(memberId, issueId){
	// hide menu
	var func = "iss_member_wg_" + issueId + ".hide()";
	var idBoxIssue = $(".box-issue_"+issueId).attr("id");
	console.log("debug box issue unassign:" + idBoxIssue);
	eval(func);
	// pass parameters
	send_unassignMemberIssue([
       {
    	   name: 'MEMBER_ID',
    	   value: memberId   
       }, {
    	   name: 'ISSUE_ID',
    	   value: issueId
       }, {
    	   name: 'BOXISSUE_ID',
    	   value:idBoxIssue
       }
   ]);
}
function toggleShowAcceptedStatus(context) {
    if ($(context).find('.ui-icon').hasClass('icon-eye-close')) {
	$('.acceptedStatusText').removeAttr("disabled");
	showAcceptedStatus.check();
	$(context).find('.ui-icon').removeClass('icon-eye-close').addClass('icon-eye-open');
    } else {
	$('.acceptedStatusText').prop('disabled', true);
	showAcceptedStatus.uncheck();
	$(context).find('.ui-icon').removeClass('icon-eye-open').addClass('icon-eye-close');
}}
function changeBackground(){
	//remove background
	$(".table-content-cell").removeClass("warning-column");
	$(".kanban-weight-point").each(function(index,val){	
		var column = index + 1;
		//get all task in colun
		var weightPoint = parseInt($(val).attr("kanban-weightpoint"));
		console.log("debug weight point:" + weightPoint);
		if(weightPoint!= undefined && weightPoint!= 0){
			var totalIssue = parseInt($(".table-content-row .table-content-cell:nth-child("+ column +") .box-issue").length);
			console.log("debug total issue in column "+ column + ":" + totalIssue);
			if(totalIssue > weightPoint){
				console.log("debug column have weight point greater than maximun:"+index);
				$(".table-content-row .table-content-cell:nth-child("+ column +")").addClass('warning-column');
			}
		}
	});	
}

function initEditor(context){
	$(context).summernote({
		  height:80,
		  toolbar: [
            ['color', ['color']],
		    ['style', ['bold', 'italic', 'underline', 'clear']],
		    ['fontsize', ['fontsize']],
		    ['para', ['ol', 'paragraph']],
		    ['fullscreen', ['fullscreen']],
		    ['help', ['help']] 
		  ]
	});
	removeHtmlTagWhenPasteInSummerNote();
}

function scrollUserStoryOfProject(){
	//scroll load more 
	$(".td-scrollable").scroll(function(){
		 if($(this).scrollTop() + $(this).innerHeight() >= this.scrollHeight && $(".remain-user-of-project").length > 0) {
			 //call from remote command
			 loadMoreProductBacklog();
	      }
	});
}
function scrollKanban(){
	//scroll kanban vertical
	$(".block-kanban").scroll(function(){
		$(".table-header").css("top",$(this).scrollTop()+"px");
	});
}