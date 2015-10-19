package com.ant.myagile.dao;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.ant.myagile.model.Feedback;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;

@ContextConfiguration(locations = {"classpath:test-context.xml"}, loader = ContextLoader.class)
public class FeedbackDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	@Autowired
	private ProjectDao projectDao;
	
	@Autowired
	private FeedbackDao feedbackDao;
	
	@Autowired
	private SprintDao sprintDao;
	
	@Test
	public void testFindFeedbacksBySprintIdAndProjectId() {
		
		Project project = projectDao.findById(22L);
		Sprint sprint = sprintDao.findSprintById(52L);
		
		Feedback feedback = new Feedback();
		feedback.setCreatedDate(new Date());
		feedback.setFeedbackContent("Test ABC");
		feedback.setProject(project);
		feedback.setMember(project.getOwner());
		feedback.setSprint(sprint);
		feedbackDao.saveFeedback(feedback);
		
		List<Feedback> feedbacks = feedbackDao.findFeedbacksBySprintIdAndProjectId(22L, 52L);
		assertEquals(feedbacks.size(), 1);
		assertEquals(feedbacks.get(0).getFeedbackContent(), "Test ABC");
	}
	
}
