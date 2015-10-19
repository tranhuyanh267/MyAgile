PrimeFaces.widget.DataTable.prototype.bindCheckboxEvents = function() {
    var c = this.jqId + " table thead th.ui-selection-column .ui-chkbox.ui-chkbox-all .ui-chkbox-box", a = this;
    this.checkAllToggler = $(c);
    this.checkAllToggler.on("mouseover", function() {
	var e = $(this);
	if (!e.hasClass("ui-state-disabled") && !e.hasClass("ui-state-active")) {
	    e.addClass("ui-state-hover")
	}
    }).on("mouseout", function() {
	$(this).removeClass("ui-state-hover")
    }).on("click", function() {
	$('<style></style>').appendTo($(document.body)).remove();
	a.toggleCheckAll()
    });
    var b = this.jqId + " tbody.ui-datatable-data > tr.ui-widget-content:not(.ui-datatable-empty-message) > td.ui-selection-column .ui-chkbox .ui-chkbox-box", d = this.jqId + " tbody.ui-datatable-data > tr.ui-widget-content:not(.ui-datatable-empty-message) > td.ui-selection-column .ui-chkbox input";
    $(document).off("mouseover.ui-chkbox mouseover.ui-chkbox click.ui-chkbox", b).on("mouseover.ui-chkbox", b, null, function() {
	var e = $(this);
	if (!e.hasClass("ui-state-disabled") && !e.hasClass("ui-state-active")) {
	    e.addClass("ui-state-hover")
	}
    }).on("mouseout.ui-chkbox", b, null, function() {
	$(this).removeClass("ui-state-hover")
    }).on("click.ui-chkbox", b, null, function() {
	var f = $(this);
	if (!f.hasClass("ui-state-disabled")) {
	    var e = f.hasClass("ui-state-active");
	    if (e) {
		a.unselectRowWithCheckbox(f)
	    } else {
		a.selectRowWithCheckbox(f)
	    }
	}
    });
    $(document).off("focus.ui-chkbox blur.ui-chkbox keydown.ui-chkbox keyup.ui-chkbox", d).on("focus.ui-chkbox", d, null, function() {
	var e = $(this), f = e.parent().next();
	if (e.prop("checked")) {
	    f.removeClass("ui-state-active")
	}
	f.addClass("ui-state-focus")
    }).on("blur.ui-chkbox", d, null, function() {
	var e = $(this), f = e.parent().next();
	if (e.prop("checked")) {
	    f.addClass("ui-state-active")
	}
	f.removeClass("ui-state-focus")
    }).on("keydown.ui-chkbox", d, null, function(g) {
	var f = $.ui.keyCode;
	if (g.which == f.SPACE) {
	    g.preventDefault()
	}
    }).on("keyup.ui-chkbox", d, null, function(i) {
	var h = $.ui.keyCode;
	if (i.which == h.SPACE) {
	    var f = $(this), g = f.parent().next();
	    if (f.prop("checked")) {
		a.unselectRowWithCheckbox(g)
	    } else {
		a.selectRowWithCheckbox(g)
	    }
	    i.preventDefault()
	}
    })
}