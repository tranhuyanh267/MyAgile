function showProjectForm(){
	$('#fieldset-projectForm').slideDown(200);
//	PrimeFaces.ab({source:'',update: 'userStoryForm\:usDescTxt',global:'false'});
}

function hideProjectForm(){
	$('#fieldset-projectForm').slideUp(200,function(){
		PrimeFaces.ab({source:'',update: 'projectForm', global:'false'});
	});
}

$(document).ready(function(){
	
	$(document).on("click",".header-sort",function(){
		
		var dataField = $(this).attr("data-field");
		var valueSort = "none";
		if($(this).find(".ui-icon-triangle-1-n").length > 0){
			valueSort = "asc";
		}else if($(this).find(".ui-icon-triangle-1-s").length > 0){
			valueSort = "desc";
		}
		
		sortProject([{name:"dataField",value:dataField},{name:"valueSortField",value:valueSort}]);
		
	});
	
	//hide button showall
	$(window).scroll(function(){
		//BL: backlog; SBL: sprint back log
		var srollTopPosition = $(this).scrollTop() + $(window).height();
		var heightDocument = $(document).height();
		
		if(srollTopPosition ==  heightDocument && $(".remain-project-data").length > 0){
			loadMoreProject();
		}
	});
});

