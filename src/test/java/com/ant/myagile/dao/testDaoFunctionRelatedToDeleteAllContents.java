package com.ant.myagile.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.ant.myagile.model.Feedback;
import com.ant.myagile.model.LastUserStoryIndex;
import com.ant.myagile.model.RetrospectiveResult;
import com.ant.myagile.model.TeamProject;
import com.ant.myagile.service.ProjectService;

@ContextConfiguration(locations = { "classpath:test-context.xml" }, loader = ContextLoader.class)
public class testDaoFunctionRelatedToDeleteAllContents extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private FeedbackDao feedbackDao;
    
    @Autowired
    private LastUserStoryIndexDao lastUserStoryIndexDAO;
    
    @Autowired
    private RetrospectiveResultDao retrospectiveResultDao;
    
    @Autowired
    private TeamProjectDao teamProjectDao;
    
    @Autowired
    private ProjectService projectService;
    
    @Ignore
    @Test
    public void testFindFeedbackByProjectId() {
        List<Feedback> feedbacks = feedbackDao.findFeedbacksByProjectId(4000);
        assertEquals(feedbacks.size(), 2);
    }
    @Ignore
    @Test
    public void testFindLastUserStoryIndexByProjectId(){
        LastUserStoryIndex param = lastUserStoryIndexDAO.findLastUserStoryIndexByProjectId(4000);
        Assert.assertNotNull(param);
    }
    @Ignore
    @Test
    public void testdeleteLastUserStoryIndex(){
        LastUserStoryIndex param = lastUserStoryIndexDAO.findLastUserStoryIndexByProjectId(4000);
        boolean check = false;
        try{
            check = lastUserStoryIndexDAO.delete(param);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        Assert.assertTrue(check);
    }
    @Ignore
    @Test
    public void testFindRetrospectiveByProject(){
        List<RetrospectiveResult> list = retrospectiveResultDao.findRetrospectiveByProject(4000);
        Assert.assertEquals(2, list.size());
    }
    @Ignore
    @Test
    public void testDeleteTeamProjectDAO(){
        TeamProject teamProj = teamProjectDao.findTeamsProjectsByProjectId(4000).get(0);
        boolean check = teamProjectDao.delete(teamProj);
        Assert.assertTrue(check);
    }
    
    @Test
    public void testdeleteProjectAndAllContents(){
        boolean check = projectService.deleteProjectAndAllContents(4000);
        Assert.assertTrue(check);
    }
}
