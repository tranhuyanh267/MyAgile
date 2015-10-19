package com.ant.myagile.service;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
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
import com.ant.myagile.dao.UserStoryDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.impl.HistoryServiceImpl;
import com.ant.myagile.service.impl.IssueServiceImpl;
import com.ant.myagile.utils.Utils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class IssueServiceTest extends TestCase {
	
	@InjectMocks
	IssueServiceImpl issueService=new IssueServiceImpl();

	@Autowired
	private MemberDao memberDao;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private PointRemainService pointRemainService;
	@Autowired
	private IssueService issueServiceAutowire;
	@Autowired
	private StatusService statusService;

	@Mock
	private HistoryServiceImpl historyService = new HistoryServiceImpl();

	@Mock
	Utils mockUtils;
	
	@Autowired
	private IssueDao issueDao;
	@Autowired
	private UserStoryDao userStoryDao;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		issueService.issueDao = issueDao;
		issueService.setSprintService(sprintService);
		issueService.setStatusService(statusService);
	}

	@Test
	public void testFindProgressOfIssue() {		
		when(mockUtils.getLoggedInMember()).thenReturn(null);
		long  sprintId = 53;
        Sprint sprint0 = sprintService.findSprintById(sprintId);
		Issue issue = new Issue();		
		issue.setEstimate("D10T0");
		issue.setSubject("123");
		issue.setRemain("D5T0");
		issue.setSprint(sprint0);
		issue.setDescription("Description");
		issue.setPriority("1");
		issue.setType("Bug");
		issue.setPointFormat("1");
		issueService.saveIssue(issue);
		List<Issue> listIssue = issueService.findIssueBySprintId(sprintId);

		Long id = listIssue.get(listIssue.size() - 1).getIssueId();
		//Check progress bar of issue don't have child
		assertEquals(issueService.findProgressOfIssue(id), 50);
		
		//Create a status
		Status status=new Status();
		status.setType(Status.StatusType.START);		
		statusService.save(status);
		
		
		issue.setRemain("D5T0");
		issue.setEstimate("D10T0");		
		issue.setStatus(status);		
		issue.setParent(issueService.findIssueById(id));
		issueService.saveIssue(issue);		
		issue.setRemain("D3T0");
		issue.setEstimate("D5T0");		
		issueService.saveIssue(issue);
		
		//Check progress bar of issue has 2 childlren
		assertEquals(issueService.findProgressOfIssue(id), 47);
	
	}
	
	@Test
	public void testCalculateProgress() {
		Issue issue = new Issue();
		issue.setEstimate("D8T2");
		issue.setRemain("D6T2");
		int expected = 20;		
		assertEquals(expected, issueService.calculateProgress(issue));
		
		issue.setRemain("D2T2");		
		expected = 60;		
		assertEquals(expected, issueService.calculateProgress(issue));
		
		issue.setRemain("D0T0");
		expected = 100;		
		assertEquals(expected, issueService.calculateProgress(issue));
		
		issue.setRemain("D5T2");
		issue.setEstimate("D0T0");
		expected = 0;		
		assertEquals(expected, issueService.calculateProgress(issue));
		
		Status st = new Status();
		st.setType(Status.StatusType.DONE);
		issue.setStatus(st);
		expected = 100;		
		assertEquals(expected, issueService.calculateProgress(issue));
	}
	
	@Test
	public void testCalculateTotalPoints() {
		List<Issue> issues = this.issueDao.findIssueBySprintId(52l);
		float totalPoints = this.issueService.calculateIssuesTotalPoints(issues);
		assertEquals(29.0f, totalPoints, 0.0);
	}

	@Test
	public void testGetStatusOfIssue(){
		Issue issue = new Issue();
		Status st = new Status();
		st.setType(Status.StatusType.START);
		issue.setStatus(st);
		String expected = "TODO";
		assertEquals(expected, issueService.findStatusOfIssue(issue));
		
		issue.getStatus().setType(Status.StatusType.DONE);
		expected = "DONE";
		assertEquals(expected, issueService.findStatusOfIssue(issue));
		
		issue.getStatus().setType(Status.StatusType.IN_PROGRESS);
		expected = "IN_PROGRESS";
		assertEquals(expected, issueService.findStatusOfIssue(issue));
	}
	
	@Test
	@Ignore
	public void testUpdateStatusOfIssueParent(){
		Issue issueParent = new Issue();
		issueParent.setSubject("ABC");
		Status st = new Status();
		st.setType(Status.StatusType.START);
		issueParent.setStatus(st);
		issueService.saveIssue(issueParent);
		
		Issue issueChild1 = new Issue();
		issueChild1.setSubject("ABC");
		issueChild1.setStatus(st);
		issueChild1.setParent(issueParent);
		issueService.saveIssue(issueChild1);
		
		Issue issueChild2 = new Issue();
		issueChild2.setSubject("ABC");
		issueChild2.setStatus(st);
		issueChild2.setParent(issueParent);
		issueService.saveIssue(issueChild2);
		
		issueService.updateStatusOfIssueParent(issueParent);
		
		assertTrue(issueParent.getStatus().getType() == Status.StatusType.START);
		
		
	}

	@Test
	public void testSaveIssue(){
	    long  sprintId = 53;
        Sprint sprint0 = sprintService.findSprintById(sprintId);
		int sizeAtBegin = issueService.findIssueBySprintId(sprintId).size();
		Issue issue = new Issue();
		issue.setSubject("Test Issue");
		issue.setSprint(sprint0);
		issueDao.saveIssue(issue);
		int sizeAtEnd = issueService.findIssueBySprintId(sprintId).size();
		assertEquals(sizeAtBegin+1, sizeAtEnd);
	}
	
	@Test
	public void testFilterIssue() {
	    long  sprintId = 52;
		String issueType = "Any";
		String issueStatus = "Any";
		String issuePriority = "Must";
		List<Issue> allIssues = issueService.findIssueBySprintId(sprintId);
		List<Issue> filteredIssue = issueService.filterIssues(allIssues, issueType, issueStatus, issuePriority);
		assertEquals(2, filteredIssue.size());
	}
	
	@Test
	public void testIsParentAndTotalChildrenPointEqual() {
		Issue parent = new Issue();
		parent.setSubject("Issue for testing only");
		parent.setEstimate("D0T0");
		issueDao.saveIssue(parent);
		
		assertEquals(true, issueService.isParentAndTotalChildrenPointEqual(parent));
		
		Issue child1 = new Issue();
		child1.setSubject("Child 1 for testing");
		child1.setEstimate("D0T0");
		child1.setParent(parent);
		issueDao.saveIssue(child1);
		assertEquals(true, issueService.isParentAndTotalChildrenPointEqual(parent));
		
		child1.setEstimate("D4T1");
		issueDao.updateIssue(child1);
		assertEquals(false, issueService.isParentAndTotalChildrenPointEqual(parent));
		
		parent.setEstimate("D4T1");
		issueDao.updateIssue(parent);
		assertEquals(true, issueService.isParentAndTotalChildrenPointEqual(parent));
		
		Issue child2 = new Issue();
		child2.setSubject("Child 2 for testing");
		child2.setEstimate("D2T2");
		child2.setParent(parent);
		issueDao.saveIssue(child2);
		assertEquals(false, issueService.isParentAndTotalChildrenPointEqual(parent));
		
		child2.setEstimate("D0T0");
		issueDao.updateIssue(child2);
		assertEquals(true, issueService.isParentAndTotalChildrenPointEqual(parent));
	}

	@Test
	public void testCalculateProgressWhenParentEqualChilds() {
		float totalRemainPointOfChildIssues = 6f;
		float totalPointOfParentIssue = 7f;
		boolean hasIssueWithPoint = true;
		int totalTaskWithDoneStatus = 6;
		int totalTask = 6;
		
		float result = Math.round(issueService.calculateProgressWhenParentEqualChilds(
				totalRemainPointOfChildIssues, totalPointOfParentIssue,
				hasIssueWithPoint, totalTaskWithDoneStatus, totalTask));
		
		assertEquals(14f, result);
	}
	
	@Test
	public void testCalculateProgressWhenParentNotEqualChilds() {
		float totalRemainPointOfChildIssues = 6f;
		float totalEstimatePointOfChildIssues = 8f;
		boolean hasIssueWithPoint = true;
		int totalTaskWithDoneStatus = 6;
		int totalTask = 6;
		
		float result = Math.round(this.issueService.calculateProgressWhenParentNotEqualChilds(
				totalRemainPointOfChildIssues, totalEstimatePointOfChildIssues,
				hasIssueWithPoint, totalTaskWithDoneStatus, totalTask));
		
		assertEquals(25f, result);
	}
	
	@Test
	public void testCalculateProgressForParentIssueTask() {
		Issue parent = new Issue();
		parent.setSubject("Issue for testing only");
		parent.setRemain("D0T0"); 
		parent.setEstimate("D6T3"); 
		issueDao.saveIssue(parent);
		
		Issue child1 = new Issue();
		child1.setSubject("Child 1 for testing");
		child1.setRemain("D3T1"); 
		child1.setEstimate("D4T1"); 
		child1.setParent(parent);
		issueDao.saveIssue(child1);
		
		Issue child2 = new Issue();
		child2.setSubject("Child 2 for testing");
		child2.setRemain("D1T1"); 
		child2.setEstimate("D2T1");
		child2.setParent(parent);
		issueDao.saveIssue(child2);
		
		float result = Math.round(this.issueService.calculateProgressForParentIssueTask(parent));
		assertEquals(25f, result);
		
		Issue parent2 = new Issue();
		parent2.setSubject("Issue for testing only");
		parent2.setRemain("D0T0");
		parent2.setEstimate("D5T2"); 
		issueDao.saveIssue(parent2);
		
		Issue child12 = new Issue();
		child12.setSubject("Child 1 for testing");
		child12.setRemain("D3T1");
		child12.setEstimate("D3T1");
		child12.setParent(parent2);
		issueDao.saveIssue(child12);
		
		Issue child22 = new Issue();
		child22.setSubject("Child 2 for testing");
		child22.setRemain("D1T1"); 
		child22.setEstimate("D2T1"); 
		child22.setParent(parent2);
		issueDao.saveIssue(child22);
		
		float result2 = Math.round(this.issueService.calculateProgressForParentIssueTask(parent2));
		assertEquals(14f, result2);
	}
		
	@Test
	public void testFindNoChidrenIssuesByUserStory(){
		UserStory userStory=new UserStory();
		userStory.setName("New User Story");
		userStory.setSortId(100L);		
		userStoryDao.create(userStory);
		
		//No child
		assertEquals(0, issueService.findNoChidrenIssuesByUserStory(userStory.getUserStoryId()).size());
		
		Issue parent = new Issue();
		parent.setSubject("Issue for testing only");
		parent.setRemain("D0T0"); 
		parent.setEstimate("D6T3");
		parent.setUserStory(userStory);
		issueDao.saveIssue(parent);
		
		//one child - no children
		assertEquals(1, issueService.findNoChidrenIssuesByUserStory(userStory.getUserStoryId()).size());
		
		Issue child1 = new Issue();
		child1.setSubject("Child 1 for testing");
		child1.setRemain("D3T1"); 
		child1.setEstimate("D4T1"); 
		child1.setParent(parent);
		issueDao.saveIssue(child1);
		
		Issue child2 = new Issue();
		child2.setSubject("Child 2 for testing");
		child2.setRemain("D1T1"); 
		child2.setEstimate("D2T1");
		child2.setParent(parent);
		issueDao.saveIssue(child2);
		
		//one child - two children
		assertEquals(2, issueService.findNoChidrenIssuesByUserStory(userStory.getUserStoryId()).size());
		
		Issue parent1 = new Issue();
		parent1.setSubject("Issue 1 for testing only");
		parent1.setRemain("D0T0"); 
		parent1.setEstimate("D6T3");
		parent1.setUserStory(userStory);
		issueDao.saveIssue(parent1);
		
		//two child - 1: two children, 2: no child
		assertEquals(3, issueService.findNoChidrenIssuesByUserStory(userStory.getUserStoryId()).size());
		
		Issue child3 = new Issue();
		child3.setSubject("Child 3 for testing");
		child3.setRemain("D1T1"); 
		child3.setEstimate("D2T1");
		child3.setParent(parent1);
		issueDao.saveIssue(child3);
		
		//two child - 1: two children, 2: one child
		assertEquals(3, issueService.findNoChidrenIssuesByUserStory(userStory.getUserStoryId()).size());
		
		Issue child4 = new Issue();
		child4.setSubject("Child 4 for testing");
		child4.setRemain("D1T1"); 
		child4.setEstimate("D2T1");
		child4.setParent(parent1);
		issueDao.saveIssue(child4);
		
		//two child - 1: two children, 2: two child
		assertEquals(4, issueService.findNoChidrenIssuesByUserStory(userStory.getUserStoryId()).size());
	}
	
	@Test
	public void testFindCurrentPointRemainOfIssue(){
		UserStory userStory=new UserStory();
		userStory.setName("New User Story");
		userStory.setSortId(100L);		
		userStoryDao.create(userStory);
		
		Issue issue = new Issue();
		issue.setSubject("Issue for testing only");
		issue.setRemain("D5T3"); 
		issue.setEstimate("D6T3");
		issue.setUserStory(userStory);
		issueDao.saveIssue(issue);
		
		//should return the original point when no point remain
		assertEquals("D5T3", issueServiceAutowire.findCurrentPointRemainOfIssue(issue));
		
		PointRemain point1 = new PointRemain();
		point1.setIssue(issue);
		point1.setDateUpdate(new Date());
		point1.setPointRemain("D4T3");
		pointRemainService.save(point1);
		
		PointRemain point2 = new PointRemain();
		point2.setIssue(issue);
		point2.setDateUpdate(new Date());
		point2.setPointRemain("D0T0");
		pointRemainService.save(point2);
		
		//should return the added point remain when add a point remain
		assertEquals("D0T0", issueServiceAutowire.findCurrentPointRemainOfIssue(issue));
		
		//tear down
		pointRemainService.delete(point1);
		pointRemainService.delete(point2);
	
	}
	
	@Test
	public void testFindPointRemainByIssueId(){
		
		UserStory userStory=new UserStory();
		userStory.setName("New User Story");
		userStory.setSortId(100L);		
		userStoryDao.create(userStory);
		
		Issue issue = new Issue();
		issue.setSubject("Issue for testing only");
		issue.setRemain("D5T3"); 
		issue.setEstimate("D6T3");
		issue.setUserStory(userStory);
		issueDao.saveIssue(issue);
		
		PointRemain point1 = new PointRemain();
		point1.setIssue(issue);
		point1.setDateUpdate(new Date());
		point1.setPointRemain("D4T3");
		pointRemainService.save(point1);
		
		PointRemain point2 = new PointRemain();
		point2.setIssue(issue);
		point2.setDateUpdate(new Date());
		point2.setPointRemain("D4T3");
		pointRemainService.save(point2);
		
		List<PointRemain> expectedList=new ArrayList<PointRemain>();
		expectedList.add(point1);
		expectedList.add(point2);
		
		List<PointRemain> actualList=new ArrayList<PointRemain>();
		actualList.addAll(pointRemainService.findPointRemainByIssueId(issue.getIssueId()));
	
		assertEquals(expectedList.size(),actualList.size());
		
		//return point sort by date desc
		assertEquals(point2.getPointRemainId(), actualList.get(0).getPointRemainId());
		
		//tearDown
		for(PointRemain pr: expectedList){
			pointRemainService.delete(pr);
		}
	}

	@Test
	public void testCopyCommonIssueInformation() {
	    Issue issue = issueDao.findIssueById(96l);
	    assertEquals("Sub userstory", issue.getSubject());
	    assertNotNull(issue.getSprint());
	    assertNotNull(issue.getStatus());
	    assertNotNull(issue.getAssigned());
	    assertEquals("D7T3", issue.getEstimate());

	    Issue copyIssue = issueService.copyCommonIssueInformation(issue);
	    assertEquals("Sub userstory", copyIssue.getSubject());
	    assertNull(copyIssue.getSprint());
	    assertNull(copyIssue.getStatus());
	    assertNull(copyIssue.getAssigned());
	    assertEquals("D0T0", copyIssue.getEstimate());
	}

	@Test
	public void testCopyIssueToSprint() {
	    int numOfIssueInSprint13Before = issueService.findIssueBySprintId(13l).size();
        issueService.copyIssueToSprint(96l, 13l);
        int numOfIssueInSprint13After = issueService.findIssueBySprintId(13l).size();
        assertEquals(numOfIssueInSprint13Before + 1, numOfIssueInSprint13After);
	}
}