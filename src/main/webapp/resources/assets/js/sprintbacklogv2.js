$(document).ready(function(){
	//load more when scroll
	$(window).scroll(function(){
		//BL: backlog; SBL: sprint back log
		var srollTopPosition = $(this).scrollTop() + $(window).height();
		var heightDocument = $(document).height();
		if(srollTopPosition ==  heightDocument && $(".remainingDataProductionBacklogOrSprintBacklog").length > 0){
			loadmoreProductBacklogOrSprintBacklog();
		}
	});
});

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

function handleSuccess(xhr, status, args) {
    if (args.save == true) {
	PrimeFaces.ab({
	    source : '',
	    update : 'form2'
	});
	PrimeFaces.ab({
	    source : '',
	    update : 'title-form'
	});
    }
}

function resetFormAdd(){
	$("#form-add-bl\\:usNameTxt").val("");
	spinValue.value = 0;
	$("#form-add-bl\\:usValueSpner_input").val("0");
	spinRisk.value = 0;
	$("#form-add-bl\\:usRiskSpner_input").val("0");
	
	selPriority.selectValue("NONE");
	editNote.clear();
	editDescription.clear();
}

function expandAll(index) {
    var type = $('#toggle' + index).html();
    var typeString = "";

    if (type.indexOf("Expand All") >= 0) {
	typeString = "expand()";
	$('#toggle' + index).html(type.replace("Expand All", "Collapse All").replace("icon-resize-full", "icon-resize-small"));
    } else {
	typeString = "collapse()";
	$('#toggle' + index).html(type.replace("Collapse All", "Expand All").replace("icon-resize-small", "icon-resize-full"));
    }

    if (index == 1) {
	$('#sortable1 li.expand').each(function() {
	    var id = $(this)[0].id.replace(/:/g, '\\:').replace(/panel_/g, '');
	    var x = "widgetVar_" + id + "." + typeString;
	    eval(x);
	});	
    } else {
	$('#sortable2 li.expand').each(function() {
	    var idBase = $(this)[0].id.replace(/:/g, '\\:').replace(/panel_/g, '');
	    var idArr = idBase.split("-");
	    var id = idArr[0];
	    var x = "widgetVar_" + id + "." + typeString;
	    if (typeString == "expand()") {
		$(this).find("div").find("div[id*=header] span.ui-panel-title a").hide();
	    } else {
		if ($(this).find("div").find("div[id*=content] div.sticker-footer a").css("display") != "none") {
		    $(this).find("div").find("div[id*=header] div.sticker-header a").show();
		}
	    }
	    eval(x);
	});
    }
}

function createContentTooltip(issueId, displayIssueId) {
    return "The estimate point of this task and total estimate points of its children are not equal. <br>" + "<div style='margin:5px 0'>- " + "<a href='#' class='ui-commandlink ui-widget'" + "onclick=\"prepareUpdateParentPoint(" + issueId + "," + displayIssueId +");return false;\"" + "style='text-decoration: underline;color: #0058F3;'>Click here</a> to update estimate point for parent issue. <br></div>" + "<div style='margin:5px 0'>- <a href='#' class='ui-commandlink ui-widget'" + "onclick=\"redirectToMeetingPage([{name: 'issueId', value: '" + issueId + "'},{name: 'teamId', value: '" + $('#teamSelect_input').val() + "'}," + "{name: 'sprintId', value: '" + $('#sprintSelect_input').val() + "'}]);return false;\"" + "style='text-decoration: underline;color: #0058F3;'>Click here</a> to update estimate point for childrens issue</div>";
}

function initTooltip() {
    var listWarning = $('#sortable2').find('.icon-warning-sign').get();
    var issueId;
    var displayIssueId;
    for ( var i in listWarning) {
	issueId = $(listWarning[i]).attr('id');
	displayIssueId = $(listWarning[i]).attr('name');
	$(listWarning[i]).qtip({
	    content : createContentTooltip(issueId, displayIssueId),
	    position : {
		my : 'left center',
		at : 'center right'
	    },
	    hide : {
		fixed : true,
		delay : 300
	    }

	});
    }
    
    //init tool tip for history sprint
    $(".wokring-history-icon").each(function(){
    	var listSprintHhistoryLi = $(this).next();
    	$(this).qtip({
    		content : listSprintHhistoryLi.html(),
		    position : {
			my : 'left center',
			at : 'center right'
		    },
		    hide : {
		    	fixed : true,
		    	delay : 300
		    }
    	});
    });
}

function prepareUpdateParentPoint(issueId, displayIssueId) {
    window.issueId = issueId;
    window.nonFormatPoint = $('#' + issueId + '').attr('non-format');
    var newPoint = $('#' + issueId + '').attr('new-point');
    var oldPoint = $('#' + issueId + '').attr('old-point');
    var string = 'The estimate point of "#' + displayIssueId + '" will change from ' + oldPoint + ' to ' + newPoint + '';
    $(".msgWithIdPlaceHolder").html(string);
    confirmChangeParentPointDlg.show();
}

function handleDoubleClickPanel(panel) {
    panel.toggle();
    var id = panel.id.replace(/:/g, '\\:');
    var flgDisplay = $("#" + id + "_content div.sticker-footer a").css("display");
    if (flgDisplay != "none") {
	$("#" + id + "_header div.sticker-header a").toggle();
    }
}

function sortable() {
    initTooltip();
    initTooltipDrag();
    if ($('#status').val() == 'open') {
	initListIs();
    }
    setTimeout(function() {
	if ($('#no-sprint').size() == 0) {
	    if ($('#status').val() == 'open') {
		initListUs();
	    }
	}
    }, 100);
}

function initListUs() {
    $(".list-us").draggable({
	cursor : 'move',
	scroll : false,
	cancel :'.disableDrag',
	
	start:function(event, ui) {
		 var id = ui.helper[0].id;
		 /*var idTemp = id.substring(6,id.trim().length);
		 controlCancelUS(idTemp);*/
	},
	stop : function(event, ui) {
	    $('.tooltip_' + id).tooltipster('disable');// Disable tooltip
	    var id = ui.helper[0].id;
	    $('#' + id).zIndex(100);
	    var clientX = $('#' + id).offset().left;
	    if (clientX < ($('body').width() / 2 - window.SIZE_PANEL / 2)) {
		$('.tooltip_' + id).tooltipster('disable');
		$("#" + id).animate({
		    top : "0px",
		    left : "0px"
		});// return the old place;
	    } else {
		$('.tooltip_' + id).tooltipster('hide');
		var index = $("#sortPaginator1_content > li").index($('#' + id));
		var beforeTop = $(".list-us").eq(index - 1).position().top;
		var height = $(".list-us").eq(index - 1).height();
		var distantToMove = -(beforeTop + height - 60);
		if (index == 0) { 
		    distantToMove = +10;
		}
		$("#" + id).animate({
		    left : $('#colum2').position().left + 5,
		    top : distantToMove
		});
		moveUserStoryToSprintBacklog([ {
		    name : 'id',
		    value : id
		} ]);// Call function moveUserStoryToSprintBacklog
	    }
	},
	drag : function(event, ui) {
	    var id = ui.helper[0].id;
	    $('#' + id).zIndex(9999);
	    var clientX = $('#' + id).offset().left;
	    if (clientX < ($('body').width() / 2 - window.SIZE_PANEL / 2)) {
		$('.tooltip_' + id).tooltipster('hide');// Hide tooltip
		$('.placeholder').hide();// Hide placeholder
	    } else {
		$('.placeholder').show();// Show placeholder
		$('.placeholder').css('height', $('#' + id).height());
		$('.placeholder').css('width', $('#' + id).width() - 6);

		$('.tooltip_' + id).tooltipster('enable');// Enable tooltip
		$('.sticker-header').tooltipster('reposition');// Tooltip
		// follow cursor
		$('.tooltip_' + id).tooltipster('show');// Show tool
	    }
	}
    });
}

function deleteIssue(id, name) {
    window.ID_ISSUE = id.substring(6);
    $('.msgDeleteIssue').html('Would you like to remove Issue #' + name + " from Sprint Backlog?");
    deleteIssueDialog.show();
}
var idIssueAndUS;
function initListIs() {

    $(".list-is").draggable({
	cursor : 'move',
	scroll : false,
	stop : function(event, ui) {
		idIssueAndUS = ui.helper[0].id;
	    var arr = idIssueAndUS.split("-");
	    var idUS = arr[1];
	    $('.tooltip_' + idIssueAndUS).tooltipster('disable');
	    $('#' + idIssueAndUS).zIndex(9998);
	    var clientX = $('#' + idIssueAndUS).offset().left;
	    if (clientX > ($('body').width() / 2 - window.SIZE_PANEL / 2 - 30)) {
		$('.tooltip_' + idIssueAndUS).tooltipster('disable');
		$("#" + idIssueAndUS).animate({
		    top : "0px",
		    left : "0px"
		});
	    } else {
		$('.tooltip_' + idIssueAndUS).tooltipster('disable');
		$('.tooltip_' + idIssueAndUS).tooltipster('hide');
		deleteIssue(idIssueAndUS, idUS);
	    }
	},
	drag : function(event, ui) {
		idIssueAndUS = ui.helper[0].id;
	    $('#' + idIssueAndUS).zIndex(9999);
	    var clientX = $('#' + idIssueAndUS).offset().left;
	    if (clientX > ($('body').width() / 2 - window.SIZE_PANEL / 2 - 30)) {
		$('.tooltip_' + idIssueAndUS).tooltipster('hide');
	    } else {
		$('.tooltip_' + idIssueAndUS).tooltipster('enable');
		$('.sticker-header').tooltipster({
		    animation : 'grow'
		});
		$('.sticker-header').tooltipster('reposition');
		$('.tooltip_' + idIssueAndUS).tooltipster('show');
	    }
	}
    });
}

function initTooltipDrag() {
    $('.sticker-header').tooltipster();
    $('.sticker-header').tooltipster('disable');
    if ($('.placeholder').size() == 0) {
	$('#sortPaginator2_content').prepend('<div class="placeholder" style="display:none"></div>');
    }
}

function updateBacklog(xhr, status, args) {
    $("#panel_" + args.newId).effect("shake", 100);
    sortable();
}

$(function() {
    sortable();
    setSizePanel();
    $(window).resize(function() {
	setSizePanel();
    });
});

function setSizePanel() {
    try {
	window.SIZE_PANEL = $('.sticker-header').css('width').replace('px', '');
    } catch (e) {
    }
}

function showTaskStickerDetails(sticker) {
    jQuery(document).ready(function($) {
	var hammerStickerHeader = $(sticker.jqId + " .sticker-header").hammer();
	$(sticker.jqId + " > .sticker-header").on("dblclick", function() {
	    sticker.toggle();
	});
	hammerStickerHeader.on("dblclick", function() {
		if(!sticker.cfg.collapsed){
			controlCancelUS(sticker.jqId);
		}
	    sticker.toggle();
	});
    });
}


function controlEditUS(id,value){
	$('.list-us').draggable('disable');
	$('.InputDes_'+id).summernote({
		  height:150,
		  toolbar: [
		    ['style', ['bold', 'italic', 'underline', 'clear']],
		    ['fontsize', ['fontsize']],
		    ['color', ['color']],
		    ['para', ['ol', 'paragraph']],
		    ['fullscreen', ['fullscreen']],
		    ['help', ['help']] 
		  ]
	});
	var description =$('.InputDes_'+id).text();
	$('.InputDes_'+id).code(description);
	$('.EditBtn_'+id).hide();
	$('.SaveBtn_'+id).show();
	$('.CancelBtn_'+id).show();
	$('.PanelEdit_'+id).show();
	$('.InputValue_'+id).show();
	$('.OutputDes_'+id).hide();	
	$('.OutputValue_'+id).hide();
	removeHtmlTagWhenPasteInSummerNote();
}
function controlCancelUS(id){
	$('.EditBtn_'+id).show();
	$('.SaveBtn_'+id).hide();
	$('.CancelBtn_'+id).hide();
	$('.PanelEdit_'+id).hide();	
	$('.OutputDes_'+id).show();	
	$('.OutputValue_'+id).show();
	$('.InputValue_'+id).hide();
	$('.msg_'+id).hide();
	$('.list-us').draggable('enable');
	$('[id^=msn]').hide();
	$('[id^=ms2n]').hide();
	$('[id^=ms3n]').hide();
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
			case 4:
			case 5: {
				$('#ms3n'+id).show(); break;
			}
		}		
		if ($('#toggle1 > span:nth-child(2)').text().trim() == 'Collapse All') {
			expandAll(1);
		}
	}
}

function handleVoidDestroyVoid(xhr, status, args) {
	var form2 = $(xhr).find('#form2');
	var collapsed = $('#sortPaginator1_content').find($("div[style='display:none']"));
	var isExpandAll = $('#toggle1 > span:nth-child(2)');
	if ((form2 != null) && (collapsed.length == 0) && (isExpandAll.text().trim() == 'Expand All')) {
		$('#toggle1 > span:nth-child(2)').text('Collapse All');
		expandAll(1);
	}
}
