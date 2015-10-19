package com.ant.myagile.dao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ant.myagile.model.Holiday;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.utils.Utils;

@ContextConfiguration(locations = {"classpath:test-context.xml"}, loader = ContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class HolidayDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private HolidayDao holidayDao;
	@Autowired
	private SprintDao sprintDao;
	@Autowired
	private Utils utils;
	@Autowired
	private MemberDao memberDao;
	@Mock
	Utils mockUtils;
	 
	@Before
    public void setUp() throws Exception {
    }
	
	
	@Test
    public void testFindHolidayByDateAndMemberInSprint() {      
		Calendar cal= Calendar.getInstance();
		cal.clear();
		cal.set(2013, Calendar.JULY, 23,00,00,00);
		Member member= memberDao.getMemberByUsername("sebastian.sussmann1@axonactive.vn");
		Sprint sprint=sprintDao.findSprintById((long)54);
		Holiday holiday=holidayDao.findHolidayByDateAndMemberInSprint(cal.getTime(), member, sprint);
		assertNotNull(holiday);
	}
	
	@Test
    public void testFindHolidayBySprint() {      
		Sprint sprint=sprintDao.findSprintById((long)54);
		List<Holiday> list=holidayDao.findHolidayBySprint(sprint);
		assertEquals((long)3, list.size());
	}
	
	@Test
    public void testFindHolidayByMemberInSprint() {     
		Member member= memberDao.getMemberByUsername("sebastian.sussmann1@axonactive.vn");
		Sprint sprint=sprintDao.findSprintById((long)54);
		List<Holiday> list=holidayDao.findHolidayByMemberInSprint(member, sprint);
		assertEquals((long)2, list.size());
	}
}
