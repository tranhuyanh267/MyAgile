function ext() {
	function getMax(dt) {
		var data = dt[dt.length - 1];
		return data[0];
	}

	function getMin(dt) {
		var data = dt[0];
		return data[0];
	}

	function getTickIntervalForDate(dt) {
		var numberofdate=((dt[dt.length-1][0]-dt[0][0])/ (24 * 60 * 60 * 1000));
		var day = Math.ceil(numberofdate / 15);
		return day + " day";
	}
	
	function getTickIntervalForStory(dt) {
		var max=dt[0][1];
		
		for(var i=0;i<dt.length;i++)
		{
			if(dt[i][1]>max)
			{
				max=dt[i][1];
			}
		}
		var tick=parseInt(Math.ceil(max / 10));
		return tick;
	}
	
	function getNumberTicks(dt){
		return dt.length;
	}
	
	function checkEmptyChart(dt) {
		var flag=true;
		for(var i=0;i<dt.length;i++)
		{
			if(dt[i][1]!=null)
				flag=false;
		}
		return flag;
	}

	function tooltipContentEditor(str, seriesIndex, pointIndex, plot) {
		var date = jQuery.datepicker.formatDate('mm/dd/yy', new Date(
				plot.data[seriesIndex][pointIndex][0]));
		if (seriesIndex == 0) {
			return "<b><i>"+date+"</i></b>" + "<p>Bug: <b>" 
			+ plot.data[seriesIndex][pointIndex][1]+"</b></p>";
		}
		if (seriesIndex == 1) {
			return "<b><i>"+date+"</i></b>" + "<p>User stories: <b>"
					+ plot.data[seriesIndex][pointIndex][1]+"</b></p>";
		}
		if (seriesIndex == 2) {
			return "<b><i>"+date+"</i></b>" + "<p>Points: <b>"
					+ plot.data[seriesIndex][pointIndex][1]+"</b></p>";
		}
	}

	this.cfg.title = "Burndown Charts";

	this.cfg.seriesDefaults = {
		rendererOptions : {
			barWidth : 10
		}
	};

	this.cfg.highlighter = {
		show : true,
		sizeAdjust : 7.5,
		tooltipContentEditor : tooltipContentEditor
	};
	
	this.cfg.grid = {
            gridLineWidth: 1,
            gridLineColor: 'rgb(235,235,235)',
            drawGridlines: true,
            background: 'white', 
        };
	
	this.cfg.axes = {
		yaxis : {
			borderColor:"#4682B4",
			min : 0,
			tickRenderer : $.jqplot.CanvasAxisTickRenderer,
			tickOptions : {
				formatString : "%d",
				textColor : '#4682B4'
			},
			labelRenderer : $.jqplot.LinearAxisRenderer,
			label : "Points",
			labelRenderer : $.jqplot.CanvasAxisLabelRenderer,
			labelOptions : {
				textColor : "#4682B4"
			}
		},
		xaxis : {
			renderer : $.jqplot.DateAxisRenderer,
			max : getMax(this.cfg.data[1]),
			min : getMin(this.cfg.data[1]),
			rendererOptions : {
				tickRenderer : $.jqplot.CanvasAxisTickRenderer
			},
			tickOptions : {
				angle : -40,
				formatString : '%m/%d/%Y'
			},
//			numberTicks:getNumberTicks(this.cfg.data[1]),
			tickInterval : getTickIntervalForDate(this.cfg.data[1]),
			label : "Date"
		},
		y2axis : {
			borderColor:"#9ACD32",
			min : 0,
			tickInterval : 2,
//			tickInterval : getTickIntervalForStory(this.cfg.data[1]),
			color : "#9ACD32",
			tickRenderer : $.jqplot.CanvasAxisTickRenderer,
			tickOptions : {
				formatString : '%d',
				textColor : '#9ACD32'
			},
			label : "Stories/Bugs",
			color : "#9ACD32",
			labelRenderer : $.jqplot.CanvasAxisLabelRenderer,
			labelOptions : {
				textColor : "#9ACD32"
			}
		}
	};

	this.cfg.legend = {
		renderer : $.jqplot.EnhancedLegendRenderer,
		show : true,
		location : 'n',
		placement : "outsideGrid",
		rendererOptions : {
			numberRows : 1
		}
	};

	//set bugs
	var bugChart = this.cfg.series[0];
	bugChart.color = "red";
	bugChart.yaxis = 'y2axis';
	bugChart.renderer = $.jqplot.BarRenderer;
	//set stories
	var storieChart = this.cfg.series[1];
	storieChart.color = "#9ACD32";
	storieChart.yaxis = 'y2axis';
	//set poitns
	var poitnChart = this.cfg.series[2];
	poitnChart.color = "#4682B4";

	if(checkEmptyChart(this.cfg.data[1]))
	{
		this.cfg.axes.yaxis.min=null;
		this.cfg.axes.y2axis.min=null;
	}
}