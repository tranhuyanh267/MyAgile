package com.ant.myagile.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Sprint;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class IssueDaoImplTest {
	
	@Autowired
	private IssueDao issueDao;
	@Autowired
	private SprintDao sprintDao;
	
	@Autowired
	private MemberDao memberDao;
	
	
	@Test
	public void testFindIssueById() {
		long issueId = 94;
		Issue issue = issueDao.findIssueById(issueId);
		assertTrue(issue.getSprint().getSprintId() == 52);
	}
	
	@Test
	public void testFindBugBySprintId() {
		long sprintId = 52;
		List<Issue> lstBugs = issueDao.findBugBySprintId(sprintId);
		assertTrue(lstBugs.size() == 2);
		assertTrue(lstBugs.get(0).getIssueId() == 95);
	}
	
	@Test
	public void testFindIssueBySprintId() {
		long  sprintId = 52;
		List<Issue> lstIssues = issueDao.findIssueBySprintId(sprintId);
		assertEquals(lstIssues.size() , 4);
		assertTrue(lstIssues.get(2).getIssueId() == 94);
	}
	
	@Test
	@Transactional
	public void testSaveIssue() {
	    long  sprintId = 52;
	    Sprint sprint0 = sprintDao.findSprintById(sprintId);
		int size = issueDao.findIssueBySprintId(sprintId).size();
		Issue issue = new Issue();
		issue.setDescription("Test");
		issue.setSprint(sprint0);
		issue.setSubject("Test");
		issue.setType("Task");
		issue.setPriority("Must");
		issueDao.saveIssue(issue);
		int newSize = issueDao.findIssueBySprintId(sprintId).size();      
		assertEquals(newSize-size,1);
	}

}
