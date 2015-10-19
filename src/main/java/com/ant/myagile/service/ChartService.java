package com.ant.myagile.service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public interface ChartService {
	
	Hashtable<String, Float> processingBugBelongDate(Long sprintId);
	
	Map<String, ArrayList<Float>> processingIssueBelongDate(Long sprintId);
	
	Hashtable<String, Object> getDataChartBySprintNoJSON(Long sprintId);
	
}
