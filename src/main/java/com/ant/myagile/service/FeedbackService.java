package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Feedback;

public interface FeedbackService {
	boolean saveFeedback(Feedback feedback);
	boolean updateFeedback(Feedback feedback);
	boolean deleteFeedback(Feedback feedback); 
	
	/**
	 * Find feedback by sprint and project
	 * @param projectId
	 * @param sprintId
	 * @return Feedback List
	 */
	List<Feedback> findFeedbacksBySprintIdAndProjectId(Long projectId, Long sprintId);
	List<Feedback> findFeedbacksByProjectId(long projectId);
}
