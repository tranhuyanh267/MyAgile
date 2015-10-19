package com.ant.myagile.dao;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.ant.myagile.model.Status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class StatusDaoImplTest {

    @Autowired
    private StatusDao statusDao;

    @Test
    public void testfindToDoStatus() {
        // Set sprint Ant.Sprint-0
    	long sprintID =52;
    	Status status= statusDao.findStatusStartBySprintId(sprintID);
    	assertTrue(status.getSprint().getSprintId() == sprintID);
    	assertTrue(status.getType() == Status.StatusType.START);
    }
    
    @Test
    public void testFindStatusBySprint() {
        // Set sprint Ant.Sprint-0
    	long sprintId =52;
    	List<Status> statusOfSprint52= statusDao.findStatusBySprintId(sprintId);
    	assertTrue(statusOfSprint52.size() == 5);
    	assertTrue(statusOfSprint52.get(0).getType() == Status.StatusType.START);
    	assertTrue(statusOfSprint52.get(3).getType() == Status.StatusType.DONE);
    	assertTrue(statusOfSprint52.get(4).getType() == Status.StatusType.ACCEPTED_HIDE);
    }
}
