package com.ant.myagile.dao;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ant.myagile.model.HistoryFieldChange;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.impl.HistoryServiceImpl;
import com.ant.myagile.utils.Utils;

@ContextConfiguration(locations = { "classpath:test-context.xml" }, loader = ContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class HistoryFieldChangeDaoImplTest extends
		AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private HistoryDao historyDao;
	@InjectMocks
	private HistoryService historyService = new HistoryServiceImpl();
	@Autowired
	private HistoryFieldChangeDao historyFieldChangeDao;
	@Autowired
	private Utils utils;
	@Autowired
	private MemberDao memberDao;
	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private TeamDao teamDao;
	@Mock
	Utils mockUtils;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		((HistoryServiceImpl) historyService).setHistoryDao(historyDao);
		// Get member Sebi as logged in user
		long sebiId = 3;
		Member memberOwner = memberDao.findMemberById(sebiId);
		when(mockUtils.getLoggedInMember()).thenReturn(memberOwner);
	}

	@Test
	public void testFindUpdateHistoryOfPointRemainByIssueId() {
		int issueId = 94;
		List<HistoryFieldChange> historyFieldChangeList = historyFieldChangeDao
				.findUpdateHistoryOfPointRemainByIssueId(issueId);
		assertEquals(2, historyFieldChangeList.size());
		assertEquals("D2T2", historyFieldChangeList.get(0).getNewValue());
	}

	@Test
	public void testHistoryOfUpdateProject() {
		int countEdit = 0;
		int temp = countEdit;

		Project project = projectDao.findById(22L);
		project.setProjectName("My Agile edited");
		this.utils.evict(project);
		Project oldProject = this.projectDao.findById(22L);
		countEdit = historyService.saveHistoryForUpdateProject(oldProject, project);
		temp++;
		assertEquals(countEdit, temp);
	}

	@Test
	public void testHistoryOfUpdateTeam() {
		int countEdit = 0;
		int temp = countEdit;

		Team team = teamDao.findTeamByTeamId(1L);
		team.setTeamName("Ant edit");
		this.utils.evict(team);
		Team oldTeam = this.teamDao.findTeamByTeamId(1L);
		countEdit = historyService.saveHistoryForUpdateTeam(oldTeam, team);
		
		temp++;
		assertEquals(countEdit, temp);
	}
}
