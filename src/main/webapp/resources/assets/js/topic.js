$(document).ready(function() {	
	$(document).on("click","#topic-detail-content a",function(e){
		var idTopic = $(this).attr("data-id-topic"),
			titleTopic = $(this).text(); 
		if (idTopic !== undefined){
			sendData([{name:'idTopic',value:idTopic}, {name: "titleTopic", value: titleTopic}]);
		}
		e.preventDefault();
	});
});
