package com.ant.myagile.service;

import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ant.myagile.dao.impl.HistoryDaoImpl;
import com.ant.myagile.dao.impl.IssueDaoImpl;
import com.ant.myagile.dao.impl.PointRemainDaoImpl;
import com.ant.myagile.model.History;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.impl.HistoryServiceImpl;
import com.ant.myagile.service.impl.IssueServiceImpl;
import com.ant.myagile.service.impl.PointRemainServiceImpl;
import com.ant.myagile.utils.Utils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PointRemain.class, Issue.class})
public class PointRemainImplTest {

	@InjectMocks
	private PointRemainServiceImpl pointRemainService;
	
	@InjectMocks
	private IssueServiceImpl issueService;
	
	@Mock
	private HistoryServiceImpl historyService;
	
	@Mock
	private PointRemainServiceImpl pointRemainService2;
	
	
	@Mock
	private PointRemainDaoImpl pointRemainDao;
	@Mock
	private IssueDaoImpl issueDao;
	@Mock
	private HistoryDaoImpl historyDao;
	
	@Mock
	private Member member;
	@Mock
	private Utils utils;
	@Mock
	private UserStory userStory;
	@Mock
	private Issue issue;
	@Mock
	private Status status;
	@Mock
	private Sprint sprint;
	@Mock
	private List<History> histories;
	@Mock
	private List<PointRemain> pointRemains;
		
	private Issue issue1;
	private Issue issue2;
	private PointRemain pointRemain1;
	private PointRemain pointRemain2;
	private Calendar calendar;
		
	@Before
	public void setUp() throws Exception {
		calendar = Calendar.getInstance();
        calendar.set(2013, 7, 9, 0, 0, 0);
        issue1 = spy(new Issue());
		issue1.setAssigned(member);
		issue1.setDescription("Test1");
		issue1.setEstimate("D5T5");
		issue1.setIssueId(1L);
		issue1.setParent(null);
		issue1.setPriority("should");
		issue1.setRemain("D3T2");
		issue1.setSprint(sprint);
		issue1.setPointFormat("1");
		issue1.setStatus(status);
		issue1.setSubject("test1");
		issue1.setType("");
		issue1.setUserStory(userStory);
		//issue1.setHistories(histories);
		issue1.setPointRemains(pointRemains);
		
		issue2 = spy(new Issue());
		issue2.setAssigned(member);
		issue2.setDescription("Test2");
		issue2.setEstimate("D5T5");
		issue2.setIssueId(2L);
		issue2.setParent(null);
		issue2.setPriority("should");
		issue1.setPointFormat("2");
		issue2.setRemain("D3T2");
		issue2.setSprint(sprint);
		issue2.setStatus(status);
		issue2.setSubject("test2");
		issue2.setType("");
		issue2.setUserStory(userStory);
		//issue2.setHistories(histories);
		issue2.setPointRemains(pointRemains);
		
		pointRemain1 = spy(new PointRemain());
		pointRemain1.setDateUpdate(calendar.getTime());
		pointRemain1.setIssue(issue1);
		pointRemain1.setPointRemain("D1T1");
		pointRemain1.setPointRemainId(1L);
		
		pointRemain2 = spy(new PointRemain());
		pointRemain2.setDateUpdate(calendar.getTime());
		pointRemain2.setIssue(issue1);
		pointRemain2.setPointRemain("D1T1");
		pointRemain2.setPointRemainId(2L);
		
	}

	@Test
	public void testSaveDataForFirstTimes() {
		List<Issue> is = new LinkedList<Issue>();
		is.add(issue1);
		is.add(issue2);
		
		List<PointRemain> p = new LinkedList<PointRemain>();
		p.add(pointRemain1);
		p.add(pointRemain2);
		
		Mockito.when(pointRemainDao.findAllIssueByDate(calendar.getTime())).thenReturn(p);
		Mockito.when(issueDao.findIssuesIsTaskBySprintId(sprint.getSprintId())).thenReturn(is);
		
		List<Issue> is3 = pointRemainService.findIssueDontUpdateByDateOfSprint(calendar.getTime(), sprint);
		PointRemain pointRemain = spy(new PointRemain());
		pointRemain.setDateUpdate(calendar.getTime());
		pointRemain.setIssue(is3.get(0));
		pointRemain.setPointRemain(is3.get(0).getRemain());
		pointRemain.setPointRemainId(1L);
		
		Mockito.when(pointRemainDao.save(pointRemain)).thenReturn(true);
		pointRemainService.save(pointRemain);
			
		pointRemainService.saveDataForFirstTimes(calendar.getTime(), sprint);
		
		Mockito.verify(pointRemainDao, Mockito.times(2)).findAllIssueByDate(calendar.getTime());
		Mockito.verify(issueDao, Mockito.times(2)).findIssuesIsTaskBySprintId(sprint.getSprintId());
		Mockito.verify(pointRemainDao).save(pointRemain);
	}
	
	@Test
	public void testSave(){
		PointRemain pointRemainObj = spy(new PointRemain());
		pointRemainObj.setDateUpdate(calendar.getTime());
		pointRemainObj.setIssue(issue);
		pointRemainObj.setPointRemain("D3T3");
		Mockito.when(pointRemainDao.save(pointRemainObj)).thenReturn(true);
		pointRemainService.save(pointRemainObj);
		Mockito.verify(pointRemainDao).save(pointRemainObj);
	}
	
	@Test
	public void testUpdatePointRemainNotEqualsNull(){
		Mockito.when(issueDao.findIssueById(1L)).thenReturn(issue1);
		Issue is = issueService.findIssueById(1L);
		is.setRemain("D5T5");
		Mockito.when(issueDao.updateIssue(is)).thenReturn(true);
		
		Mockito.when(issueDao.findIssueById(2L)).thenReturn(issue2);
		Issue isOld = issueService.findIssueById(2L);
		
		Mockito.when(historyService.saveHistoryForIssue(isOld, issue1)).thenReturn(1);
		Mockito.doNothing().doThrow(new IllegalStateException()).when(utils).evict(isOld);
		issueService.updateIssue(is);
		Mockito.when(pointRemainService2.findPointRemainByIssueIdAndNowDate(1L)).thenReturn(pointRemain1);
		Mockito.when(pointRemainDao.save(pointRemain1)).thenReturn(true);
		issueService.updatePointRemain("D5T5",1L);
		Mockito.verify(issueDao, Mockito.times(4)).findIssueById(1L);
		Mockito.verify(issueDao, Mockito.times(1)).findIssueById(2L);
		Mockito.verify(issueDao, Mockito.times(2)).updateIssue(is);
		Mockito.verify(pointRemainService2, Mockito.times(1)).update(pointRemain1);
	}
	
	@Test
	public void testUpdatePointRemainEqualsNull(){
		Mockito.when(issueDao.findIssueById(1L)).thenReturn(issue1);
		Issue is = issueService.findIssueById(1L);
		is.setRemain("D5T5");
		Mockito.when(issueDao.updateIssue(is)).thenReturn(true);
		
		Mockito.when(issueDao.findIssueById(2L)).thenReturn(issue2);
		Issue isOld = issueService.findIssueById(2L);
		
		Mockito.when(historyService.saveHistoryForIssue(isOld, issue1)).thenReturn(1);
		Mockito.doNothing().doThrow(new IllegalStateException()).when(utils).evict(isOld);
		issueService.updateIssue(is);
		Mockito.when(pointRemainService2.findPointRemainByIssueIdAndNowDate(1L)).thenReturn(null);
		Mockito.when(pointRemainService2.save(is, "D5T5")).thenReturn(true);
		issueService.updatePointRemain("D5T5",1L);
		Mockito.verify(issueDao, Mockito.times(4)).findIssueById(1L);
		Mockito.verify(issueDao, Mockito.times(1)).findIssueById(2L);
		Mockito.verify(issueDao, Mockito.times(2)).updateIssue(is);
		Mockito.verify(pointRemainService2).save(is,"D5T5");
		
	}
	@Test
	public void testDelete(){
		Mockito.when(pointRemainDao.delete(Mockito.any(PointRemain.class))).thenReturn(true);
		pointRemainService.delete(pointRemain1);
		Mockito.verify(pointRemainDao).delete(pointRemain1);
	}
}
