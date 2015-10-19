package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Feedback;

public interface FeedbackDao {
    boolean saveFeedback(Feedback feedback);

    boolean updateFeedback(Feedback feedback);

    boolean deleteFeedback(Feedback feedback);

    List<Feedback> findFeedbacksBySprintIdAndProjectId(Long projectId, Long sprintId);
    List<Feedback> findFeedbacksByProjectId(long projectId);
}

