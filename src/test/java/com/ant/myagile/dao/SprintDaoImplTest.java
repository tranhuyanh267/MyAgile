package com.ant.myagile.dao;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.ant.myagile.model.Sprint;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class SprintDaoImplTest {

    @Autowired
    private SprintDao sprintDao;

    @Test
    public void testFindSprintsByTeamId() {
        // Set team Beagle
        long teamId = 2;
        List<Sprint> listSprintOfBeagle = sprintDao.findSprintsByTeamId(teamId);
        assertTrue(listSprintOfBeagle.size() == 4);
        
        assertTrue(listSprintOfBeagle.get(0).getSprintId() == (long) 13);
        assertTrue(listSprintOfBeagle.get(3).getSprintId() == (long) 10);
    }

    @Test
    public void testFindSprintsByTeamIdArrangeByAsc() {
        // Set team Beagle
        long teamId = 2;

        List<Sprint> listSprintOfBeagle = sprintDao
                .findSprintsByTeamIdOrderByAsc(teamId);

        assertTrue(listSprintOfBeagle.size() == 4);
        assertTrue(listSprintOfBeagle.get(0).getSprintId() == (long) 10);
        assertTrue(listSprintOfBeagle.get(3).getSprintId() == (long) 13);
    }

    @Test
    public void testFindLastSprintByTeamId() {
        // Set team Beagle
        long teamId = 2;

        Sprint latestSprint = sprintDao
                .findLastSprintByTeamId(teamId);

        assertTrue(latestSprint.getSprintId() == (long) 13);
    }
}
