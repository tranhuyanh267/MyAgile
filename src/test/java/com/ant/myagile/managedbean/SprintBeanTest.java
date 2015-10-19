package com.ant.myagile.managedbean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ant.myagile.dao.MemberDao;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.Utils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
public class SprintBeanTest {

    @Mock
    Utils mockUtils;

    @Autowired
    private TeamService teamService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private MemberDao memberDao;

    @InjectMocks
    private SprintBean sprintBean = new SprintBean();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testGetSprintsByTeamId() {
        // Get member Sebi as logged in user
    }
}
