$(document).ready(function() {
	updateTableWidth();
	
	//scroll kanban vertical
	$("#form-kanban").scroll(function(){
		$(".tb-header-kanban").css("top",$(this).scrollTop()+"px");
	});
	
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
	changeBackground();
	
	$("body").scrollTop(0);
	$(window).resize(function(){
		$("body").scrollTop(0);
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
		var task = $(this).parents(".frm-edit-task");
		
		$(task).addClass("visibility-hidden");
		$(task).addClass("height-0-overflow-hidden");
	});
	
	$(".icon-edit-task").click(function(){
		var task = $(this).parents(".sub-task");
		var editTask = $(task).find(".frm-edit-task");
		if($(editTask).hasClass("visibility-hidden")){
			$(editTask).removeClass("visibility-hidden");
			$(editTask).removeClass("height-0-overflow-hidden");
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
	$(".kanban-tooltip").tooltip();
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
function fixRowInFirefox(){
	if(navigator.userAgent.toLowerCase().indexOf('firefox') > -1)
	{
		var heightBoxIssue = 51;
		var minTdHeight = 213;
	    //get row in table header content
		var rows = $(".tb-header-kanban-content tbody tr");
		$(rows).each(function(index,val){
			var tds = $(val).children("td");
			var maxTd = 0;
			
			$(tds).each(function(tdIndex,tdVal){
				//set height for each td
				//get all issue in column-progress-done
				if($(tdVal).find(".column-progress-done").length > 0){
					if(maxTd < $(tdVal).find(".box-issue").length){
						maxTd = $(tdVal).find(".box-issue").length;
					}
				}
				//get all issue in column-progress
				if($(tdVal).find(".column-progress").length > 0){
					if(maxTd < $(tdVal).find(".column-progress").find(".box-issue").length){
						maxTd = $(tdVal).find(".column-progress").find(".box-issue").length;
					}
				}
				//get all issue in column-done
				if($(tdVal).find(".column-done").length > 0){
					if(maxTd < $(tdVal).find(".column-done").find(".box-issue").length){
						maxTd = $(tdVal).find(".column-done").find(".box-issue").length;
					}
				}
			});
			//set height for td
			if(parseInt(maxTd) != 0){
				$(tds).each(function(tdIndex,tdVal){
					$(tdVal).height(parseInt(maxTd) * heightBoxIssue + 5);//5 margin-bottom
				});
			}else{
				$(tds).each(function(tdIndex,tdVal){
					$(tdVal).height(minTdHeight);
				});
			}
			
			
		});
		//alert($(".box-issue").outerHeight(true));
	}
}

function scrollTopZero(){
	$("#form-kanban").scrollTop(0);
	//update spinvalue
}

function resetFormAdd(){
	$("#add-backlog-form\\:usNameTxt").val("");
	spinValue.value = 0;
	$("#add-backlog-form\\:usValueSpner_input").val("");
	editorDescription.clear();
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

function updateTableWidth() {
	var tableWidth = 300;
	var lastWidth = 0;
	$('.widthSpinner input').each(function() {
		tableWidth += parseInt($(this).val(), 10) * 16;
		lastWidth = parseInt($(this).val(), 10) * 16;
	});
	if ($('.icon-eye-close').length != 0) {
		tableWidth -= lastWidth;
	}
	$('#kanban-table').width(tableWidth);
	$('.tb-header-kanban-content').width(tableWidth);
}

function handleSaveRequestFormSetting(xhr, status, args) {
    if (args.save) {
    	updateTableWidth();
    	hideSettingForm();
    }
}

function setWidthTable(){
	var countTd = $(".tb-header-kanban tr:first-child th").length;
	if(countTd > 3){
		var newWidth = parseInt(countTd)*400 - 100;
		//table header
		$(".tb-header-kanban").css("width",newWidth+"px");
		//table content
		$(".tb-header-kanban-content").css("width",newWidth+"px");
	}else{
		$(".td-content-backlog").attr('style', 'width: 302px !important');
	}
	
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
	var moveTaskToKanban = false;
	$(".kanban-column").sortable({
		connectWith: ".kanban-column",
		placeholder: "placeholder",
		containment: 'document',
		helper: 'clone',
//		zIndex:10000,
		appendTo: "body",
		start : function(event, ui) {
			if($(ui.item).parent(".kanban-column").hasClass("kanban-column-backlog")){
				
				//check move task
				if($(ui.item).hasClass("background-yellow")){
					//move task to kanban
					moveTaskToKanban = true;
				}else{
					//drag backlog into kanban
					dragBacklogIntoKanban = true;
				}
			}else{
				//move task in kanban
				dragBacklogIntoKanban = false;
			}
		},
		receive : function(event, ui) {
			var idIssue = $(ui.item).find(".hidden-issue-id").val();
			var idBacklog = $(ui.item).find(".hidden-backlog-id").val();
			
			var idKanbanStatus = $(ui.item).parents("td").attr("data-id-status");
			var idKanbanSwimline = $(ui.item).parents("td").attr("data-id-swimline");
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
								 {name: "idKanbanSwimline", value: idKanbanSwimline},
								 {name:"isChildDone",value:isChildDone}
						]);
					}
				}else{
					if(dragBacklogIntoKanban){
						//drag backlog into kanban
						saveDragAndDropBacklog([
								 {name:"idKanbanStatus",value:idKanbanStatus},
								 {name:"idBacklog",value:idBacklog},
								 {name: "idKanbanSwimline", value: idKanbanSwimline},
								 {name:"isChildDone",value:isChildDone}
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
			passMemberAndIssueId([{name:"ISSUE_ID",value:issueId},{name:"MEMBER_ID",value:memberId}]);
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
	if(!$(varClass).hasClass("ui-state-error")){
		PrimeFaces.ab({
	    source : '',
	    update : "form-kanban",
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
	eval(func);
	// pass parameters
	send_unassignMemberIssue([
       {
    	   name: 'MEMBER_ID',
    	   value: memberId   
       }, {
    	   name: 'ISSUE_ID',
    	   value: issueId
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
	$(".kanban-child-column").each(function(index,val){		
		var countTask = ($(val).children().children().children("div")).length;
		var weightPoint = $(val).attr("kanban-weightpoint");
		if(weightPoint!= undefined && weightPoint!= 0){
			if(countTask > weightPoint){
				$(val).addClass('warning-column');
			}
		}
	});	
}