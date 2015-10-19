$(document).ready(function(){
	
	$(document).on("click",".td-icon-expand",function(){
		var dataIdParent = $(this).parent().find(".task-id").text().trim();
		var trChilds = $("tr[data-parent-id='"+dataIdParent+"']");
		if(trChilds.hasClass("display-none")){
			trChilds.removeClass('display-none');
		}else{
			trChilds.addClass('display-none');
		}
	});
	
	$(document).on("click",".header-sort",function(){
		
		var dataField = $(this).attr("data-field");
		var valueSort = "none";
		if($(this).find(".ui-icon-triangle-1-n").length > 0){
			valueSort = "asc";
		}else if($(this).find(".ui-icon-triangle-1-s").length > 0){
			valueSort = "desc";
		}
		
		fieldSortIssues([{name:"dataField",value:dataField},{name:"valueSortField",value:valueSort}]);
		
	});
	
	//hide button showall
	$(window).scroll(function(){
		//BL: backlog; SBL: sprint back log
		var srollTopPosition = $(this).scrollTop() + $(window).height();
		var heightDocument = $(document).height();
		
		if(srollTopPosition ==  heightDocument && $(".remaining-issue-mark").length > 0){
			loadmore();
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

function subDevPoint(val){
	return val.slice(val.indexOf("D")+1,val.indexOf("T"));
}

function subTestPoint(val){
	return val.slice(val.indexOf("T")+1);
}

function showAddForm(id) {
	$('#add-issue-form\\:issue-subject').val('');
	jQuery("#add-issue-form\\:issue-description").find("iframe").contents().find('body').text('');
	jQuery("#add-issue-form\\:issue-note").find("iframe").contents().find('body').text('');
	$('.ui-panel-title').text("Add issue for SprintBacklog #"+id);
	$('#add-form').css({'visibility':'visible'});
}

function hideAddForm() {
	$('#add-form').css({'visibility':'hidden'});
}			
												
function handleSaveButtonAddForm(xhr, status, args){
	if(args.save==true) {
		$('#add-form').css({'visibility':'hidden'});
		setTimeout(function(){
			$('tr:regex(id,issue-list_node_.*)').css('background-color','#ECECEC');
			$('tr:regex(id,issue-list_node_.*_.*$)').css('background-color','white');
		},500);
		PrimeFaces.ab({source:"",update:'list-form'});
		PrimeFaces.ab({source:"",update:'add-issue-form'});
	}
	else if(args.validationFailed) 
	{  
		PrimeFaces.ab({source:"add-issue-form:save-button",update:"add-issue-form"});
    }  
}
function handleUpdateEstimatePoint(xhr, status, args){
	if(args.update==true) {
		PrimeFaces.ab({source:"",update:'list-form'});
	}
}
function updateEstimatePoint(){
	var $id = 'p_inplace_e' + window.ID_PLACEHOLDER;
	fnEstimate = $id + '.save();';
	eval(fnEstimate);
}

function handleChangeEstimePoint(issueId, context){
		var existHistory = $(context).closest('td').find('.existHistoryOfPointRemain').val();
		var oldEstimate = $(context).closest('td').find('.oldEstimate').val();
		var estimate = $(context).val();
		if (estimate == oldEstimate) {
			var $id = 'p_inplace_e' + issueId;
			fnEstimate = $id + '.cancel();';
			eval(fnEstimate);
		}
		if (existHistory=='true' && estimate != oldEstimate) {
			changeIdPlaceholder(issueId);
			confirmReEstimateDlg.show();
		}
		if (existHistory=='false' && estimate != oldEstimate){
			var $id = 'p_inplace_e' + issueId;
			fnEstimate = $id + '.save();';
			eval(fnEstimate);
		}
}

function prepareUpdateParentPoint(issueId,displayIssueId){				 
	window.issueId = issueId;
	window.nonFormatPoint = $('#'+issueId+'').attr('new-point');
	var format = $('#'+issueId+'').attr('point-format');
	var newPoint = nonFormatPoint;			
  	var oldPoint = $('#'+issueId+'').attr('old-point');
  	if(format=='2'){
  		if(!$.isNumeric(newPoint)){
  			newPoint = parseFloat(subDevPoint(newPoint))+parseFloat(subTestPoint(newPoint));			  			
  		}
  		oldPoint = parseFloat(subDevPoint(oldPoint))+parseFloat(subTestPoint(oldPoint));
  	}
	var string  = '---The estimate point of "#'+displayIssueId+'" will change from '+oldPoint+' to '+newPoint+'';
	$(".msgWithIdPlaceHolder").html(string);
	confirmChangeParentPointDlg.show();
}

function setIdParent(issueParentId){
	window.issueParentId = issueParentId;				
}			

function hideTable(){				
	if(window.issueParentId){	
		var tbody = $('#list-form\\:issue-list_data');					
		var parent = tbody.find('tr.'+window.issueParentId+'');
		var data_rk = parent.attr('data-rk');					
		var childList = tbody.find('tr[data-prk="'+data_rk+'"].child-issue').get();
		var devPoint = 0;
		var testPoint = 0;
		tbody.find('tr').hide();
		parent.show();
		for(var i in childList){
			var estimatePoint = $(childList[i]).find('td:last-child').prev().prev().text();
			if(i==0)
				$(childList[i]).find('td:last-child').attr('rowspan',childList.length);
			else
				$(childList[i]).find('td:last-child').remove();
			if($.isNumeric(estimatePoint)){
				devPoint = devPoint + parseFloat(estimatePoint);
			}else{
				devPoint = devPoint + parseFloat(subDevPoint(estimatePoint));
				testPoint = testPoint + parseFloat(subTestPoint(estimatePoint));
			}
			$(childList[i]).show();
		}
		$(childList[0]).find('td:last-child').text("D"+devPoint+"T"+testPoint);	
		var form = $('#list-form');
		form.append('<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"'+
				' style="margin-top: 10px; padding: 5px 10px;"><span class="ui-button-icon-left ui-icon ui-c icon-reply" '+
				'style="margin-right:5px;"></span>Back</button>');
	}
}