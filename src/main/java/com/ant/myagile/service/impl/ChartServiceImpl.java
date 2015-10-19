package com.ant.myagile.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.converter.ClientTimeOffsetConverter;
import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.PointRemainDao;
import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.service.ChartService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.utils.Utils;

@Service("chartService")
@Transactional
public class ChartServiceImpl implements ChartService {
    @Autowired
    IssueDao issueDao;
    @Autowired
    SprintDao sprintDao;
    @Autowired
    PointRemainDao pointRemainDao;
    @Autowired
    PointRemainService pointRemainService;

    private Hashtable<String, Float> findBugsBelongDate(Hashtable<String, Float> bugHash, float rPoint, String date) {
	if (rPoint != 0) {
	    if (bugHash.containsKey(date)) {
		Float numOfBug = new Float(bugHash.get(date));
		bugHash.put(date, numOfBug + 1);
	    } else {
		bugHash.put(date, (float) 1);
	    }
	}
	return bugHash;
    }

    private TreeMap<String, ArrayList<Float>> findPointUpdateBelongDate(TreeMap<String, ArrayList<Float>> issueHash, float rPoint, String date) {
	ArrayList<Float> arr = new ArrayList<Float>();
	if (issueHash.containsKey(date)) {
	    arr = issueHash.get(date);
	}
	arr.add(rPoint);
	issueHash.put(date, arr);
	return issueHash;
    }

    private TreeMap<String, ArrayList<Float>> addStartAndEndDateIfNoExists(TreeMap<String, ArrayList<Float>> issueHash, String dateStart, String dateEnd) {
	if (!issueHash.containsKey(dateEnd)) {
	    issueHash.put(dateEnd, null);
	}

	if (!issueHash.containsKey(dateStart)) {
	    issueHash.put(dateStart, null);
	}
	return issueHash;
    }

    @Override
    public Hashtable<String, Float> processingBugBelongDate(Long sprintId) {
	Hashtable<String, Float> bugTable = new Hashtable<String, Float>();
	// Get all bugs
	List<Issue> bugs = issueDao.findIssuesByTypeAndSprint(sprintId, "Bug");
	for (Issue bug : bugs) {
	    List<PointRemain> pointRemains = pointRemainDao.findPointRemainByIssueIdAndSprint(bug.getIssueId(), sprintDao.findSprintById(sprintId));
	    for (PointRemain pointRemain : pointRemains) {
		float rPoint = Utils.convertPoint(pointRemain.getPointRemain());
		String date = Utils.dateToString(pointRemain.getDateUpdate());
		findBugsBelongDate(bugTable, rPoint, date);
	    }
	}
	return bugTable;
    }

    @Override
    public Map<String, ArrayList<Float>> processingIssueBelongDate(Long sprintId) {
	Sprint sprint = sprintDao.findSprintById(sprintId);
	TreeMap<String, ArrayList<Float>> ht = new TreeMap<String, ArrayList<Float>>();
	List<Issue> issues = issueDao.findIssuesByTypeAndSprint(sprintId, "Task");
	createPointRemainToCorrectBurdownChart(sprintId);

	for (Issue issue : issues) {
	    List<PointRemain> pointRemains = pointRemainDao.findPointRemainByIssueIdAndSprint(issue.getIssueId(), sprintDao.findSprintById(sprintId));

	    for (PointRemain pointRemain : pointRemains) {
		float rPoint = Utils.convertPoint(pointRemain.getPointRemain());
		Date dateInClientLocale = ClientTimeOffsetConverter.convertToClientDate(pointRemain.getDateUpdate());
		//convert Date before format
		String date = Utils.dateToString(dateInClientLocale);
		findPointUpdateBelongDate(ht, rPoint, date);
	    }
	}
	addStartAndEndDateIfNoExists(ht, sprint.getDateStart().toString(), sprint.getDateEnd().toString());

	return ht;
    }
    
    /**
     * This function will re-caculate the point remain of all task in sprint
     * To make sure it draw burndown chart correctly 
     * @param sprintId
     */
    public void createPointRemainToCorrectBurdownChart(Long sprintId) {
    	List<Issue> issues = issueDao.findIssuesByTypeAndSprint(sprintId, "Task");
    	List<PointRemain> allPointRemainOfSprint = new ArrayList<PointRemain>();
    	for (Issue issue: issues) {
    		List<PointRemain> pointRemains = pointRemainDao.findPointRemainByIssueIdAndSprint(issue.getIssueId(), sprintDao.findSprintById(sprintId));
    		allPointRemainOfSprint.addAll(pointRemains);
    	}
    	Collections.sort(allPointRemainOfSprint, new Comparator<PointRemain>() {
			@Override
			public int compare(PointRemain o1, PointRemain o2) {
				if (o1.getDateUpdate().getTime() < o2.getDateUpdate().getTime()) return -1;
				else return 1;
			}
    		
		});
    	
    	Date datePrevious = new Date();
    	int maxItem = 0;
    	
    	List<PointRemain> prevDatePointRemain = new ArrayList<PointRemain>();
    	List<PointRemain> curDatePointRemain = new ArrayList<PointRemain>();
    	for (int i = 0; i < allPointRemainOfSprint.size(); i++) {
    		PointRemain pointRemain = allPointRemainOfSprint.get(i);
    		Calendar calendar = Calendar.getInstance();
    		calendar.setTime(pointRemain.getDateUpdate());
    	    calendar.set(Calendar.HOUR_OF_DAY, 0);
    	    calendar.set(Calendar.MINUTE, 0);
    	    calendar.set(Calendar.SECOND, 0);
    	    calendar.set(Calendar.MILLISECOND, 0);
    	    Date dateUpdate = calendar.getTime();
    	    
    		if ((i == 0) || (dateUpdate.equals(datePrevious))) {
    			curDatePointRemain.add(pointRemain);
    		} else {
    			if (curDatePointRemain.size() > maxItem) maxItem = curDatePointRemain.size();
    			//Problem occur, we need to insert new point remain
    			if (prevDatePointRemain.size() > curDatePointRemain.size()) {
    				for (PointRemain prevPointRemain : prevDatePointRemain) {
    					Calendar calendarNextDate = Calendar.getInstance();
    		    		calendarNextDate.setTime(prevPointRemain.getDateUpdate());
    		    		calendarNextDate.add(Calendar.DATE, 1);
    		    	    calendarNextDate.set(Calendar.HOUR_OF_DAY, 0);
    		    	    calendarNextDate.set(Calendar.MINUTE, 0);
    		    	    calendarNextDate.set(Calendar.SECOND, 0);
    		    	    calendarNextDate.set(Calendar.MILLISECOND, 0);
    		    	    
    		    	    PointRemain savePointRemain = new PointRemain();
    		    	    savePointRemain.setIssue(prevPointRemain.getIssue());
    		    	    savePointRemain.setDateUpdate(calendarNextDate.getTime());
    		    	    savePointRemain.setPointRemain(prevPointRemain.getPointRemain());
    		    	    boolean skip = false;
    		    	    for (PointRemain curPointRemain : curDatePointRemain) {
							if (savePointRemain.getIssue().getIssueId() == curPointRemain.getIssue().getIssueId()) {
								skip = true;
							}
						}
    		    	    if (!skip) pointRemainDao.save(savePointRemain);
					}
    			}
    			prevDatePointRemain = curDatePointRemain;
    			curDatePointRemain = new ArrayList<PointRemain>();
    			curDatePointRemain.add(pointRemain);
    		}
    		datePrevious.setTime(dateUpdate.getTime());
		}
    	
	}

    public HashMap<String, Float> sumPointandCountUserStory(ArrayList<Float> arrPoint) {
	HashMap<String, Float> result = new HashMap<String, Float>();
	float rPoint = 0;
	float countIssue = 0;
	for (Float p : arrPoint) {
	    rPoint += p.floatValue();
	    if (p != 0)
		countIssue++;
	}
	result.put("TOTAL_POINT", rPoint);
	result.put("TOTAL_ISSUE", countIssue);
	return result;
    }

    @Override
    public Hashtable<String, Object> getDataChartBySprintNoJSON(Long sprintId) {
	Hashtable<String, Float> bugTable = this.processingBugBelongDate(sprintId);
	Map<String, ArrayList<Float>> issueTable = this.processingIssueBelongDate(sprintId);

	Set<String> dList = issueTable.keySet();
	// Create point list and issue list
	List<Float> pList = new ArrayList<Float>();
	List<Float> iList = new ArrayList<Float>();
	List<Float> bList = new ArrayList<Float>();
	for (String date : dList) {
	    ArrayList<Float> arrPoint = issueTable.get(date);
	    if (arrPoint != null) {
		HashMap<String, Float> result = sumPointandCountUserStory(arrPoint);
		pList.add(result.get("TOTAL_POINT"));
		iList.add(result.get("TOTAL_ISSUE"));

		// check date have bug?
		if (bugTable.containsKey(date)) {
		    bList.add(bugTable.get(date));
		} else {
		    bList.add((float) 0);
		}
	    } else {
		if (pList.size() == 0 && bList.size() == 0) {
		    pList.add((float) 0);
		    iList.add((float) 0);
		    bList.add((float) 0);
		} else {
		    pList.add(null);
		    iList.add(null);
		    bList.add(null);
		}
	    }
	}

	Hashtable<String, Object> rs = new Hashtable<String, Object>();
	rs.put("dList", dList);
	rs.put("pList", pList);
	rs.put("iList", iList);
	rs.put("bList", bList);
	return rs;
    }

}
