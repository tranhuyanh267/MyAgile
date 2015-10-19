package com.ant.myagile.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.ant.myagile.dao.SprintDao;
import com.ant.myagile.model.Sprint;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class SprintServiceImplTest {

	@Autowired
	private SprintService sprintService;
	
	@Autowired
	private SprintDao sprintDao;

	/*
	Sprint 10 start    '2013-08-01 00:00:00', end  '2013-08-10 00:00:00'
	Sprint 11          '2013-08-11 00:00:00',      '2013-08-20 00:00:00'
	Sprint 12          '2013-08-21 00:00:00',      '2013-08-31 00:00:00'
	Sprint 13          '2013-08-31 00:00:00',      '2013-09-09 00:00:00'
	*/
	
	@Test
	public void testSprintDateValidate() {
	    // Set team Beagle
        long teamId = 2;
        
        Map<String, String> mapValid = new HashMap<String, String>();
        
        /**
         * Test edit mode
         */
        //Edit invalid date start
        long sprintId = 11;
        Sprint editSprint = sprintDao.findSprintById(sprintId);
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, Calendar.AUGUST, 9, 0, 0, 0);
        editSprint.setDateStart(calendar.getTime());
        mapValid = sprintService.validateSprintDate(teamId, editSprint);
        
        assertTrue(mapValid.containsKey("dateStartEdit"));
        assertFalse(mapValid.containsKey("dateEndEdit"));
	    assertEquals("The start date is not later than 08/10/2013", mapValid.get("dateStartEdit"));
	    
	    
	    //Edit invalid date end
        sprintId = 12;
        editSprint = sprintDao.findSprintById(sprintId);
        calendar.set(2013, 8, 1, 0, 0, 0);
        editSprint.setDateEnd(calendar.getTime());
        mapValid = sprintService.validateSprintDate(teamId, editSprint);
        
        assertTrue(mapValid.containsKey("dateEndEdit"));
        assertFalse(mapValid.containsKey("dateStartEdit"));
        assertEquals("The end date is not earlier than 08/31/2013", mapValid.get("dateEndEdit"));

        /**
         * Test new mode
         */
        //New invalid date start
        Sprint newSprint = new Sprint();
        newSprint.setSprintName("New name");
        calendar.set(2013, 8, 8, 0, 0, 0);
        newSprint.setDateStart(calendar.getTime());
        calendar.set(2013, 8, 15, 0, 0, 0);
        newSprint.setDateEnd(calendar.getTime());
        mapValid = sprintService.validateSprintDate(teamId, newSprint);
        assertTrue(mapValid.containsKey("dateStart"));
        assertFalse(mapValid.containsKey("dateEnd"));
        assertEquals("The start date is not later than 09/09/2013", mapValid.get("dateStart"));

        //New invalid date start
        //Invalid date end
        newSprint = new Sprint();
        newSprint.setSprintName("New name");
        calendar.set(2013, 8, 9, 0, 0, 0);
        newSprint.setDateStart(calendar.getTime());
        calendar.set(2013, 8, 8, 0, 0, 0);
        newSprint.setDateEnd(calendar.getTime());
        mapValid = sprintService.validateSprintDate(teamId, newSprint);
        assertFalse(mapValid.containsKey("dateStart"));
        assertTrue(mapValid.containsKey("dateEnd"));
        assertEquals("The end date is not earlier than 09/09/2013", mapValid.get("dateEnd"));
	}
}
