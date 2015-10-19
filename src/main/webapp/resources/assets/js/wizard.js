function navigate(indexOfColum){
	//Reset color for all tab 
	var numberOfTab=$(".list").children().size();				
	for(var i=0;i<numberOfTab;i++){
		var doneTab=$(".list").children().eq(i).children(0);
		doneTab.removeClass('current-tab');
	}			
	
	//Set color for done tab
	for(var i=0;i<indexOfColum;i++){
		var doneTab=$(".list").children().eq(i).children(0);
		doneTab.removeClass('current-tab');
		doneTab.addClass('done-tab');
	}	
	 //Set color for current tab
 	var currentTab=$(".list").children().eq(indexOfColum).children(0);
 	currentTab.addClass('current-tab');
 	currentTab.addClass('done-tab');
}



function showAddForm(xhr, status, args){
	PrimeFaces.ab({source: '', update: 'newSprint', process: 'listSprints'});
	PrimeFaces.ab({source: '', update: 'listTeams', process: 'listTeams'});
	return false;
}
//setTimeout(function(){dlg1.show();},500);


function setScrollableForTeamList(){	
	$(".teams-selector").mCustomScrollbar("destroy");
	$(".teams-selector").mCustomScrollbar({
		scrollInertia:700,
		autoDraggerLength:false,
		autoHideScrollbar:true,
		theme:"dark-thin",
		advanced:{
	        updateOnContentResize: true
	    }
	});
}


function initUpload(index) {
    var uploader = new plupload.Uploader({
	runtimes : 'html5, flash',
	browse_button : 'btnChangeLogoTeam' + index,
	max_file_size : '10mb',
	url : 'upload',
	filters : [ {
	    title : "Image files",
	    extensions : "jpg,png"
	} ],
	multi_selection : false,
	multiple_queues : false
    });

    if (uploader) {
	uploader.destroy();
    }
    uploader.bind("Error", function(up, err) {
	$("#upload-error" + index).show();
	$("#upload-error" + index).html(err.message);
    });

    uploader.bind("Init", function(up) {
	$("#upload-error" + index).hide();
    });

    uploader.init();

    uploader.bind("BeforeUpload", function(up, file) {
	up.settings.multipart_params = {
	    upload_type : 'team-logo'
	}
    });

    uploader.bind("FilesAdded", function() {
	var teamLogoBlock = "teamLogoBlock" + index + ".show()";
	eval(teamLogoBlock);
	uploader.start();
    });

    uploader.bind("FileUploaded", function(up, file, res) {
	updateLogo([ {
	    name : 'filename',
	    value : file.name,
	}, {
	    name : 'index',
	    value : index,

	} ]);
	var teamLogoBlock = "teamLogoBlock" + index + ".hide()";
	eval(teamLogoBlock);
	$("#upload-error" + index).hide();
	PrimeFaces.ab({
	    source : '',
	    update : 'repeatTeam:' + index + ':team-logo-img'
	});
    });

    uploader.bind("UploadProgress", function(up, file) {
	$(".upload-progress" + index).html(file.percent + "%");
    });
}
function init(){
	//Get number of createTeam
	var numberOfTeam = $('button:regex(id,btnChangeLogoTeam.*)').size();		
	for (var i=0; i < numberOfTeam; i++){
		initUpload(i);			
	}				
}

var uploaderProject = new plupload.Uploader({
	runtimes : 'html5, flash',
	browse_button : 'btnChangeLogoNew',
	max_file_size : '10mb',
	url : 'upload',
	filters : [ {
		title : "Image files",
		extensions : "jpg,png"
	} ],
	multi_selection : false,
	multiple_queues : false
});

function initUploaderProject() {
	if (uploaderProject) {
		uploaderProject.destroy();
	}
	uploaderProject.bind("Init", function() {
		$("#upload-add-alert").hide();
	});

	uploaderProject.init();

	uploaderProject.bind("Error", function(up, err) {
		$("#upload-add-alert-message").text(err.message);
		$("#upload-add-alert").show();
		uploaderProject.refresh();
	});

	uploaderProject.bind("BeforeUpload", function(up, file) {

		up.settings.multipart_params = {
			upload_type : 'project-logo'
		}

	});

	uploaderProject.bind("FilesAdded", function() {
		projectLogoBlockNew.show();
		uploaderProject.start();
	});

	uploaderProject.bind("FileUploaded", function(up, file, res) {
		updateLogoNew([ {
			name : 'filenameNew',
			value : file.name
		} ]);
		projectLogoBlockNew.hide();
		$("#upload-add-alert").hide();
	});

	uploaderProject.bind("UploadProgress", function(up, file) {
		$(".upload-progress-new").html(file.percent + "%");
	});
};

function setHightLightTeam(index){
	$('.team-list').children().eq(index).addClass("selectedTeam");
}