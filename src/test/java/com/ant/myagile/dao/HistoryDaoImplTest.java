package com.ant.myagile.dao;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.History;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.impl.HistoryServiceImpl;
import com.ant.myagile.utils.Utils;

@ContextConfiguration(locations = {"classpath:test-context.xml"}, loader = ContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class HistoryDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {
	@InjectMocks
	private HistoryService historyService = new HistoryServiceImpl();
	@Autowired
	private HistoryDao historyDao;

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
	@Autowired
	private AttachmentDao attachmentDao;
	@Mock
	Utils mockUtils;
	 
	@Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ((HistoryServiceImpl) historyService).setHistoryDao(historyDao);
    	// Get member Sebi as logged in user
        long sebiId = 3;
        Member memberOwner = memberDao.findMemberById(sebiId);
        when(mockUtils.getLoggedInMember()).thenReturn(memberOwner);
    }
	
	@Test
	public void TestFindHistoryByContainer() {
	    List<History> histories = historyDao.findHistoryByContainer("Issue", (long) 94);
	    assertEquals(histories.size(), 3);
	    assertEquals(histories.get(0).getHistoryId().longValue(), (long) 3);
	}
	
	@Test
    public void testHistoryOfCreateProject() {      
    	Project project =new Project();
		project.setProjectName("New Project");
		projectDao.save(project);
		int countBefore = historyDao.findHistoryByContainer("Project",project.getProjectId()).size();
			
		historyService.saveHistoryForNewProject(project);
		int countAfter = historyDao.findHistoryByContainer("Project",project.getProjectId()).size();
		countBefore++;
		assertEquals(countBefore, countAfter);
	}
	
	@Test
    public void testHistoryOfCreateTeam() {	
		Team team =new Team();
		team.setTeamName("New Team");
		team.setMailGroup("newteam@gmail.com");
		teamDao.save(team);
		int countBefore = historyDao.findHistoryByContainer("Team",team.getTeamId()).size();
		
		historyService.saveHistoryForNewTeam(team);
		int countAfter = historyDao.findHistoryByContainer("Team",team.getTeamId()).size();
		countBefore++;
		assertEquals(countBefore, countAfter);
	}
	
	
	@Test
	public void testAttachForIssue() {
		String type = "ISSUE";
		long sebiId = 3;
        Member memberOwner = memberDao.findMemberById(sebiId);
        
		Attachment attachment = new Attachment();
		attachment.setAuthor(memberOwner);
		attachment.setContainerId(1L);
		attachment.setCreatedOn(new Date());
		attachment.setDiskFilename("file_20130506.doc");
		attachment.setFilename("file.doc");
		attachment.setTemp(false);
		attachmentDao.save(attachment);
		int countBefore = historyDao.findHistoryByContainer("Issue",1L).size();
		
		historyService.saveHistoryForAttachment(attachment, type);
		int countAfter = historyDao.findHistoryByContainer("Issue",1L).size();
		countBefore++;
		assertEquals(countBefore, countAfter);
	}
}
