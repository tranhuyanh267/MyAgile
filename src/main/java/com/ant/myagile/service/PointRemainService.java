package com.ant.myagile.service;

import java.util.Date;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Sprint;

public interface PointRemainService {
	 List<PointRemain> findPointRemainByIssueId(long issueId);

	 List<PointRemain> findPointRemainByIssueIdAndSprint(long issueId, Sprint s);

	 PointRemain findPointRemainByIssueIdAndNowDate(long issueId);

	 boolean save(PointRemain pointRemain);
	
	 boolean save(Issue issue, String pointRemain);

	 boolean update(PointRemain pointRemain);
	
	 List<Issue> findIssueDontUpdateByDateOfSprint(Date d,Sprint s) ;
	
	 void saveDataForFirstTimes(Date dateUpdate, Sprint sprint);
	
	 boolean delete(PointRemain pointRemain);

	/**
     * Check data format of remain point that user input
     * @param context
     * @param validate
     * @param value
     * @throws ValidatorException
     */
	 void checkPointRemain(FacesContext context, UIComponent validate, Object value, String pointFormat);

	/**
     * Check data format of estimate point that user input
     * @param context
     * @param validate
     * @param value
     * @throws ValidatorException
     */
	 void checkEstimatePoint(FacesContext context, UIComponent validate, Object value);

	/**
	 * Return real point remain base on format
	 * @param issue to get the format
	 * @return real point remain
	 */
     String getPointRemainByFormat(Issue issue);

    /**
     * Return real point estimate base on format
     * @param issue to get the format
     * @return real point remain
     */
     String getPointEstimateByFormat(Issue issue);
    

    /**
	 * Update PointRemain
	 * @param issueId
	 */
     void updatePointRemain(String pointRemain, long issueId);
     List<String> findHistoryOfPointRemainByIssueId(long issueId);
     
     void deleteAllPointRemainByIssue(Issue  issue);
}
