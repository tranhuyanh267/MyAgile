package com.ant.myagile.service;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.ant.myagile.dao.HistoryDao;
import com.ant.myagile.model.HistoryFieldChange;

@ContextConfiguration(locations = {"classpath:test-context.xml"}, loader = ContextLoader.class)
public class HistoryFieldChangeServiceImpl extends AbstractTransactionalJUnit4SpringContextTests {
	
	@Autowired
	private HistoryFieldChangeService historyFieldChangeService;
	@Autowired
	private HistoryDao historyDao;
	@Test
	public void testFindUpdateHistoryOfPointRemainByIssueId() {
		int issueId = 94;
		List<HistoryFieldChange> historyFieldChangeList = historyFieldChangeService.findUpdateHistoryOfPointRemainByIssueId(issueId);
		assertEquals(2, historyFieldChangeList.size());
		assertEquals("D2T2", historyFieldChangeList.get(0).getNewValue());
	}
	
	@Test
	public void testFindUpdateHistoryOfProject() {
		long projectId = 22;
		List<HistoryFieldChange> historyFieldChangeList = historyFieldChangeService.findUpdateHistoryOfProject(projectId,"Project Name");
		assertEquals(1, historyFieldChangeList.size());
		assertEquals("My Agile edited", historyFieldChangeList.get(0).getNewValue());
	}
	
	@Test
	public void testFindUpdateHistoryOfTeam() {
		long teamId = 1;
		List<HistoryFieldChange> historyFieldChangeList = historyFieldChangeService.findUpdateHistoryOfTeam(teamId,"Team Name");
		assertEquals(1, historyFieldChangeList.size());
		assertEquals("Ant edited", historyFieldChangeList.get(0).getNewValue());
	}
}
