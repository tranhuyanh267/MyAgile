package com.ant.myagile.service.impl;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hibernate.HibernateException;
import org.omnifaces.model.tree.ListTreeModel;
import org.omnifaces.model.tree.TreeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dto.IssueStateLazyLoading;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.MemberIssueService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.utils.Utils;

@Service
@Transactional
public class IssueServiceImpl implements IssueService 
{
    	private Logger logger = Logger.getLogger(IssueServiceImpl.class);
    
	private static final String FORMAT_DT = "1";
	private static final String FORMAT_NUMBER = "2";
	private static final String PATTERN_NUMBER = "\\d{1,2}(\\.5)?(\\.0+)?";
	private static final String PATTERN_DT = "\\d{1,2}(\\.5)?(\\.0+)?\\s\\d{1,2}(\\.5)?(\\.0+)?";
	String pattern = "D\\d{1,2}(\\.5)?(\\.0+)?T\\d{1,2}(\\.5)?(\\.0+)?";
	private static final String STATUS_DONE_COLUMN = "Done";
	private static final String STATUS_ACCEPTED_COLUMN = "Accepted";
	private static final int FIRST_INDEX =0;
	public static final int MAX_PERCENT = 100;
	
	@Autowired
	public IssueDao issueDao;
	@Autowired
	public StatusService statusService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private PointRemainService pointRemainService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private Utils utils;
	@Autowired
	private MemberIssueService memberIssueService;


	@Override
	public Issue findIssueById(long issueId) 
	{
		return this.issueDao.findIssueById(issueId);
	}

	@Override
	public List<Issue> findIssueBySprintId(long sprintId) 
	{
		return this.issueDao.findIssueBySprintId(sprintId);
	}

	@Override
	public List<Issue> findIssueByStatus(Status status)
	{
		return this.issueDao.findIssueByStatus(status);
	}

	@Override
	public boolean saveIssue(Issue issue) 
	{
		logger.debug("Enter save issue!");
		if (this.issueDao.saveIssue(issue)) {
			historyService.saveHistoryForNewIssue(issue);
			return true;
		}
		return false;
	}

	@Override
	public void deleteIssue(Issue issue) 
	{
		this.issueDao.deleteIssue(issue);
	}

	@Override
	public boolean updateIssue(Issue issue) 
	{
		logger.debug("Enter updateIssue!");
		this.utils.evict(issue);
		Issue oldIssue = this.issueDao.findIssueById(issue.getIssueId());
		// Check issue changed field by field to log into history
		this.historyService.saveHistoryForIssue(oldIssue, issue);
		this.utils.evict(oldIssue);
		logger.debug("Exit updateIssue!");
		return this.issueDao.updateIssue(issue);
	}

	@Override
	public List<Issue> findIssueByTeamAndProject(Long teamId, Long projectId) 
	{
		return this.issueDao.findIssueByTeamAndProject(teamId, projectId);
	}

	@Override
	public boolean assignMemberToIssue(Issue issue, Member member) 
	{
		issue.setAssigned(member);
		return this.updateIssue(issue);
	}

	@Override
	public List<Issue> findIssueByTeamAndSprint(Long teamId, Long sprintId) 
	{
		return this.issueDao.findIssueByTeamAndSprint(teamId, sprintId);
	}

	@Override
	public int calculateProgress(Issue issue) 
	{
		float estimatePoint = Utils.convertPoint(issue.getEstimate());
		try {
			if (estimatePoint > 0) {
				float remainPoint = Utils.convertPoint(issue.getRemain());
				float numerator = ((estimatePoint - remainPoint) >= 0) ? estimatePoint - remainPoint : 0;
				return (int) ((numerator / estimatePoint) * MAX_PERCENT);
			} else if (issue.getStatus().getType().equals(Status.StatusType.DONE)) {
				return MAX_PERCENT;
			} else {
				return 0;
			}
		} catch (Exception e) {
		    logger.error("calculateProgress error: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public void updateStatusForIssue(long statusId, long issueId) 
	{
		Issue issue = this.issueDao.findIssueById(issueId);
		this.utils.evict(issue);
		Status status = this.statusService.findStatusById(statusId);
		
		if (!issue.getStatus().getStatusId().equals(status.getStatusId())) 
		{
			issue.setStatus(status);
			this.updatePointRemainWhenDone(issue);
			this.updateIssue(issue);
		}
	}

	@Override
	public void updatePointRemainWhenDone(Issue issue) 
	{
		if ((issue.getStatus().getType().equals(Status.StatusType.DONE)) &&  (!issue.getRemain().equals("D0T0"))) 
		{		
			issue.setRemain("D0T0");
			this.updatePointRemain("D0T0", issue.getIssueId());		
		}
	}

	@Override
	public int findProgressOfIssue(long issueID) 
	{
		try {
			Issue parentIssue = this.findIssueById(issueID);
			if (this.findIssueByParent(parentIssue).size() > 0)
			{
				float totalEsPoint = 0;
				float totalRePoint = 0;
				for (Issue issueChild : this.findIssueByParent(parentIssue)) {
					totalEsPoint += Utils
							.convertPoint(issueChild.getEstimate());
					totalRePoint += Utils.convertPoint(issueChild.getRemain());
				}
				if (this.checkStatusDoneOfIssue(parentIssue)) {
					return MAX_PERCENT;
				}
				if (totalEsPoint == 0) {
					return 0;
				}
				int result = (int) (totalRePoint * MAX_PERCENT / totalEsPoint);
				return Math.max(0, Math.min(MAX_PERCENT, MAX_PERCENT - result));

			} else {
				if (parentIssue.getEstimate().equals("D0T0")) {
					if (parentIssue.getStatus().getType().equals(Status.StatusType.DONE)
							|| parentIssue.getStatus().getType().equals(Status.StatusType.ACCEPTED_SHOW)
							|| parentIssue.getStatus().getType().equals(Status.StatusType.ACCEPTED_HIDE)) {
						return MAX_PERCENT;
					}
					return 0;
				}
				float remain, estimate;

				estimate = Utils.convertPoint(parentIssue.getEstimate());
				remain = Utils.convertPoint(parentIssue.getRemain());
				int result = MAX_PERCENT - (int) (remain * MAX_PERCENT / estimate);
				return Math.max(0, Math.min(MAX_PERCENT, result));
			}
		} catch (Exception e) {
		    logger.error("findProgressOfIssue error: " + e.getMessage());
			return 0;
		}
	}

	@Override
	public String findStatusOfIssue(Issue issue) 
	{
		if (issue.getStatus() == null) {
			return "IN_PROGRESS";
		} else if (issue.getStatus().getType().equals(Status.StatusType.START)) {
			return "TODO";
		} else if (issue.getStatus().getType().equals(Status.StatusType.DONE)
				|| (issue.getStatus().getType().equals(Status.StatusType.ACCEPTED_HIDE)) 
				|| (issue.getStatus().getType().equals(Status.StatusType.ACCEPTED_SHOW))) {
			return "DONE";
		} 
		
		return "IN_PROGRESS";
	}

	@Override
	public void updateStatusOfIssueParent(Issue issue) 
	{
		Issue parentIssue = issue;
		List<Issue> issueChildList = this.findIssueByParent(parentIssue);
		Set<Status> statusSet = new HashSet<Status>();
		
		if (issueChildList != null && issueChildList.size() > 0) 
		{
			int countStart = 0;
			int countDone = 0;
			parentIssue.setRemain(countRemainPoints(issueChildList));;
			for (Issue is : issueChildList) {
				Status statusIssue = is.getStatus();
				if (statusIssue.getType().equals(Status.StatusType.START)) {
					countStart++;
				} else if ((statusIssue.getType().equals(Status.StatusType.DONE)) 
						|| (statusIssue.getType().equals(Status.StatusType.ACCEPTED_HIDE)) 
						|| (statusIssue.getType().equals(Status.StatusType.ACCEPTED_SHOW))) {
					countDone++;
				} else {
					statusSet.add(statusIssue);
				}
			}
			if (countStart > 0 && countDone == 0) {
				parentIssue.setStatus(issueChildList.get(0).getStatus());
			} else if (countStart == 0 && countDone > 0) {
				parentIssue.setStatus(issueChildList.get(0).getStatus());
			} else {
				for(Status statusTemp : statusSet){
					if(parentIssue.getStatus() == null){
						parentIssue.setStatus(statusTemp);
					}else if(parentIssue.getStatus().getStatusId() > statusTemp.getStatusId()){
						parentIssue.setStatus(statusTemp);
					}
				}
				
			}
			this.updateIssue(parentIssue);
		}
	}

	@Override
	public List<Issue> findIssueByParent(Issue parent) 
	{
		return this.issueDao.findIssueByParent(parent);
	}

	@Override
	public List<Issue> findIssuesParent(long projectId, long sprintId) 
	{
		try {
			return this.issueDao.findIssuesParent(projectId, sprintId);
		} catch (HibernateException e) {
		    	logger.error("findIssuesParent error: " + e.getMessage());
			return null;
		}
	}

	@Override
	public List<Issue> findIssuesNotParent(long sprintId) 
	{
		try {
			return this.issueDao.findIssuesNotParent(sprintId);
		} catch (HibernateException e) {
		    logger.error("findIssuesNotParent error: " + e.getMessage());
		    return null;
		}
	}

	@Override
	public List<Issue> findIssuesIsTaskBySprintId(long sprintId) {
		return this.issueDao.findIssuesIsTaskBySprintId(sprintId);
	}

	@Override
	public List<Issue> findIssuesByTypeAndSprint(long sprintId, String type) {
		return this.issueDao.findIssuesByTypeAndSprint(sprintId, type);
	}

	@Override
	public String getTotalPoint(Issue issue) 
	{
		List<Issue> children = this.findIssueByParent(issue);
		if(children != null && children.size()>0)
			return this.countEstimatePoints(children);
		else
			return issue.getEstimate();		
	}

	@Override
	public void updatePointRemain(String pointRemain, Long issueId) 
	{
		String pointActual = pointRemain;
		if (pointActual.matches(PATTERN_NUMBER)) {
			pointActual = String.format("D%sT%s", pointActual,
					0);
		}
		if (pointActual.matches(PATTERN_DT)) {
			String[] str = pointActual.split("\\s");
			pointActual = String.format("D%sT%s", str[0], str[1]);
		}
		String pointRemainUpper = pointActual.toUpperCase();
		Issue issue = this.findIssueById(issueId);
		issue.setRemain(pointRemainUpper);
		this.updateIssue(issue);
		PointRemain pointCheck = this.pointRemainService
				.findPointRemainByIssueIdAndNowDate(issueId);

		if (pointCheck != null) {
			pointCheck.setPointRemain(pointRemainUpper);
			this.pointRemainService.update(pointCheck);
		} else {
			this.pointRemainService.save(issue, pointRemainUpper);
			this.pointRemainService.saveDataForFirstTimes(new Date(), issue.getSprint());
		}
	}

	@Override
	public float calculateIssuesTotalPoints(List<Issue> issues)
	{
		float totalPoints = 0;
		if (issues.size() > 0) {
			for (int i = 0; i < issues.size(); i++) {
				totalPoints += Utils.convertPoint(issues.get(i).getEstimate());
			}
		}
		return totalPoints;
	}
	
	@Override
	public float calculateIssuesPointDelivered(List<Issue> issues) 
	{
		float pointDelivered = (float) 0.0;
		for(Issue issue : issues){
			Status.StatusType statusType = issue.getStatus().getType();
			if((statusType.equals(Status.StatusType.ACCEPTED_HIDE)) || (statusType.equals(Status.StatusType.ACCEPTED_SHOW))){
				pointDelivered += this.findTotalEstimatePoint(issue);
			}
		}
		return pointDelivered;
	}
	
	@Override
	public int calculateUserStoryDelivered(List<Issue> issues)
	{
		int userStoryDelivered = 0;
		for(Issue issue : issues){
			Status.StatusType statusType = issue.getStatus().getType();
			if((statusType.equals(Status.StatusType.ACCEPTED_HIDE)) || (statusType.equals(Status.StatusType.ACCEPTED_SHOW)) ){
				userStoryDelivered += 1;
			}
		}
		return userStoryDelivered;
	}

	@Override
	public float findTotalRemainPoint(Issue issue)
	{
		if (this.findIssueByParent(issue).size() == 0) {
		    if ((issue.getSprint().getStatus().equals("closed")) && (Utils.convertPoint(issue.getRemain()) > 0.0f)) {
		        return 0;
		    } else {
                return Utils.convertPoint(issue.getRemain());
            }
		} else {
			float result = 0;
			for (Issue child : this.findIssueByParent(issue)) {
                if (!((child.getSprint().getStatus().equals("closed")) && (Utils.convertPoint(child.getRemain()) > 0.0f))) {
                    result += Utils.convertPoint(issue.getRemain());
                }
			}
			return result;
		}
	}

	@Override
	public float findTotalEstimatePoint(Issue issue) 
	{
	    if (this.findIssueByParent(issue).size() == 0) 
	    {
                if ((issue.getSprint().getStatus().equals("closed")) && (Utils.convertPoint(issue.getRemain()) > 0.0f)) {
                    return 0;
                } else {
                    return Utils.convertPoint(issue.getEstimate());
                }
	    } else {
                float result = 0;
                for (Issue child : this.findIssueByParent(issue)) 
                {
                    if (!((child.getSprint().getStatus().equals("closed")) && (Utils.convertPoint(child.getRemain()) > 0.0f))) {
                        result += Utils.convertPoint(issue.getEstimate());
                    }
                }
                return result;
	    }
	}

	@Override
	public List<Issue> findIssuesSingle(long sprintId) 
	{
		return this.issueDao.findIssuesSingle(sprintId);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<Issue> filterIssues(List<Issue> originalIssueList, String filterIssueType, String filterIssueStatus, String filterIssuePriority) 
	{
		List<Issue> filteredIssue = new ArrayList<Issue>();
		Matcher issueTypeMatcher = ((filterIssueType.equalsIgnoreCase("Any")) ? Matchers
				.anything() : Matchers.equalToIgnoringCase(filterIssueType));
		Matcher issueStatusMatcher = ((filterIssueStatus
				.equalsIgnoreCase("Any")) ? Matchers.anything() : Matchers
				.equalTo(filterIssueStatus));
		Matcher issuePriorityMatcher = ((filterIssuePriority
				.equalsIgnoreCase("Any")) ? Matchers.anything() : Matchers
				.equalToIgnoringCase(filterIssuePriority));

		filteredIssue = filter(
				having(on(Issue.class).getType(), issueTypeMatcher),
				originalIssueList);
		filteredIssue = filter(
				having(on(Issue.class).getStatus().getName(),
						issueStatusMatcher), filteredIssue);
		filteredIssue = filter(
				having(on(Issue.class).getPriority(), issuePriorityMatcher),
				filteredIssue);
		return filteredIssue;
	}

	@Override
	public boolean checkIssuesBelongToWithUserStory(Long userStoryId) 
	{
		return this.issueDao.checkIssuesBelongUserStory(userStoryId);
	}

	@Override
	public List<Issue> findIssuesByUserStory(Long userStoryId) 
	{
		return this.issueDao.findIssuesByUserStory(userStoryId);
	}

	@Override
	public List<Issue> findNoChidrenIssuesByUserStory(Long userStoryId) 
	{
		List<Issue> parents= new ArrayList<Issue>();
		List<Issue> result=new ArrayList<Issue>();
		parents=findIssuesByUserStory(userStoryId);
		
		if(parents!=null)
		{
			for(Issue is: parents)
			{
				List<Issue> children=findIssueByParent(is);
				if(children.size()>0){
					result.addAll(children);
				}else{
					result.add(is);
				}
			}
			return result;
		}
		return result;
	}

	@Override
	public boolean isParentAndTotalChildrenPointEqual(Issue parent) 
	{
		List<Issue> children = this.findIssueByParent(parent);
		String totalChildrenPoint = this.countEstimatePoints(children);
		
		try {
			if (children.isEmpty()) {
				return true;
			} else {
				return Utils.convertPoint(totalChildrenPoint) == Utils
						.convertPoint(parent.getEstimate());
			}
		} catch (Exception e) {
		    	logger.error("isParentAndTotalChildrenPointEqual" + e.getMessage());
			return true;
		}
	}

	@Override
	public boolean showPointConflictWarning(Issue issue) 
	{
		return (!this.isParentAndTotalChildrenPointEqual(issue))
				&& (issue.getParent() == null);
	}

	@Override
	public String countEstimatePoints(List<Issue> issueList)
	{
		float totalDevPoint = 0;
		float totalTestPoint = 0;
		
		try {
			for (Issue i : issueList) {
				String estimatePoint = i.getEstimate().toUpperCase();
				float childDevPoint = Float.parseFloat(estimatePoint.substring(
						1, estimatePoint.indexOf('T')));
				float childTestPoint = Float.parseFloat(estimatePoint
						.substring(estimatePoint.indexOf('T') + 1,
								estimatePoint.length()));
				totalDevPoint += childDevPoint;
				totalTestPoint += childTestPoint;
			}
		} catch (Exception e) {
		    logger.error("countEstimatePoints error: " + e.getMessage());
		}
		String totalPoint = "D" + totalDevPoint + "T" + totalTestPoint;
		totalPoint = totalPoint.replace(".0", "");
		return totalPoint;
	}
	
	@Override
	public String countRemainPoints(List<Issue> issueList) 
	{
		float totalDevPoint = 0;
		float totalTestPoint = 0;
		try {
			for (Issue i : issueList) {
				String remainPoint = i.getRemain().toUpperCase();
				float childDevPoint = Float.parseFloat(remainPoint.substring(
						1, remainPoint.indexOf('T')));
				float childTestPoint = Float.parseFloat(remainPoint
						.substring(remainPoint.indexOf('T') + 1,
								remainPoint.length()));
				totalDevPoint += childDevPoint;
				totalTestPoint += childTestPoint;
			}
		} catch (Exception e) {
		    logger.error("countRemainPoints error: " + e.getMessage());
		}
		String totalPoint = "D" + totalDevPoint + "T" + totalTestPoint;
		totalPoint = totalPoint.replace(".0", "");
		return totalPoint;
	}

	
	@Override
	public boolean checkStatusDoneOfIssue(Issue issue) 
	{
		List<Issue> issues = this.findIssueByParent(issue);
		if (issues == null || issues.size() == 0) {
			return issue.getStatus().getType().equals(Status.StatusType.DONE);
		}
		boolean flag = true;
		for (Issue childIssue : issues) {
			if (!childIssue.getStatus().getType().equals(Status.StatusType.DONE)) {
				flag = false;
			}
		}
		return flag;
	}

	@Override
	public void updateEstimatePoint(Issue issue, String point) 
	{
		String estimatePointActual = point;
		String pointFormat = "1";
		if (estimatePointActual.matches(PATTERN_NUMBER)) {
			estimatePointActual = String.format("D%sT%s", estimatePointActual, 0);
		}
		if (estimatePointActual.matches(PATTERN_DT)) {
			String[] str = estimatePointActual.split("\\s");
			estimatePointActual = String.format("D%sT%s", str[0], str[1]);
		}
		issue.setEstimate(estimatePointActual.toUpperCase());
		issue.setPointFormat(pointFormat);
		this.updateIssue(issue);
	}

	@Override
	public boolean existHistoryOfPointRemain(Issue issue)
	{
		List<PointRemain> points = pointRemainService.findPointRemainByIssueId(issue.getIssueId());
		
		if (points.size() == 0 || (points.size() == 1 && points.get(0).getPointRemain().equals(issue.getEstimate()))){
		    return false;
		}
		
		return true;
	}

	@Override
	public Issue findRootIssueBySubIssue(Issue issue) 
	{
		Issue rootIssue = issue;
		while (rootIssue.getParent() != null) {
			rootIssue = this.findRootIssueBySubIssue(rootIssue.getParent());
		}
		return rootIssue;
	}

	@Override
	public TreeModel<Issue> buildIssueRelationshipTree(Issue rootIssue) 
	{
		TreeModel<Issue> issueTreeList = new ListTreeModel<Issue>();		
		TreeModel<Issue> root = issueTreeList.addChild(rootIssue);		
		List<Issue> subIssues = this.issueDao.findIssueByParent(rootIssue);		
		for(int i = 0; i < subIssues.size(); i++) {
			root.addChild(subIssues.get(i));
		}		
		return issueTreeList;
	}

	@Override
	public float calculateProgressForParentIssueTask(Issue issue)
	{
		float progressPercent = 0f;
		try {
			progressPercent = 0;
			List<Issue> subIssues = this.issueDao.findIssueByParent(issue);			
			float totalPointOfParentIssue = Utils.convertPoint(issue.getEstimate());
			float totalEstimatePointOfChildIssues = 0;
			float totalRemainPointOfChildIssues = 0;
			int totalTaskWithDoneStatus = 0;			
			boolean hasIssueWithPoint = false;			
			for(int i = 0; i < subIssues.size(); i++) {
				float estimatePoint = Utils.convertPoint(subIssues.get(i).getEstimate());
				float remainPoint = Utils.convertPoint(subIssues.get(i).getRemain());				
				if(estimatePoint != 0 || remainPoint != 0) {
					hasIssueWithPoint = true;
				}				
				totalEstimatePointOfChildIssues += estimatePoint;
				totalRemainPointOfChildIssues += remainPoint;
				if(subIssues.get(i).getStatus() != null && (subIssues.get(i).getStatus().getType().equals(Status.StatusType.DONE))) {
					totalTaskWithDoneStatus++;
				}
			}
			
			if (totalPointOfParentIssue == totalEstimatePointOfChildIssues) {
				progressPercent = this.calculateProgressWhenParentEqualChilds(
						totalRemainPointOfChildIssues, totalPointOfParentIssue,
						hasIssueWithPoint, totalTaskWithDoneStatus, subIssues.size());
			} else if (totalPointOfParentIssue != totalEstimatePointOfChildIssues) {
				progressPercent = this.calculateProgressWhenParentNotEqualChilds(
						totalRemainPointOfChildIssues,
						totalEstimatePointOfChildIssues, hasIssueWithPoint,
						totalTaskWithDoneStatus,
						subIssues.size());
			}
			progressPercent = Math.max(0, Math.min(MAX_PERCENT, progressPercent));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return progressPercent;
	}

	@Override
	public float calculateProgressWhenParentEqualChilds( float totalRemainPointOfChildIssues, float totalPointOfParentIssue,
			boolean hasIssueWithPoint, int totalTaskWithDoneStatus, int totalTask) 
	{
		float progressPercent = 0f;
		if(hasIssueWithPoint) {
			progressPercent = (1 - (totalRemainPointOfChildIssues / totalPointOfParentIssue)) * MAX_PERCENT;
		} else {
			progressPercent = totalTaskWithDoneStatus * MAX_PERCENT / totalTask;
		}
		return progressPercent;
	}

	@Override
	public float calculateProgressWhenParentNotEqualChilds( float totalRemainPointOfChildIssues, float totalEstimatePointOfChildIssues, 
								boolean hasIssueWithPoint, int totalTaskWithDoneStatus, int totalTask) 
	{
		float progressPercent = 0f;
		if(hasIssueWithPoint) {
			progressPercent = (1 - (totalRemainPointOfChildIssues / totalEstimatePointOfChildIssues)) * MAX_PERCENT;
		} else {
			progressPercent = totalTaskWithDoneStatus * MAX_PERCENT / totalTask;
		}
		return progressPercent;
	}
	
	@Override
	public boolean isDTFormat(String point)
	{
		return point.matches(PATTERN_DT);
	}
	
	@Override
	public boolean isNumberFormat(String point)
	{
		return point.matches(PATTERN_NUMBER);
	}
	
	@Override
	public String converterToDTFormat(String point)
	{
		String returnString = "";
		String[] str = point.split("\\s");
		returnString = String.format("D%sT%s", str[0], str[1]);
		return returnString;
	}
	
	@Override
	public String converterNumberToDTFormat(String point)
	{
		String returnString = "";
		returnString = String.format("D%sT%s", point, 0);		
		return returnString;
	}		

	@Override
	public String getPointByFormat(String point, String format) 
	{
		String returnPoint = point;
		if(format.equals(FORMAT_NUMBER)){
			returnPoint = Integer.toString((int) Utils.convertPoint(point));
		}
		return returnPoint;
	}
	
	@Override
	public String checkingPointFormat(String estimatePoint) 
	{
		return estimatePoint.matches(PATTERN_NUMBER) ? FORMAT_NUMBER : FORMAT_DT;		
	}
			
	@Override
	public String checkingEstimatePoint(String estimatePoint)
	{
		String returnString = estimatePoint;
		if(estimatePoint.equals("")){
			returnString = "D0T0";
		} else if (this.isNumberFormat(estimatePoint)) {
			returnString = this.converterNumberToDTFormat(estimatePoint);
		} else if (this.isDTFormat(estimatePoint)) {
			returnString = this.converterToDTFormat(estimatePoint);
		}
		returnString = returnString.toUpperCase();
		return returnString; 
	}
	
	@Override
	public String checkingRemainPoint(String remainPoint)
	{
		String returnString = remainPoint;
		if(remainPoint.equals("")){
			returnString = "D0T0";
		} else if (this.isNumberFormat(remainPoint)) {
			returnString = this.converterNumberToDTFormat(remainPoint);
		} else if (this.isDTFormat(remainPoint)) {
			returnString = this.converterToDTFormat(remainPoint);
		}
		returnString = returnString.toUpperCase();
		return returnString;
	}

	@Override
	public String findCurrentPointRemainOfIssue(Issue issue) 
	{
		List<PointRemain> prList = new ArrayList<PointRemain>();
		prList.addAll(pointRemainService.findPointRemainByIssueId(issue.getIssueId()));
		if(prList.size() > 0){
			return prList.get(FIRST_INDEX).getPointRemain();
		}
		return issue.getRemain();
	}

	@Override
	public Issue findIssueByUserStoryAndSprint(long userStoryId, long sprintId) 
	{
	    return issueDao.findIssueByUserStoryAndSprint(userStoryId, sprintId);
	}

	@Override 
	public Issue copyCommonIssueInformation(Issue issue) 
	{
	    Issue issueCopy = new Issue();
	    issueCopy.setSubject(issue.getSubject());
            issueCopy.setDescription(issue.getDescription());
            issueCopy.setPriority(issue.getPriority());
            issueCopy.setType(issue.getType());
            issueCopy.setPointFormat(issue.getPointFormat());
            issueCopy.setRemain("D0T0");
            issueCopy.setEstimate("D0T0");
            return issueCopy;
	}

    @Override
    public void copyIssueToSprint(long issueId, long sprintId)
    {
        Sprint sprintCopy = sprintService.findSprintById(sprintId);
        Issue issue = this.findIssueById(issueId);
        Issue issueCopy = copyCommonIssueInformation(issue);
        issueCopy.setSprint(sprintCopy);
        Status startStatus = statusService.findStatusStartBySprintId(sprintId);
        issueCopy.setStatus(startStatus);
        if (issue.getUserStory() != null) {
            Issue issueCopyParent = this.findIssueByUserStoryAndSprint(issue.getUserStory().getUserStoryId(), sprintId);
            if (issueCopyParent != null) {
                issueCopy.setParent(issueCopyParent);
            } else {
                issueCopy.setUserStory(issue.getUserStory());
            }
            saveIssue(issueCopy);
        } else {
            Issue issueParent = issue.getParent();
            Issue issueCopyParent = this.findIssueByUserStoryAndSprint(issueParent.getUserStory().getUserStoryId(), sprintId);
            if (issueCopyParent != null) {
                issueCopy.setParent(issueCopyParent);
                this.saveIssue(issueCopy);
            } else {
                Issue issueParentCopy = copyCommonIssueInformation(issueParent);
                issueParentCopy.setUserStory(issueParent.getUserStory());
                issueParentCopy.setSprint(sprintCopy);
                issueParentCopy.setStatus(startStatus);
                issueCopy.setParent(issueParentCopy);
                this.saveIssue(issueParentCopy);
                this.saveIssue(issueCopy);
            }
        }
    }

    public void setSprintService(SprintService sprintService) 
    {
        this.sprintService = sprintService;
    }

    public void setStatusService(StatusService statusService) 
    {
        this.statusService = statusService;
    }

	@Override
	public long saveIssueAndGetId(Issue issue)
	{
		return issueDao.saveIssueAndGetId(issue);
	}

	@Override
	public void deleteAllIssueInSprint(Sprint sprint) 
	{
		List<Issue> issues = issueDao.findIssueBySprintId(sprint.getSprintId());
		if(issues.size() > 0){
			for (Issue issue : issues) {
				memberIssueService.deleteAllByIssue(issue.getIssueId());
				pointRemainService.deleteAllPointRemainByIssue(issue);
				issueDao.deleteIssue(issue);
			}
		}
	}

	@Override
	public List<Issue> findParentIssuesBySprintAndExceptParentThis( long issueId, long sprintId)
	{
		return issueDao.findParentIssuesBySprintAndExceptParentThis(issueId, sprintId);
	}

	@Override
	public List<Issue> findChildrenIssuesOfRemainingParent(long parentId)
	{
		return issueDao.findChildrenIssuesOfRemainingParent(parentId);
	}

	@Override
	public List<Issue> findRemainingTasksByProjectAndTeam(long projectId, long teamId) 
	{
		return issueDao.findRemainingTasksByProjectAndTeam(projectId, teamId);
	}

	@Override
	public boolean checkIssueParentDoneBySubject(String subject) 
	{
		return issueDao.checkIssueParentDoneBySubject(subject);
	}
	@Override
	public boolean checkIssueChildDoneBySubject(String subject) 
	{
		return issueDao.checkIssueChildDoneBySubject(subject);
	}

	@Override
	public boolean checkSubIssueInSprintBySubject(Issue issue, Sprint sprint)
	{
		return issueDao.checkSubIssueInSprintBySubject(issue, sprint);
	}

	@Override
	public Issue findNewestIssueAvailableBySubjectNotInSprint(Issue issue, Sprint exceptSprint) 
	{
		return issueDao.findNewestIssueAvailableBySubjectNotInSprint(issue, exceptSprint);
	}

	@Override
	public Issue findIssueByUserStoryAndSprintAndCurrentIssue( Issue currentIssue,Sprint sprint)
	{
		return issueDao.findIssueByUserStoryAndSprintAndCurrentIssue(currentIssue,sprint);
	}

	@Override
	public Issue findOldestTaskOfUserStory(Long userStoryId) 
	{
		return issueDao.findOldestTaskOfUserStory(userStoryId);
	}

	@Override
	public Issue findNewestTaskOfUserStory(Long userStoryId)
	{
		return issueDao.findNewestTaskOfUserStory(userStoryId);
	}

	@Override
	public Sprint findSprintHasContinuousIssue(Issue issue)
	{
		return issueDao.findSprintHasContinuousIssue(issue);
	}

	@Override
	public List<Sprint> getAllSprintBeforeByIssue(Issue issue) 
	{
		return issueDao.getAllSprintBeforeByIssue(issue);
	}

	@Override
	public List<Sprint> getAllSprintAfterByIssue(Issue issue) 
	{
		return issueDao.getAllSprintAfterByIssue(issue);
	}

	@Override
	public List<Issue> loadLazyIssuesBySprintId(Long sprintId, IssueStateLazyLoading issueStateLazyLoading)
	{
		return issueDao.loadLazyIssuesBySprintId(sprintId,issueStateLazyLoading);
	}
	
	@Override
	public List<Issue> findLazyIssuesParentByProjectIdAndSprintId( long projectId, long sprintId, StateLazyLoading lazyLoadingIsuseList)
	{
		return issueDao.findLazyIssuesParentByProjectIdAndSprintId(projectId,sprintId,lazyLoadingIsuseList);
	}

	@Override
	public List<Issue> findLazyIssueBySprintId(long sprintId, StateLazyLoading lazyLoadingSprintBacklogs) 
	{
		return issueDao.findLazyIssueBySprintId(sprintId,lazyLoadingSprintBacklogs);
	}

	@Override
	public int countTotalLazyIssueBySprintId(long sprintId, StateLazyLoading lazyLoadingSprintBacklogs) 
	{
		return issueDao.countTotalLazyIssueBySprintId(sprintId,lazyLoadingSprintBacklogs);
	}

	@Override
	public int countTotalLazyIssuesParentByProjectIdAndSprintId(long projectId, long sprintId, StateLazyLoading lazyLoadingIsuseList) 
	{
		return issueDao.countTotalLazyIssuesParentByProjectIdAndSprintId(projectId,sprintId,lazyLoadingIsuseList);
	}

	@Override
	public int countTotalLazyIssuesBySprintId(Long sprintId, IssueStateLazyLoading issueStateLazyLoading) 
	{
		return issueDao.countTotalLazyIssuesBySprintId(sprintId,issueStateLazyLoading);
	}

	@Override
	public List<Issue> findAllLazyIssuesBySprint(long sprintId) 
	{
		return issueDao.findAllLazyIssuesBySprint(sprintId);
	}

	@Override
	public boolean updateOrderIssue(String[] arryIdIssueOrder, long idStatusFromClient, boolean swimline) 
	{
		logger.warn("begin update order issue");
		try {
			if(arryIdIssueOrder == null){
				return false;
			}
			for(int i = 0;i < arryIdIssueOrder.length; i++){
				Issue issueById = issueDao.findIssueById(Long.parseLong(arryIdIssueOrder[i]));
				issueById.setOrderIssue(i);
				issueDao.updateIssue(issueById);
			}
			return true;
		} catch (Exception e) {
			logger.error("update order issue unsuccessful",e);
			return false;
		}
		
	}

	@Override
	public List<Issue> getIssueOrderByStatus(long statusId) 
	{
		return issueDao.getIssueOrderByStatus(statusId);
	}

	@Override
	public List<Issue> getIssueOrderByStatusAndSwimline(long statusId) 
	{
		return issueDao.getIssueOrderByStatusAndSwimline(statusId);
	}

	@Override
	public List<Issue> getIssueOrderByStatusAndNotInSwimline(long statusId) 
	{
		return issueDao.getIssueOrderByStatusAndNotInSwimline(statusId);
	}

	@Override
	public boolean updateAllIssueByStatusUserStoryInLastSprint( UserStory currentUserStory,Long currentTeamId) 
	{
		Sprint lastSprintOfTeam =  sprintService.findLastSprintByTeamId(currentTeamId);
		Issue issuesOfSprint = issueDao.findIssueByUserStoryAndSprint(currentUserStory.getUserStoryId(), lastSprintOfTeam.getSprintId());
		List<Issue> childIssues = new ArrayList<Issue>();
		
		if(issuesOfSprint != null) 
		{
			childIssues = issueDao.findIssueByParent(issuesOfSprint);
			
			if(currentUserStory.getStatus().equals(UserStory.StatusType.DONE))
			{
				Status statusDoneOfSprint = statusService.findStatusDoneBySprintId(lastSprintOfTeam.getSprintId());
				//update issue and child issue of last sprint
				//update done issue parent
				issuesOfSprint.setStatus(statusDoneOfSprint);
				issueDao.updateIssue(issuesOfSprint);
				//update done for child issue
				for(Issue childIssue : childIssues){
					childIssue.setStatus(statusDoneOfSprint);
					issueDao.updateIssue(childIssue);
				}
			} else {
				Status statusStartOfSprint = statusService.findStatusStartBySprintId(lastSprintOfTeam.getSprintId());
				//update issue and child issue of last sprint
				//update done issue parent
				issuesOfSprint.setStatus(statusStartOfSprint);
				issueDao.updateIssue(issuesOfSprint);
				//update done for child issue
				for(Issue childIssue : childIssues){
					childIssue.setStatus(statusStartOfSprint);
					issueDao.updateIssue(childIssue);
				}
			}
		}
		return false;
	}

	/**
	 * can move task from done column to accepted column in Scrum board
	 */
	@Override
	public boolean canChangeToAccepted(Issue issue,long idStatusFromClient) 
	{
		boolean canMove = false;
		Status status = statusService.findStatusById(idStatusFromClient);
		if(STATUS_DONE_COLUMN.equalsIgnoreCase(issue.getStatus().getName()) && STATUS_ACCEPTED_COLUMN.equalsIgnoreCase(status.getName())){
			canMove = true;
		}
		return canMove;
	}

	@Override
	public List<Issue> allIssueLazyIssuesBySprintId(Long sprintId, IssueStateLazyLoading issueStateLazyLoading) 
	{
		return issueDao.allIssueLazyIssuesBySprintId(sprintId,issueStateLazyLoading);
	}

	@Override
	public long getIdOfUserStory(long issueId)
	{
		Issue issue = findIssueById(issueId);
		
		if(issue.getParent() == null) 
		{
			return issue.getUserStory().getUserStoryId();
		}
		
		return issue.getIssueId();
	}

	@Override
	public void setVoidAllUnexpiredIssuesWhenSetUserStoryVoid(Long userStoryId) 
	{
		issueDao.setVoidAllUnexpiredIssuesWhenSetUserStoryVoid(userStoryId);
		
	}

	@Override
	public void destroyVoidAllUnexpiredIssuesWhenDestroyVoidUserStory(Long userStoryId) 
	{
		issueDao.destroyVoidAllUnexpiredIssuesWhenDestroyVoidUserStory(userStoryId);
		
	}
	
	@Override
	public List<Issue> findAllUnexpiredChildrenIssuesByUserStory(Long userStoryId)
	{
		return issueDao.findAllUnexpiredChildrenIssuesByUserStory(userStoryId);
	}

}
