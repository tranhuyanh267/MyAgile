var jcrop_api;
function UploadedProgress(){
	$('body').css('overflow', 'hidden');
	cropImage.show();											
	teamLogoBlock.hide();
	$("#upload-error").hide();
	initJcrop();
}

function cancelDrop(){
	jcrop_api.destroy();
	
}
function initJcrop() {
	jcrop_api = $.Jcrop('img[id *="cropt"]', {
		onChange : setCoords,
		onSelect : setCoords
	});
	jcrop_api.setSelect([ 150, 150, 0, 0 ]);
	jcrop_api.setOptions({
		allowSelect : true,
		allowMove : true,
		allowResize : true,
		aspectRatio : 1
	});
	
}
function setCoords(c) {
	jQuery('#x1').html(c.x);
	jQuery('#y1').html(c.y);
	jQuery('#x2').html(c.x2);
	jQuery('#y2').html(c.y2);
	jQuery('#w').html(c.w);
	jQuery('#h').html(c.h);
};
function affterDropImage(xhr, status, args){
	updateLogo([ {
		name : 'filename',
		value : name
	} ]);
}
function passParam() {
	var x1 = $('#x1').html();
	var x2 = $('#x2').html();
	var y1 = $('#y1').html();
	var y2 = $('#y2').html();
	var w = $('#w').html();
	var h = $('#h').html();
	var imageWidth = $('img[id*="cropt"]').width();
	var imageHeight = $('img[id*="cropt"]').height();
	cropImageParam([ {
		name : 'x1',
		value : x1
	}, {
		name : 'x2',
		value : x2
	}, {
		name : 'y1',
		value : y1
	}, {
		name : 'y2',
		value : y2
	}, {
		name : 'w',
		value : w
	}, {
		name : 'h',
		value : h
	}, {
		name : 'imageWidth',
		value : imageWidth
	}, {
		name : 'imageHeight',
		value : imageHeight
	} ]);
}