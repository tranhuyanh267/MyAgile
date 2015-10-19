package com.ant.myagile.service;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ant.myagile.dao.HistoryDao;
import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.MemberDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Member;
import com.ant.myagile.service.impl.HistoryServiceImpl;
import com.ant.myagile.utils.Utils;

@ContextConfiguration(locations = {"classpath:test-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class HistoryServiceImplTest {
    
    @Mock
    Utils mockUtils;
    
    @InjectMocks
	private HistoryService historyService = new HistoryServiceImpl();
	
	@Autowired
	private IssueDao issueDao;
	
	@Autowired
    private HistoryDao historyDao;
	
	@Autowired
    private MemberDao memberDao;
	
	@Autowired
	private PointRemainService pointRemainService;
	
	@Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ((HistoryServiceImpl) historyService).setHistoryDao(historyDao);
        ((HistoryServiceImpl) historyService).setPointRemainService(pointRemainService);
    }
	
	@Test
	public void TestSaveHistoryForIssue() {
	    Issue oldIssue = issueDao.findIssueById((long) 93);
	    Issue newIssue = issueDao.findIssueById((long) 93);
	    int numofchange;

	    // Get member Sebi as logged in user
        long sebiId = 3;
        Member memberOwner = memberDao.findMemberById(sebiId);
        when(mockUtils.getLoggedInMember()).thenReturn(memberOwner);

	    // Test with no change
	    numofchange = historyService.saveHistoryForIssue(oldIssue, newIssue);
	    assertEquals(0, numofchange);

	    // Test with 1 change
	    newIssue = issueDao.findIssueById((long) 93);
	    newIssue.setSubject("New subject");
        numofchange = historyService.saveHistoryForIssue(oldIssue, newIssue);
        assertEquals(1, numofchange);
        
        // Test with 1 change
        newIssue = issueDao.findIssueById((long) 93);
        newIssue.setRemain("D0T0");
        Member memberAssigned = memberDao.findMemberById((long) 4);
        newIssue.setAssigned(memberAssigned);
        numofchange = historyService.saveHistoryForIssue(oldIssue, newIssue);
        assertEquals(2, numofchange);
	}
	
	@Test
	public void TestFindHistoryByContainerAndSprintWithStartRowAndEndRow(){
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.SEPTEMBER, 2);
		Date dateStart = cal.getTime();
		cal.set(2013, Calendar.SEPTEMBER, 5, 23, 59);
		Date dateEnd = cal.getTime();
		List<Object> list = historyService.findIssueHistoryBySprintWithStartAndEndRow(52L, 0, 0, dateStart, dateEnd);
		int size = list.size();
		assertEquals(4, size);
	}
	
	@Test
	public void TestFindDateArrangeOfIssueHistoryBySprint(){
		List<Date> list = historyService.findDateRangeOfIssueHistoryBySprintId(52L);
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(2013, Calendar.SEPTEMBER, 3, 0, 0, 0);
		assertEquals(cal.getTimeInMillis(), list.get(0).getTime());
		cal.clear();
		cal.set(2013, Calendar.SEPTEMBER, 5, 0, 0, 0);
		assertEquals(cal.getTimeInMillis(), list.get(1).getTime());
	}
}
