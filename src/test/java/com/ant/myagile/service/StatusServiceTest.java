package com.ant.myagile.service;

import static org.powermock.api.mockito.PowerMockito.spy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ant.myagile.dao.impl.StatusDaoImpl;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.impl.StatusServiceImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Status.class, Issue.class, Team.class})
public class StatusServiceTest {
	
	@Mock	
	private StatusDaoImpl statusDao;
	
	@InjectMocks
	private StatusServiceImpl statusService;
	
	@Mock
	private Team team;
	
	@Mock
	private Sprint sprint;
	
	private Status status;
	private Status status2;
	
	private static final long ID_1 = new Long(1L);
	private static final long ID_2 = new Long(2L);
	
	@Before
	public void setUp() throws Exception {
		
		status = spy(new Status());
		status.setStatusId(1L);
		status.setName("To do");
		status.setColor("#FFFFFF");
		status.setType(Status.StatusType.START);
		status.setSprint(sprint);
		
		status2 = spy(new Status());
		status2.setStatusId(1L);
		status2.setName("Done");
		status2.setColor("#FFFFFF");
		status.setType(Status.StatusType.DONE);
		status2.setSprint(sprint);
		
	}
	
	private final boolean checkStateResult(Status expected, Status actual){
		Assert.assertEquals(expected.getStatusId(), actual.getStatusId());
		Assert.assertEquals(expected.getName(), actual.getName());
		Assert.assertEquals(expected.getColor(), actual.getColor());
		Assert.assertEquals(expected.getType(), actual.getType());
		Assert.assertEquals(expected.getSprint(), actual.getSprint());
		return true;
	}
	
	@Test
	public void findStatusBySprint() {
		
		List<Status> expected = new LinkedList<Status>();
		expected.add(status);
		expected.add(status2);
		
		Mockito.when(statusDao.findStatusBySprintId(1L)).thenReturn(expected);
		List<Status> actual = statusService.findStatusBySprintId(1L);
		Mockito.verify(statusDao).findStatusBySprintId(1L);
		
		Assert.assertEquals(2, actual.size());
		Assert.assertTrue(checkStateResult(status,actual.get(0)));
		Assert.assertTrue(checkStateResult(status2,actual.get(1)));
	}
	
	@Test
	public void testUpdate(){
		Mockito.when(statusDao.update(Mockito.any(Status.class))).thenReturn(true);
		statusService.update(status);
		Mockito.verify(statusDao).update(status);
	}
	
	@Test
	public void testDelete(){
		Mockito.when(statusDao.delete(Mockito.any(Status.class))).thenReturn(true);
		statusService.delete(status);
		Mockito.verify(statusDao).delete(status);
	}
	
	@Test
	public void testSave(){
		Mockito.when(statusDao.save(Mockito.any(Status.class))).thenReturn(true);
		statusService.save(status);
		Mockito.verify(statusDao).save(status);
	}

	@Test
	public void testUpdateIssueBySprintAndTeam(){
		Mockito.doNothing().doThrow(new IllegalStateException()).when(statusDao).
                updateIssueBySprintId(Mockito.anyLong(), Mockito.anyLong());
		statusService.updateIssueBySprintId(ID_1, ID_2);
		Mockito.verify(statusDao).updateIssueBySprintId(ID_1, ID_2);
	}
	@Test
	public void testFindStatusById(){
		Status expected = status;
		Mockito.when(statusDao.findStatusById(Mockito.anyLong())).thenReturn(status);
		Status actual = statusService.findStatusById(ID_1);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
    public void testReuseKanbanSetting(){
	    Team team = new Team();
	    Sprint newSprint = new Sprint();
        Sprint oldSprint = new Sprint();

	    Status status1 = new Status();
        status1.setStatusId(1L);
        status1.setName("Column 1");
        status1.setColor("#FFFFFF");
        status1.setType(Status.StatusType.START);
        status1.setSprint(oldSprint);

        Status status2 = new Status();
        status2.setStatusId(1L);
        status2.setName("Column 2");
        status2.setColor("#FF57FF");
        status1.setType(Status.StatusType.START);
        status2.setSprint(oldSprint);

        Status status3 = new Status();
        status3.setStatusId(1L);
        status3.setName("Column 3");
        status3.setColor("#32FFFF");
        status1.setType(Status.StatusType.START);
        status3.setSprint(oldSprint);

        List<Status> oldStatus = new ArrayList<Status>();
        oldStatus.add(status1);
        oldStatus.add(status2);
        oldStatus.add(status3);
        oldSprint.setSprintId((long) 1);
        Mockito.when(statusService.findStatusBySprintId(Mockito.anyLong())).thenReturn(oldStatus);
        statusService.reuseKanbanSetting(team, newSprint, oldSprint);
        Mockito.verify(statusDao, Mockito.atLeast(3)).save((Status) Mockito.anyObject());
    }
}