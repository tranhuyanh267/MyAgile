$(document).ready(function(){
	$(window).scroll(function(){
		//BL: backlog; SBL: sprint back log
		var srollTopPosition = $(this).scrollTop() + $(window).height();
		var heightDocument = $(document).height();
		
		if(srollTopPosition ==  heightDocument && $(".remain-data-remaining-task").length > 0){
			loadMoreRemainingTask();
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
		
		sortRemainingTask([{name:"dataField",value:dataField},{name:"valueSortField",value:valueSort}]);
		
	});
});


function fillZeroTextInProgressBar() {
	jQuery(document).ready(function( $ ) {
		$( ".ui-progressbar-label" ).each(function( index  ) {
			if($(this).html().trim().length == 0 || $(this).html() == "0%") {
				$(this).html("0%").show();
			}
		});
	});
}
$(document).on("click",".assignButton",function(e){
	e.stopPropagation();
	if($(this).find(".icon-check-empty").length > 0){
		//assign task
		var issueId = $(this).parent().find(".task-id").text().trim();
		console.log("assign task:"+issueId);
		selectedTask([{name:"issueIdSelected",value:issueId}]);
		$(this).find(".icon-check-empty").addClass("icon-check").removeClass("icon-check-empty");
	}else if($(this).find(".icon-check").length > 0){
		//un assign task
		var issueId = $(this).parent().find(".task-id").text().trim();
		console.log("un assign task:"+issueId);
		unSelectedTask([{name:"issueIdSelected",value:issueId}]);
		$(this).find(".icon-check").addClass("icon-check-empty").removeClass("icon-check");
	}
	
});

$(document).on("click",".td-icon-expand",function(){
	var dataIdParent = $(this).parent().find(".task-id").text().trim();
	if($(".parent-"+dataIdParent).hasClass("display-none")){
		$(".parent-"+dataIdParent).removeClass('display-none');
	}else{
		$(".parent-"+dataIdParent).addClass('display-none');
	}
});