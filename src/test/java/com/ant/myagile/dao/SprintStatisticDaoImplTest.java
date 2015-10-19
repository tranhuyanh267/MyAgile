package com.ant.myagile.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import static org.junit.Assert.assertEquals;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.SprintStatistic;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class SprintStatisticDaoImplTest {
	
	@Autowired
	private SprintStatisticDao sprintStatisticDao;
	@Autowired
	private SprintDao sprintDao;
	
	@Test
	public void testFindSprintStatisticBySprint(){
		Sprint sprint = sprintDao.findSprintById(Long.parseLong("52"));
		SprintStatistic sta = sprintStatisticDao.findSprintStatisticBySprint(sprint);
		assertEquals(sprint.getSprintId(), sta.getSprint().getSprintId());
	}
	
	@Test
	public void testUpdate(){
		Sprint sprint = sprintDao.findSprintById(Long.parseLong("52"));
		SprintStatistic sta = sprintStatisticDao.findSprintStatisticBySprint(sprint);
		sta.setNote("JUnitTest");
		sprintStatisticDao.update(sta);
		SprintStatistic staAfterUpdate = sprintStatisticDao.findSprintStatisticBySprint(sprint);
		assertEquals("JUnitTest", staAfterUpdate.getNote());
	}
	
	@Test
	public void testSave(){
		Sprint sprint = sprintDao.findSprintById(Long.parseLong("53"));
		SprintStatistic sta = new SprintStatistic();
		sta.setSprint(sprint);
		sta.setNote("TestSave");
		sprintStatisticDao.save(sta);
		SprintStatistic statistic = sprintStatisticDao.findSprintStatisticBySprint(sprint);
		assertEquals("TestSave", statistic.getNote());
	}
	
	@Test
	public void testDelete(){
		Sprint sprint = sprintDao.findSprintById(Long.parseLong("53"));
		SprintStatistic statistic = sprintStatisticDao.findSprintStatisticBySprint(sprint);
		sprintStatisticDao.delete(statistic);
		SprintStatistic sta = sprintStatisticDao.findSprintStatisticBySprint(sprint);
		assertEquals(null, sta);
	}

}
