package com.ant.myagile.service.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.PointRemainDao;
import com.ant.myagile.model.HistoryFieldChange;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.service.HistoryFieldChangeService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.utils.Utils;
import com.hp.gagawa.java.elements.Span;

@Service
public class PointRemainServiceImpl implements PointRemainService {
	@Autowired
	private PointRemainDao pointRemainDao;
	@Autowired
	private IssueService issueService;
	@Autowired
	private IssueDao issueDao;
	@Autowired
	private HistoryFieldChangeService historyFieldChangeService;

	@Override
	public boolean save(PointRemain pointRemain) {
		try{
			pointRemainDao.save(pointRemain);
			return true;
		}catch(HibernateException e){
			return false;
		}
	}

	@Override
	public boolean update(PointRemain pointRemain) {
		try{
			pointRemainDao.update(pointRemain);
			return true;
		}catch (HibernateException e){
			return false;
		}
	}

	@Override
	public List<PointRemain> findPointRemainByIssueId(long issueId) {
		return pointRemainDao.findPointRemainByIssueId(issueId);
	}

	@Override
	public List<PointRemain> findPointRemainByIssueIdAndSprint(long issueId, Sprint s) {
		return pointRemainDao.findPointRemainByIssueIdAndSprint(issueId, s);
	}
	
	public List<String> findHistoryOfPointRemainByIssueId(long issueId) {
		List<HistoryFieldChange> issueHistory = historyFieldChangeService.findUpdateHistoryOfPointRemainByIssueId(issueId);
		List<String> issueHistoryString = new ArrayList<String>();
		if(issueHistory != null && issueHistory.size() > 0) {
			for(int i = 0;i<issueHistory.size();i++) {
				Span textStrike = new Span();
				textStrike.setStyle("text-decoration: line-through");
				textStrike.appendText(issueHistory.get(i).getOldValue());
				issueHistoryString.add(textStrike.write());
			}
		} else {
			issueHistoryString.add("No History");
		}
		return issueHistoryString;
		
	}

	@Override
	public PointRemain findPointRemainByIssueIdAndNowDate(long issueId) {
		return pointRemainDao.findPointRemainByIssueIdAndNowDate(issueId);
	}

	@Override
	public List<Issue> findIssueDontUpdateByDateOfSprint(Date d, Sprint s) {
		List<PointRemain> listPointRemains= pointRemainDao.findAllIssueByDate(d);
		List<Issue> allIssuesOfSprint=issueDao.findIssuesIsTaskBySprintId(s.getSprintId());			
			for(int i=0;i<listPointRemains.size();i++)
			{				
				boolean flag=false;
				int j=0;
				for(;j<allIssuesOfSprint.size();j++)
				{
					if(allIssuesOfSprint.get(j).getIssueId().equals(listPointRemains.get(i).getIssue().getIssueId()))
					{
						flag=true;
						break;
					}
				}
				if(flag)
					allIssuesOfSprint.remove(j);
			}
		return allIssuesOfSprint;
	}

	@Override
	public void saveDataForFirstTimes(Date dateUpdate, Sprint sprint) {
		List<Issue> is=findIssueDontUpdateByDateOfSprint(dateUpdate, sprint);
		for(int i=0;i<is.size();i++)
		{
			PointRemain pr = new PointRemain();
			pr.setDateUpdate(new Date());
			pr.setIssue(is.get(i));
			pr.setPointRemain(is.get(i).getRemain());
			save(pr);
		}		
	}

	@Override
	public boolean save(Issue issue, String pointRemain) {
		try{
			PointRemain pointRemainObj = new PointRemain();
			pointRemainObj.setDateUpdate(new Date());
			pointRemainObj.setIssue(issue);
			pointRemainObj.setPointRemain(pointRemain);
			save(pointRemainObj);
			return true;
		}catch(HibernateException e){
			return false;
		}
	}

	@Override
	public boolean delete(PointRemain pointRemain) {
		try{
			pointRemainDao.delete(pointRemain);
			return true;
		}catch(HibernateException e){
			return false;
		}
	}

	@Override
	public void checkPointRemain(FacesContext context, UIComponent validate, Object value, String pointFormat) throws ValidatorException{
		String pointFormatTemp = pointFormat;
		Object checkValue = validate.getAttributes().get("pointR");
			if(checkValue != null){
				long pointR = Long.parseLong(checkValue.toString());
				Issue is = issueService.findIssueById(pointR);
				pointFormatTemp = is.getPointFormat();
			}
			String pointRemainStr = value.toString().toUpperCase().trim();
			String pattern = "D\\d{1,2}(\\.5)?(\\.0+)?T\\d{1,2}(\\.5)?(\\.0+)?";
			String pattern1 = "\\d{1,2}(\\.5)?(\\.0+)?";
			String pattern2 = "\\d{1,2}(\\.5)?(\\.0+)?\\s\\d{1,2}(\\.5)?(\\.0+)?";
						
			if((!pointRemainStr.matches(pattern2)) && (!pointRemainStr.matches(pattern)) && (pointFormatTemp.equals("1"))){
				FacesMessage msg =  new FacesMessage(FacesMessage.SEVERITY_ERROR,"", pointRemainStr + " is not valid (eg: D5T3 or 5 3)");
				throw new ValidatorException(msg);
			}
			
			if((!pointRemainStr.matches(pattern1)) && (pointFormatTemp.equals("2"))){
				FacesMessage msg =  new FacesMessage(FacesMessage.SEVERITY_ERROR,"", pointRemainStr + " is not valid (eg: 5)");
				throw new ValidatorException(msg);
			}
		}

	@Override
	public void checkEstimatePoint(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		String pointRemainStr = value.toString().toUpperCase().trim();
		String pattern = "D\\d{1,2}(\\.5)?(\\.0+)?T\\d{1,2}(\\.5)?(\\.0+)?";
		String pattern1 = "\\d{1,2}(\\.5)?(\\.0+)?";
		String pattern2 = "\\d{1,2}(\\.5)?(\\.0+)?\\s\\d{1,2}(\\.5)?(\\.0+)?";
		if(!(pointRemainStr.equals("")) && (!pointRemainStr.matches(pattern2)) && (!pointRemainStr.matches(pattern)) && (!pointRemainStr.matches(pattern1))){
			FacesMessage msg =  new FacesMessage(FacesMessage.SEVERITY_ERROR,"", pointRemainStr + " is not valid (eg: D5T3 or 5 or 5 3)");
			throw new ValidatorException(msg);
		}
	}

	@Override
	public String getPointRemainByFormat(Issue issue){
	    String pointRemainFormat = issue.getRemain();
        if(issue.getPointFormat().equals("2")){
            float pointDev = Float.parseFloat(Utils.getPointDev(pointRemainFormat));
            pointRemainFormat = String.format("%s",pointDev).replace(".0", "");
        }
        else{
            float pointDev = Float.parseFloat(Utils.getPointDev(pointRemainFormat));
            float pointTest = Float.parseFloat(Utils.getPointTest(pointRemainFormat));
            pointRemainFormat = String.format("D%sT%s", pointDev, pointTest).replace(".0", "");
        }
        return pointRemainFormat;
	}
	
	@Override
    public String getPointEstimateByFormat(Issue issue){
        String pointEstimateFormat = issue.getEstimate();
        if(issue.getPointFormat().equals("2")){
           float pointDev = Float.parseFloat(Utils.getPointDev(pointEstimateFormat));
           float pointTest = Float.parseFloat(Utils.getPointTest(pointEstimateFormat));
           pointEstimateFormat = String.format("%s", pointDev+pointTest).replace(".0", "");
        }
        else{
            float pointDev = Float.parseFloat(Utils.getPointDev(pointEstimateFormat));
            float pointTest = Float.parseFloat(Utils.getPointTest(pointEstimateFormat));
            pointEstimateFormat = String.format("D%sT%s", pointDev, pointTest).replace(".0", "");
        }
        return pointEstimateFormat;
	}

	@Override
	public void updatePointRemain(String pointRemain, long issueId) {
		String pattern = "\\d{1,2}(\\.5)?(\\.0+)?\\s\\d{1,2}(\\.5)?(\\.0+)?";
		String pointRemainActual = pointRemain.trim();
		Issue is = issueService.findIssueById(issueId);
		if(is.getPointFormat().equals("2")){
			pointRemainActual = String.format("D%sT%s", pointRemainActual, 0);
		}
		if(pointRemainActual.matches(pattern)){
			String[] str = pointRemainActual.split("\\s");
			pointRemainActual = String.format("D%sT%s", str[0], str[1]);
		}
		issueService.updatePointRemain(pointRemainActual, issueId);
	}

	@Override
	public void deleteAllPointRemainByIssue(Issue issue) {
		List<PointRemain>pointRemains = pointRemainDao.findPointRemainByIssueId(issue.getIssueId());
		if(pointRemains.size() > 0){
			for (PointRemain pointRemain : pointRemains) {
				pointRemainDao.delete(pointRemain);
			}
		}
		
	}
}
