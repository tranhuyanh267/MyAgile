//*******************check activation account*********************************/
function checkActivation(){
	var active = $('#notice').attr('accesskey');
	var nullPass = $('.contentNotice').attr('accesskey');
	var accountExpired = $('.contentNotice').attr('title');
	var documentURL = document.URL;
	if (active == 'false' && documentURL.indexOf("active") === -1) {
		if(nullPass == 'true' && accountExpired == 'true'){
			$('.contentNotice').text("You need to confirm your email address and set a password before using system.");
		}else if(nullPass == 'false' && accountExpired == 'true' && documentURL.indexOf("profile") === -1){
			var ctx = $('#root').text();
			window.location.replace(ctx + "profile");
			$("#notice").remove();
//			$('.contentNotice').text("You need to confirm your email address before using system.");
		}else{
			$('.contentNotice').text("You need to set a password before using system.");
		}
		$('#notice').show();
	} else {
		$("#notice").remove();
	}

	$('#info, #editProfilelink, #signoutlink').mouseover(function() {
		$('#notice').hide();
	});
	$('#info, #editProfilelink, #signoutlink').mouseleave(function() {
		$('#notice').delay(5000).fadeIn();
	});
	$('.linkNotice').mouseover(function() {
		$('.contentNotice').css('background-color', '#0096d6');
		$(this).css('text-decoration', 'none');
	});
	$('.linkNotice').mouseleave(function() {
		$('.contentNotice').css('background-color', '#2C3E50');
	});
}
/******************************************************************************************/
