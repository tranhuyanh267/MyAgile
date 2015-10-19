package com.ant.myagile.managedbean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.jfree.util.Log;
import org.primefaces.context.RequestContext;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.service.ChartService;

@Component("chartBean")
@Scope("session")
public class ChartBean {
    @Autowired
    private ChartService chartSerivce;
    private CartesianChartModel combinedModel;
    private long sprintId;

    @PostConstruct
    public void init() {
	combinedModel = new CartesianChartModel();
    }

    public long getSprintId() {
	return sprintId;
    }

    public void setSprintId(long sprintId) {
	this.sprintId = sprintId;
	if (sprintId != 0) {
	    this.createCombinedModel(sprintId);
	} else {
	    RequestContext context = RequestContext.getCurrentInstance();
	    context.execute("$('#bar').css('display','none')");
	    context.update("bar");
	}
    }

    public boolean isDisplay() {
	return this.sprintId != 0;
    }

    public CartesianChartModel getCombinedModel() {
	return combinedModel;
    }

    /**
     * Create a CartesianChartModel to draw burn down chart by collect data
     * about product backlog, issue list and bug list of sprint
     * 
     * @param sprintId
     *            - ID of Sprint, Long number format
     */
    @SuppressWarnings("unchecked")
    public void createCombinedModel(long sprintId) {
	try {
	    Hashtable<String, Object> data = chartSerivce.getDataChartBySprintNoJSON(sprintId);

	    Set<String> arrDate = (Set<String>) data.get("dList");
	    String[] arrDateStr = arrDate.toArray(new String[0]);

	    List<Float> pList = (List<Float>) data.get("pList");
	    List<Float> iList = (List<Float>) data.get("iList");
	    List<Float> bList = (List<Float>) data.get("bList");

	    combinedModel = new CartesianChartModel();
	    LineChartSeries bugs = new LineChartSeries();
	    bugs.setLabel("Bugs");
	    LineChartSeries stories = new LineChartSeries();
	    stories.setLabel("Stories Remaining");
	    LineChartSeries points = new LineChartSeries();
	    points.setLabel("Point Remaining");

	    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    for (int i = 0; i < arrDateStr.length; i++) {
		bugs.set(formatter.parse(arrDateStr[i]).getTime(), bList.get(i));
		stories.set(formatter.parse(arrDateStr[i]).getTime(), iList.get(i));
		points.set(formatter.parse(arrDateStr[i]).getTime(), pList.get(i));
	    }

	    combinedModel.addSeries(bugs);
	    combinedModel.addSeries(stories);
	    combinedModel.addSeries(points);

	    RequestContext context = RequestContext.getCurrentInstance();
	    context.update("bar");
	} catch (Exception e) {
	    Log.error(e.getMessage());
	}
    }

    public TimeZone getTimeZone() {
	TimeZone timeZone = null;
	timeZone = TimeZone.getDefault();
	return timeZone;
    }
}
