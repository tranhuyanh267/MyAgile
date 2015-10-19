String.prototype.format = function() {
    var args = arguments;

    return this.replace(/\{(\d+)\}/g, function() {
        return args[arguments[1]];
    });
};

function getPrefix() {
  var styles = window.getComputedStyle(document.documentElement, ''),
    pre = (Array.prototype.slice
      .call(styles)
      .join('') 
      .match(/-(moz|webkit|ms)-/) || (styles.OLink === '' && ['', 'o'])
    )[1],
    dom = ('WebKit|Moz|MS|O').match(new RegExp('(' + pre + ')', 'i'))[1];
  return {
    dom: dom,
    lowercase: pre,
    css: '-' + pre + '-',
    js: pre[0].toUpperCase() + pre.substr(1)
  };
}

function setTextAllInPaginationSelect(){
	var pagination_selectbox = $('.ui-paginator-rpp-options');
	if(pagination_selectbox.length){
		pagination_selectbox.find('option:last-child').text('All');
	}	
}

function addElementUserProjectLink(idTable){
	var html = $(".wrap-admin-link-project").html();
	$(idTable+" .form-inline").append(html);
}

function removeTextInPagination(){
	var pagination_selectbox = $('.ui-paginator-rpp-options');
	if(pagination_selectbox.length){
		$('.ui-paginator').find('.ui-icon').text('');
	}	
}

function changeIdPlaceholder(id) {
	window.ID_PLACEHOLDER = id;
	$(".msgWithIdPlaceHolder").each(function() {
		var msg = $(this).html();
		msg = msg.replace("ID_PLACEHOLDER", id);
		$(this).html(msg);
	});
}
function revertIdPlaceholder() {
	$(".msgWithIdPlaceHolder").each(function() {
		var msg = $(this).text();
		$(this).text(msg.replace(window.ID_PLACEHOLDER, "ID_PLACEHOLDER"));
	});
}

$.expr[":"].Contains = $.expr.createPseudo(function (arg) {
    return function (elem) {
        return $(elem).text().toUpperCase().indexOf(arg.toUpperCase()) >= 0;
    };
});
//Author:PhucHieu
//Purpose: select id follow regrex
jQuery.expr[':'].regex = function(elem, index, match) {
		var matchParams = match[3].split(','),
    validLabels = /^(data|css):/,
    attr = {
        method: matchParams[0].match(validLabels) ? 
                    matchParams[0].split(':')[0] : 'attr',
        property: matchParams.shift().replace(validLabels,'')
    },
    regexFlags = 'ig',
    regex = new RegExp(matchParams.join('').replace(/^\s+|\s+$/g,''), regexFlags);
	return regex.test(jQuery(elem)[attr.method](attr.property));
};
// Get prefix of each browser for CSS3
function getVendorPrefix(){
	var regex = /^(Moz|webkit|Khtml|O|ms|Icab)(?=[A-Z])/;
	var someScript = document.getElementsByTagName('script')[0];
	for(var prop in someScript.style)
	{
		if(regex.test(prop))
		{
			return prop.match(regex)[0];
		}
	}
	if('WebkitOpacity' in someScript.style) return 'webkit';
	if('KhtmlOpacity' in someScript.style) return 'Khtml';
	return '';
}	



;(function($, window, undefined) {
    // outside the scope of the jQuery plugin to
    // keep track of all dropdowns
    var $allDropdowns = $();

    // if instantlyCloseOthers is true, then it will instantly
    // shut other nav items when a new one is hovered over
    $.fn.dropdownHover = function(options) {

        // the element we really care about
        // is the dropdown-toggle's parent
        $allDropdowns = $allDropdowns.add(this.parent());

        return this.each(function() {
            var $this = $(this),
                $parent = $this.parent(),
                defaults = {
                    delay: 500,
                    instantlyCloseOthers: true
                },
                data = {
                    delay: $(this).data('delay'),
                    instantlyCloseOthers: $(this).data('close-others')
                },
                settings = $.extend(true, {}, defaults, options, data),
                timeout;

            $parent.hover(function(event) {
                // so a neighbor can't open the dropdown
                if(!$parent.hasClass('open') && !$this.is(event.target)) {
                    return true;
                }

                if(settings.instantlyCloseOthers === true)
                    $allDropdowns.removeClass('open');

                window.clearTimeout(timeout);
                $parent.addClass('open');
            }, function() {
                timeout = window.setTimeout(function() {
                    $parent.removeClass('open');
                }, settings.delay);
            });

            // this helps with button groups!
            $this.hover(function() {
                if(settings.instantlyCloseOthers === true)
                    $allDropdowns.removeClass('open');

                window.clearTimeout(timeout);
                $parent.addClass('open');
            });

            // handle submenus
            $parent.find('.dropdown-submenu').each(function(){
                var $this = $(this);
                var subTimeout;
                $this.hover(function() {
                    window.clearTimeout(subTimeout);
                    $this.children('.dropdown-menu').show();
                    // always close submenu siblings instantly
                    $this.siblings().children('.dropdown-menu').hide();
                }, function() {
                    var $submenu = $this.children('.dropdown-menu');
                    subTimeout = window.setTimeout(function() {
                        $submenu.hide();
                    }, settings.delay);
                });
            });
        });
    };

    $(document).ready(function() {
        // apply dropdownHover to all elements with the data-hover="dropdown" attribute
        $('[data-hover="dropdown"]').dropdownHover();
    });
})(jQuery, this);
