package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ant.myagile.dao.FeedbackDao;
import com.ant.myagile.model.Feedback;
import com.ant.myagile.service.FeedbackService;

@Service("feedbackService")
public class FeedbackServiceImpl implements FeedbackService {

	@Autowired
	private FeedbackDao feedbackDao;
	
	@Override
	public boolean saveFeedback(Feedback feedback) {
		return feedbackDao.saveFeedback(feedback);
	}

	@Override
	public boolean updateFeedback(Feedback feedback) {
		return feedbackDao.updateFeedback(feedback);
	}

	@Override
	public boolean deleteFeedback(Feedback feedback) {
		return feedbackDao.deleteFeedback(feedback);
	}

	@Override
	public List<Feedback> findFeedbacksBySprintIdAndProjectId(Long projectId,
			Long sprintId) {
		return this.feedbackDao.findFeedbacksBySprintIdAndProjectId(projectId, sprintId);
	}

    @Override
    public List<Feedback> findFeedbacksByProjectId(long projectId) {
        return this.feedbackDao.findFeedbacksByProjectId(projectId);
    }

}
