package com.ant.myagile.service;

import static org.mockito.Mockito.when;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.MemberDao;
import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.impl.IssueServiceImpl;
import com.ant.myagile.utils.Utils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class UserStoryServiceTest extends TestCase {
	@Autowired
	UserStoryService usService;
	@Autowired
	StatusService statusService;
	@Autowired
	SprintService sprintService;
	

	@Mock
	Utils mockUtils;
	@InjectMocks
	IssueServiceImpl issueService=new IssueServiceImpl();
	
	
	@Autowired
	private MemberDao memberDao;

	@Autowired
	private SprintDao sprintDao;
		
	@Autowired
	private IssueDao issueDao;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		issueService.issueDao = issueDao;
	}

	@Test
	@Ignore
	public void testFindProgressOfUserStory(){
	    long  sprintId = 52;
        Sprint sprint0 = sprintDao.findSprintById(sprintId);
		//Get logging user
		when(mockUtils.getLoggedInMember()).thenReturn(null);
		
		//Create userStory
		UserStory	us=new UserStory();
		us.setDescription("Description");
		us.setName("Name");
		us.setSortId(100L);		
		usService.create(us);
		
		
		Issue issue = new Issue();
		issue.setEstimate("D20T0");
		issue.setSubject("123");
		issue.setRemain("D10T0");
		issue.setSprint(sprint0);
		issue.setDescription("Description");
		issue.setPriority("1");
		issue.setType("Bug");
		issue.setPointFormat("1");
		issue.setUserStory(us);
		
		//Get issue have been just add Database
		List<Issue> listIssue = issueService.findIssueBySprintId(sprintId);
		Long id = listIssue.get(listIssue.size() - 1).getIssueId();
		
		//Create new sprint with id is 100L and status
		Status status=new Status();
		status.setType(null);	
	
		Sprint sprint=new Sprint();
		sprint.setSprintId(100L);
		sprint.setSprintName("Sprint name");
		sprintService.save(sprint);
		
		status.setSprint(sprint);
		statusService.save(status);
		issue.setStatus(status);
		issueService.saveIssue(issue);
		
	//Check progress bar of issue don't have child
		assertEquals(usService.findProgressOfUserStory(us), 50);
	
		issue.setRemain("D3T0");
		issue.setEstimate("D10T0");
		issue.setParent(issueService.findIssueById(id));
		issueService.saveIssue(issue);		
		
		issue.setRemain("D0T0");
		issue.setEstimate("D5T0");		
		issueService.saveIssue(issue);
	//Check progress bar of issue has 2 childlren
		assertEquals(usService.findProgressOfUserStory(us), 63);

	}
	
	@Test
	@Ignore
	public void testFindStatusOfUserStory(){
		//Get logging user
		when(mockUtils.getLoggedInMember()).thenReturn(null);
	
		UserStory	us=new UserStory();
		us.setName("New User Story");
		us.setSortId(100L);		
		usService.create(us);
		
		assertEquals(usService.findStatusOfUserStory(us), "to-do");
		
		Issue issue1 = new Issue();
		issue1.setSubject("New Issue");
		
		Status status1=new Status();
		status1.setType(null);
		status1.setSprint(sprintService.findSprintById(52L));
		statusService.save(status1);
		issue1.setStatus(status1);
		issue1.setUserStory(us);
		issueService.saveIssue(issue1);	
		
		assertEquals(usService.findStatusOfUserStory(us), "in-progress");
		
		Issue issue2 = new Issue();
		issue2.setSubject("New Issue 2");
		
		Status status2=new Status();
		issue2.setUserStory(us);
		status2.setType(Status.StatusType.DONE);
		status2.setSprint(sprintService.findSprintById(52L));
		statusService.save(status2);
		issue2.setStatus(status2);
		issue2.setUserStory(us);
		issueService.saveIssue(issue2);
		
		assertEquals(usService.findStatusOfUserStory(us), "in-progress");
		
		status1.setType(Status.StatusType.DONE);
		statusService.update(status1);
		
		assertEquals(usService.findStatusOfUserStory(us), "done");

	}
}