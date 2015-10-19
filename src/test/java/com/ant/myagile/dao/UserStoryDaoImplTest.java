package com.ant.myagile.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.ant.myagile.model.Project;
import com.ant.myagile.model.UserStory;

@ContextConfiguration(locations = {"classpath:test-context.xml"}, loader = ContextLoader.class)
public class UserStoryDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests{
	@Autowired
	protected UserStoryDao userStoryDao;
	@Autowired
	protected ProjectDao prDao;
	
	@Test
	public void testFindAllUserStoryByProjectId() {
		Project p = new Project();
		p.setProjectName("New Project");

		UserStory us1 = new UserStory();
		us1.setName("User Story 1");
		us1.setDescription("US1 Description");
		us1.setSortId(-1L);
		UserStory us2 = new UserStory();
		us2.setName("User Story 2");
		us2.setDescription("US2 Description");
		us2.setSortId(-2L);
		us1.setProject(p);
		us2.setProject(p);

		prDao.save(p);
		userStoryDao.create(us1);
		userStoryDao.create(us2);

		List<UserStory> list = new ArrayList<UserStory>();
		list.add(us2);
		list.add(us1);

		assertEquals(list, userStoryDao.findAllUserStoryByProjectId(p.getProjectId()));

	}

	@Test
	public void testCheckExistUserStory() {
		Project p = new Project();
		p.setProjectName("New Project Check Exist");
		prDao.save(p);

		UserStory us = new UserStory();
		us.setName("User Story Check Exist");
		us.setDescription("US Description");
		us.setSortId(-3L);
		us.setProject(p);
		userStoryDao.create(us);

		assertEquals(us, userStoryDao.checkExistUserStory("User Story Check Exist",
						p.getProjectId()));
	}
}
