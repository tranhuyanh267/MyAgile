function initData(data, statisticData) {

	var res = data.replace("[", "").replace("]", "").split(",");
	
	function createSpreadsheetData(rowCount, colCount) {
		var rows = [];
		for ( var i = 0 ; i < rowCount ; i++) 
		{
			rows.push(statisticData.data[i]);
		}
		return rows;
	}
	
	function getWidthTable() {
		var width=$(".wtHider").css("width");
		return width;
	}

	var myData = createSpreadsheetData(res.length, statisticData.data[0]);
	
	var title = [ "Sprint size<br/>[Weeks]", "Available Man-Days<br/>[Days]", "Team-size<br/>[Member]", 
	              "Planned stories<br/>[User stories]","Accepted stories<br/>[User stories]", 
	              "Planned points<br/>[Points]","Accepted point<br/>[Points]", 
	              "Planned velocity<br/>[P/P/D]","Accepted velocity<br/>[P/P/D]" ];

	$("#listStatisticTable").handsontable({
		data : myData,
		rowHeaders : true,
		colHeaders : true,
		colHeaders : function(col) {
			return title[col];
		},
		rowHeaders : function(row) {
			return res[row];
		},
		cells : function(r, c, prop) {
			var cellProperties = {};
			cellProperties.readOnly = true;
			return cellProperties;
		},
		height: function () {
		      var height=46+ myData.length*22;
		      if(height>210)
		    	  return 210;
		      return height;
	    },
	    variableRowHeights: false
	});
}

	function ext() {
		this.cfg.legend = {
				renderer : $.jqplot.EnhancedLegendRenderer,
				show : true,
				location : 'n',
				placement : "outsideGrid",
				rendererOptions : {
					numberRows : 1
				},
				marginTop : "-15px"
		};
		this.cfg.gridPadding={ top: 20} ;
		//set plan chart
		var seriesPlan = this.cfg.series[0];
		seriesPlan.color = "#EAA228";
		seriesPlan.renderer = $.jqplot.BarRenderer;
		//set delivered chart
		var seriesDelivered = this.cfg.series[1];
		seriesDelivered.color = "#006600";
		seriesDelivered.renderer = $.jqplot.BarRenderer;
		
		this.cfg.title  ={
	        show: false
	    };

		this.cfg.seriesDefaults = {
				rendererOptions : {
					barWidth : null
				},
				pointLabels: { 
					show: false, 
					formatString: '%.2f',
					hideZeros:true,
					ypadding : 0
				},
				shadow:true
			};
		
		function tooltipContentEditor(str, seriesIndex, pointIndex, plot) {
			return plot.data[seriesIndex][pointIndex];
		};

		this.cfg.highlighter = {
				tooltipLocation:"n",
				showMarker:false,
				show : true,
				tooltipContentEditor : tooltipContentEditor
			};
		
		function handleAngle(dt) {
			return -45;
		};
		
		this.cfg.axes.xaxis.tickOptions={
				angle : handleAngle(this.cfg.data[1])
		};
		
		function handleLabel(dt) {
			var label= dt.series[0].label.split(" ")[1];
			if(label=="point")
				return "Points";
			else if (label == "velocity")
				return "P/P/D";
			else
				return "Stories";
		};
		
		function handleformat(dt) {
			var label= dt.series[0].label.split(" ")[1];
			if(label=="point")
				return "%.0d";
			else if (label == "velocity")
				return "%.1f";
			else
				return "%.0d";
		};
		
		this.cfg.axes.yaxis={
				min : 0,
				tickRenderer : $.jqplot.CanvasAxisTickRenderer,
				tickOptions : {
					formatString : handleformat(this.cfg)
				},
				label : handleLabel(this.cfg),
				labelRenderer : $.jqplot.CanvasAxisLabelRenderer
		};
    }
	
	function updateDialogGenerateStatictis(){
		 PrimeFaces.ab({source: '', update: 'regenerateConfirm'});
	}
	
	function highlightCalendar(specialDays, date, cssClass) {
		var dateCalendar=$.datepicker.formatDate('yy-mm-dd', date);
		if(specialDays.indexOf(dateCalendar)!=-1)
		{
			return [ true, cssClass, '' ];
		}
		return [ true, '' ];
	}

	/**
	 * Binds the event beforeShowDay to every calendar found
	 */
	function bindEventsHighlights(specialDays) {
	    jQuery(".hasDatepicker").datepicker("option", "beforeShowDay", function (date) {
	        return highlightCalendar(specialDays, date, 'highlight-calendar');
	    });
	}