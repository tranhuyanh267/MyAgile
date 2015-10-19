$(document).ready(function(){
	//focus on close me
	$(document).on("focus","#userStoryForm\\:usNameTxt",function(){
		$("#msg-save-inform").hide();
	});
	//click msg for close
	$(document).on("click","#msg-save-inform",function(){
		$(this).hide();
	});
	
	$(document).on("dblclick",".sticker",function(){
		$(this).find(".ui-panel-content").toggle();
	});
});

function firstClickToSelectAllText() {
	$(".txtEstimateAndRemain input").one("click",function() {
		console.log("one click");
		var id = $(this).attr("id");
		document.getElementById(id).selectionStart = 0;
		document.getElementById(id).selectionEnd = $(this).val().length;
	});
}

$(function(){
	$('#footer').remove();
//	document.addEventListener('touchmove', function(event) {
//	     event.preventDefault();
//	}, false);
});

var is_dragging = false;
var dragging_item = null;
var is_scrolling = false;
var scroll_direction = null;
var overlapListener = null;
var isInSwimLine = "FALSE";

function getScrollWidth(){
	var scrollDiv = document.createElement("div");
	scrollDiv.className = "scrollbar-measure";
	document.body.appendChild(scrollDiv);	
	window.scrollbarWidth = scrollDiv.offsetWidth - scrollDiv.clientWidth;
	document.body.removeChild(scrollDiv);
}

function receiveIssueInfo(data) {
    var jsonIssue = $.parseJSON(data);
    var userId = jsonIssue.user;
    var update = jsonIssue.updateAll;
    var issueId = jsonIssue.issueId;
    var currentUserId = $('#currentLogUserId').val();
    var sprintId = $('#sprintSelect_input').val();
    var sprintInKB = jsonIssue.sprintId;
    var remain = jsonIssue.remain;
    if (userId != currentUserId && sprintId == sprintInKB) {
		if (update == "true") {
		    if (issueId == -1) {
			updateStatusList();
			PrimeFaces.ab({
			    source : '',
			    update : 'kanban_wrapper',
			    global : false
			});
		    } else {
			updateAllIssue();
			PrimeFaces.ab({
			    source : '',
			    update : 'kanban_wrapper',
			    global : false
			});
		    }
		} else {
			var str = '$(p_inplace_' + issueId + '.display.selector).html()';
		    var previousPoint = eval(str);
	
		    updateAllIssue();
		    var status = eval('sticker_' + issueId);
		    status = status.cfg.collapsed;
		    PrimeFaces.ab({
			source : '',
			updateSelector : '@(.panel_' + issueId + ')',
			global : false,
			oncomplete : function(a, b, c) {
			    var stringExe = 'showTaskStickerDetails(sticker_' + issueId + ')';
			    eval(stringExe);
			    if (status == false) {
				$('.panel_' + issueId + ' > div').eq(1).css('display', 'block');
				eval('sticker_' + issueId).toggleState(false, "", "");
			    }
			}
		    });
	
		    $('.history_' + issueId + ' >div >div >ul').prepend('<li style="cursor: pointer;"><span style="text-decoration: line-through">' + previousPoint + '</span></li>');
		    $('html').click(function() {
			$('.history_' + issueId).removeClass('ui-overlay-visible');
			$('.history_' + issueId).css('visibility', 'hidden');
	
		    });
		}
    }
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

function sortable() {
	var prevColumn = '';
	var nextColumn = '';
	window.updateString = '';
	$(".task-group").sortable('destroy');
    $(".task-group").sortable({
	connectWith : ".task-group",
	revert : 200,
	placeholder : 'placeholder',
	items : '.sticker',
	opacity : 0.8,
	tolerance : 'pointer',
	start : function(event, ui) {
	    $(ui.item).width($('.task-group .sticker').width());
	    is_dragging = true;
	    dragging_item = ui.item;
	    $('.placeholder').css('height', ui.item.eq(0).css('height'));
	    prevColumn = $(ui.item).parent().attr('id');
	},
	update : function(event, ui) {
		nextColumn = $(ui.item).parent().attr('id');
		//sort task in column
		if(nextColumn == prevColumn) {
			var trParent = $(this).parents("tr");
		    var statusId = $(this).parent().attr("id");
		    var issueId = $(ui.item).find(".issue-hidden").val();
		    var displayIssueId = $(ui.item).find(".display-issueId-hidden").val();
		    //nextColumn = $(ui.item).parent().attr('id');
		    var id = $(ui.item).attr('id');
		    var idRemain = new String(id.replace('issue_sticker', 'ajaxInplacePointRemain_display'));
		    var selectRemain = idRemain.split(':').join('\\:');
		    var remain = $("#" + selectRemain).html();
	        var itemIndex = ui.item.parent().children().index(ui.item.parent());
	        if($(trParent).hasClass("tr-swimline")){
	        	isInSwimLine = "TRUE";
	        }else{
	        	isInSwimLine = "FALSE";
	        }
	        console.log("debug"," issue id: "+issueId+"\n status id: "+statusId+"\n remain: "+remain+"\n prevColumn: "+prevColumn+"\n next column "+nextColumn+"\n")
	        updateStatusForIssue(displayIssueId, issueId, statusId, remain, prevColumn, nextColumn);
		}
	},
	receive : function(event, ui) {
		var trParent = $(this).parents("tr");
	    var statusId = $(this).parent().attr("id");
	    var issueId = $(ui.item).find(".issue-hidden").val();
	    var displayIssueId = $(ui.item).find(".display-issueId-hidden").val();
	    nextColumn = $(ui.item).parent().attr('id');
	    var id = $(ui.item).attr('id');
	    var idRemain = new String(id.replace('issue_sticker', 'ajaxInplacePointRemain_display'));
	    var selectRemain = idRemain.split(':').join('\\:');
	    var remain = $("#" + selectRemain).html();
        var itemIndex = ui.item.parent().children().index(ui.item.parent());
        if($(trParent).hasClass("tr-swimline")){
        	isInSwimLine = "TRUE";
        }else{
        	isInSwimLine = "FALSE";
        }
        console.log("debug"," issue id: "+issueId+"\n status id: "+statusId+"\n remain: "+remain+"\n prevColumn: "+prevColumn+"\n next column "+nextColumn+"\n")
        updateStatusForIssue(displayIssueId, issueId, statusId, remain, prevColumn, nextColumn);
	},
	stop : function() {
//	    is_dragging = false;
//	    dragging_item = null;
//	    is_scrolling = false;
//	    scroll_direction = null;
//	    clearInterval(overlapListener);
//	    overlapListener = null;
	}
    });
    
   
    $(".task-group").on("mousemove touchmove", function(event) {
		if (is_dragging) {
		    var documentWidth = $(document).width();
		    var currentTopRightPositionOfDraggingItem = $(dragging_item).offset().left + $(dragging_item).width();
		    var currentTopLeftPositionOfDraggingItem = $(dragging_item).offset().left;
		    var totalTaskGroupWidth = $(".task-group").length * $(".task-group").width();
		    var ending_right = (documentWidth - ($(".board").offset().left + totalTaskGroupWidth));
		    overlapListener = window.setInterval(function() {
			if (is_scrolling) {
			    if (scroll_direction == "right") {
				$("#kanban").scrollLeft($("#kanban").scrollLeft() + 10);
			    } else if (scroll_direction == "left") {
				$("#kanban").scrollLeft($("#kanban").scrollLeft() - 10);
			    }
			}
		    }, 200);
		    if (currentTopRightPositionOfDraggingItem > documentWidth) {
			if (ending_right < 0) {
			    is_scrolling = true;
			    scroll_direction = "right";
			} else {
			    is_scrolling = false;
			    clearInterval(overlapListener);
			}
		    } else if (currentTopLeftPositionOfDraggingItem < 0) {
			is_scrolling = true;
			scroll_direction = "left";
	
		    } else {
			is_scrolling = false;
			clearInterval(overlapListener);
		    }
		}
    });
}

function updateStatusForIssue(displayIssueId, issueId, statusId, remain, sender, receiver) {
    window.issueId = issueId;
    window.statusId = statusId;
    window.sender = sender;
    window.receiver = receiver;
    if ((statusId == STATUS_DONE_ID) && (remain != "D0T0") && (remain != "0") && ($("#pastSprint").val() == true)) {
	changeIdPlaceholder(displayIssueId);
	var confirmMgs = $('#confirmDoneDialog').find('.msgWithIdPlaceHolder').html();
	if (jQuery.isNumeric(remain)) {
	    confirmMgs = confirmMgs.replace(' D0T0 ', ' 0 ');
	} else
	    confirmMgs = confirmMgs.replace(' 0 ', ' D0T0 ');
	$('#confirmDoneDialog').find('.msgWithIdPlaceHolder').html(confirmMgs);
	confirmDoneDlg.show();
    } else {
    	
    //get order issue in cell seperate by ;
    var stringIssueOrder = "";
    $("div[id='"+receiver+"']").find(".issue-hidden").each(function(index,val){
    	stringIssueOrder = stringIssueOrder + ";" + $(val).val();
    });
    
    stringIssueOrder = stringIssueOrder.substring(1, stringIssueOrder.length);
    //save string issue order and swimline to window object
    window.stringIssueOrder = stringIssueOrder;
    window.isInSwimLine = isInSwimLine;
    
	handleDrop([ {
	    name : 'issueId',
	    value : issueId
	}, {
	    name : 'statusId',
	    value : statusId
	}, {
	    name : 'senderId',
	    value : sender
	}, {
	    name : 'receiverId',
	    value : receiver
	},{
	    name : 'isSwimLine',
	    value : isInSwimLine
	},{
	    name : 'orderIssue',
	    value : stringIssueOrder
	}]);
    }
}

function assignTaskToMember(memberId, issueId){
	var func = "iss_member_wg_" + issueId + ".hide()";
	eval(func);
	var panel = $('.panel_' + issueId + '');//.attr('id');
	var columndId = $(panel).parents(".column").attr("id");
	
	rc_assignTaskToMember([
       {
    	   name: 'memberId',
    	   value: memberId   
       }, {
    	   name: 'issueId',
    	   value: issueId
       }, {
    	   name: 'columnId',
    	   value: columndId
       }
   ]);
}

function scrollForRelationshipGraph(issueId) {
    var taskPanelId = "#task-" + issueId;
    $("#org").jOrgChart({
	chartElement : '#chart'
    });

    setTimeout(function() {
	$(".orgChart").mCustomScrollbar("destroy");
	$(".orgChart").mCustomScrollbar({
	    scrollInertia : 1000,
	    autoDraggerLength : false,
	    autoHideScrollbar : false,
	    theme : 'dark-thick',
	    horizontalScroll : true
	});

	var pnTaskRelationshipScrollContainer = $(".content .mCSB_container");
	var chartWrapperWidth = $(".orgChart").width();

	if (pnTaskRelationshipScrollContainer.width() <= chartWrapperWidth) {
	    pnTaskRelationshipScrollContainer.width("100%");
	}

	var taskPanelPosition = $(".orgChart " + taskPanelId).position();
	var taskPanelWidth = $(".orgChart " + taskPanelId).width();
	if (taskPanelPosition) {
	    $(".orgChart").mCustomScrollbar("scrollTo", taskPanelPosition.left - (chartWrapperWidth / 2 - taskPanelWidth / 2));
	}
    }, 0);

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

function expandAndCollapseColum() {	
    $('.expand i').click(function() {
		var i = $(this);
		var th = $(this).parent();	
		var thead_fixed = th.closest('table').find('.status').eq(th[0].cellIndex);
		var thead_kanban = $('#boardKanban').find('.status').eq(th[0].cellIndex);		
		var td = $('#boardKanban').find('td').eq(th[0].cellIndex);
		if (!i.attr('expand') || i.attr('expand') == 'false') {
		    $(this).removeClass('icon-collapse-alt').addClass('icon-expand-alt');
		    i.attr('expand', 'true');
		    thead_fixed.addClass('column-collapsed');
		    thead_kanban.addClass('column-collapsed');
		    td.addClass('column-collapsed');
		    td.removeClass('task-group');
		} else if (i.attr('expand') == 'true') {
		    $(this).removeClass('icon-expand-alt').addClass('icon-collapse-alt');
		    i.attr('expand', 'false');
		    thead_fixed.removeClass('column-collapsed');
		    thead_kanban.removeClass('column-collapsed');
		    td.removeClass('column-collapsed');
		    td.addClass('task-group');
		}		
    });
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
    var idRemain = '#' + id.replace('issue_sticker', 'avatar-wrapper') + ' .task-progress';
    var prid = id.replace('issue_sticker', 'chartPanel');
    var $idRemain = $(idRemain.split(':').join('\\:'));
    $($idRemain).animate({
	width : args.progressValue + '%'
    });
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

function updateEstimatePoint() {
    var $id = 'p_inplace_e' + window.ID_PLACEHOLDER;
    fnEstimate = $id + '.save();';
    eval(fnEstimate);
    return false;
}


function setFixed() {
	var top = $('#boardKanban').offset().top;
    $('#kanban').on('scroll', function(e) {
    	e.preventDefault();	
    	$("#boardFixed").css("top",($(this).scrollTop()));
//    	$('#boardKanban').stop().animate().offset({top: top - $(this).scrollTop()});
//		$("#boardFixed").css({
//			transform: 'translateY('+ $(this).scrollTop()+'px))',
//			MozTransform: 'translateY('+ $(this).scrollTop()+'px)',
//			WebkitTransform: 'translateY('+ $(this).scrollTop()+'px)',
//			msTransform: 'translateY('+ $(this).scrollTop()+'px)'			
//		});    	
//		$('#test').text($(this).scrollTop());
    });
////    $('#kanban').on('touchstart', function(){
////    	$('#test').text($(this).scrollTop());
////    });
//    $('#kanban').on('touchmove', function(){
//    	$$("#boardFixed").css({
//			transform: 'translateY('+ $(this).scrollTop()+'px))',
//			MozTransform: 'translateY('+ $(this).scrollTop()+'px)',
//			WebkitTransform: 'translateY('+ $(this).scrollTop()+'px)',
//			msTransform: 'translateY('+ $(this).scrollTop()+'px)'			
//		});    	
//    });
}

function setBoardHeight() {
//    var kanban = $('#kanban');
//    var kanban_wrapper = $('#kanban_wrapper');
//    var board = $('#boardKanban');
//    var win = $(window);
//    if (!kanban_wrapper.length) {
//    	kanban.css('height', win.height() - kanban.offset().top - 30);
//    }
//	kanban_wrapper.css('height', win.height() - kanban.offset().top - scrollbarWidth);
//	board.css('height', win.height() - kanban.offset().top - scrollbarWidth);
    setFixed();
}

function createContentTooltip(issueId) {
    return "The estimate point of this task and total estimate points of its children are not equal. <br>" + "<div style='margin:5px 0'>- " + "<a href='#' class='ui-commandlink ui-widget'" + "onclick=\"prepareUpdateParentPoint(" + issueId + ");return false;\"" + "style='text-decoration: underline;color: #0058F3;'>Click here</a> to update estimate point for parent issue. <br></div>" + "<div style='margin:5px 0'>- <a href='#' class='ui-commandlink ui-widget'" + "onclick=\"redirectToMeetingPage([{name: 'issueId', value: '" + issueId + "'},{name: 'teamId', value: '" + $('#teamSelect_input').val() + "'}," + "{name: 'sprintId', value: '" + $('#sprintSelect_input').val() + "'}]);return false;\"" + "style='text-decoration: underline;color: #0058F3;'>Click here</a> to update estimate point for childrens issue</div>";
}

function initTooltip() {
    var listWarning = $('form[id="relationshipChart"]').find('.icon-warning-sign').get();
    var issueId;
    for ( var i in listWarning) {
	issueId = $(listWarning[i]).attr('id');

	$(listWarning[i]).qtip({
	    content : createContentTooltip(issueId),
	    position : {
		target : $(listWarning[i]),
		my : 'left center',
		at : 'center right'
	    },
	    hide : {
		fixed : true,
		delay : 300
	    }

	});
    }
}

function prepareUpdateParentPoint(issueId) {
    window.issueId = issueId;
    window.nonFormatPoint = $('#' + issueId + '').attr('new-point');
    var format = $('#' + issueId + '').attr('point-format');
    var newPoint = nonFormatPoint;
    var oldPoint = $('#' + issueId + '').attr('old-point');
    if (format == '2') {
	if (!$.isNumeric(newPoint)) {
	    newPoint = parseFloat(subDevPoint(newPoint)) + parseFloat(subTestPoint(newPoint));
	}
	oldPoint = parseFloat(subDevPoint(oldPoint)) + parseFloat(subTestPoint(oldPoint));
    }
    var string = 'The estimate point of issue "#' + issueId + '" will change from ' + oldPoint + ' to ' + newPoint + '';
    $(".msgWithIdPlaceHolder").html(string);
    confirmChangeParentPointDlg.show();
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

function formatMessageCopyIssueToSprintDlg() {
	var args = [].slice.call(arguments);
	var message = window.mgs['myagile.dashboard.CopyIssueToSprint'].toString();
	message = message.format.apply(message, args);
	window.issueId = args[0].slice(1, args[0].length);
	window.sprintId = args[2];
	$(".msgCopyIssueToSprint").html(message);
}

function showTaskStickerDetails(sticker) {
	//not work i realy don't know 
//    jQuery(document).ready(function($) {
//	var hammerStickerHeader = $(sticker.jqId + " .sticker-header").hammer();
//	$(sticker.jqId + " > .sticker-header").on("dblclick", function() {
//	    sticker.toggle();
//	});
//
//	hammerStickerHeader.on("doubletap", function() {
//	    sticker.toggle();
//	});
//
//	$("input[id$=':pointRemain'], input[id$=':estimatePoint']").on("tap", function() {
//	    $(this).focus();
//	});
//    });

}

function tooltipProjectDetail(selector, projectId, projectName, projectLogo, context){
	var ribbon = $('#' + selector + '');
	if(!projectLogo.length){
		projectLogo = "default";
	}
	var logoPath = "" + context + "/file/?type=project-logo&amp;filename=" + projectLogo + "&amp;pId=" + projectId + "";
	var imgTag = '<IMG class="project-logo img-rounded img-polaroid" alt=" + project" src="' + logoPath + '" />';
	ribbon.qtip({
        overwrite: false,
        content: '<div>' + imgTag + ' ' + projectName + '</div>',
        position: {
            my: 'left bottom',
            at: 'top right',            
            viewport: $('#kanban')
        }
	});
}

function updateColumnSendAndRevice(){
	var sendColumn = window.sender;
    var receiveColumn = window.receiver;
    PrimeFaces.ab({
	    source : '',
	    update : sendColumn + ' , ' + receiveColumn,
	    global : true,
	    onstart:function(){
	    	confirmDoneDlg.hide();
	    },
	    onsuccess:function(a){
	    	PrimeFaces.ab({
	    	    source : '',
	    	    update : 'scriptKanban',
	    	    global : false
	    	});
	    }
	});
}

function updateValueChangedAfterSaveUserStory(){
	var idFirstCell;
	//check swimline exist or not
	if($(".tr-swimline").length > 0){
		//update exacly cell (0,0) in kanban
		//find cell (0,0)
		var firstCell = $("#boardKanban tbody .normal-row-with-swimline .cantain-drop:first-child > .column");
		idFirstCell = firstCell.attr("id");
	}else{
		//find cell (0,0)
		var firstCell = $("#boardKanban tbody tr:first-child .cantain-drop:first-child > .column");
		idFirstCell = firstCell.attr("id");
	}
	
	//update first cell
	PrimeFaces.ab({
	    source : '',
	    update : idFirstCell + ' , scriptKanban',
	    global : true,
	    onsuccess:function(data){
	    	PrimeFaces.ab({
	    	    source : '',
	    	    update : 'scriptKanban',
	    	    global : false
	    	});
	    }
	});
}

function removeMoreOptionButtons() {
	var browserInfo = new UAParser().getResult();
	if(browserInfo.device.type != "tablet") {
		jQuery("a[class^='btn-more-options-']").remove();
		jQuery("a[class^='edit-issue-link']").removeAttr("style");
	}
}
