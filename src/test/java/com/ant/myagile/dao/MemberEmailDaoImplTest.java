package com.ant.myagile.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberEmail;

@ContextConfiguration(locations = { "classpath:test-context.xml" }, loader = ContextLoader.class)
public class MemberEmailDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	private static final Logger LOGGER = Logger.getLogger(MemberEmailDaoImplTest.class);

	@Autowired
	private MemberEmailDao memberEmailDao;

	@Autowired
	private MemberDao memberDao;

	private Member haveManyEmailMember;
	private Member noManyEmailMember;
	MemberEmail email1;
	MemberEmail email2;
	MemberEmail email3;

	private List<MemberEmail> memberEmailList;

	public MemberEmailDaoImplTest() {
	}

	@Test
	public void testFindEmailByMemberWithNullMember() {
		memberEmailList.addAll(memberEmailDao.findMemberEmailByMember(null));
		assertEquals(0, memberEmailList.size());
	}

	@Test
	public void testFindEmailByMemberWithMemberNotHaveMoreEmail() {
		memberEmailList.addAll(memberEmailDao.findMemberEmailByMember(noManyEmailMember));
		assertEquals(0, memberEmailList.size());
	}
	
	@Test
	public void testFindEmailByMemberWithMemberHaveManyEmail() {
		memberEmailList.addAll(memberEmailDao.findMemberEmailByMember(haveManyEmailMember));
		String[] actual = { memberEmailList.size()+"",
							memberEmailList.get(0).getEmail(),
							memberEmailList.get(1).getEmail(),
							memberEmailList.get(2).getEmail()};
		String[] expected = {"3","vnnvanhuong@gmail.com","van_huong1491@yahoo.com.vn","nvhuong406@student.ctu.edu.vn"};
		for(int i = 0; i < expected.length; i++){
			assertEquals(expected[i], actual[i]);
		}

	}
	
	@Test
	public void testFindMemberEmailById(){
		MemberEmail[] actual = {memberEmailDao.findMemberEmailById(null),
								memberEmailDao.findMemberEmailById(email3.getEmailId()+1),
								memberEmailDao.findMemberEmailById(email2.getEmailId()), 
								memberEmailDao.findMemberEmailById(email1.getEmailId())};
		MemberEmail[] expected = {null, null, email2, email1};
		for(int i = 0; i< expected.length; i++){
			assertEquals(expected[i], actual[i]);
		}
	}
	
	@Test
	public void testFindMemberEmailByToken(){
		String token = UUID.randomUUID().toString();
		token = token.replace("-", "");
		email1.setToken(token);
		memberEmailDao.update(email1);
		MemberEmail[] actual = {memberEmailDao.findMemberEmailByToken(null), 
								memberEmailDao.findMemberEmailByToken(token+"123"), 
								memberEmailDao.findMemberEmailByToken(token)};
		MemberEmail[] expected = {null, null, email1};
		for(int i = 0; i< expected.length; i++){
			assertEquals(expected[i], actual[i]);
		}
	}
	
	@Test
	public void testFindMemberEmailByEmail(){
		MemberEmail[] expected = {null, null, email1};
		MemberEmail[] actual = {memberEmailDao.findMemberEmailByEmail("mailNotExists@mail.com"), 
				memberEmailDao.findMemberEmailByEmail(null), 
				memberEmailDao.findMemberEmailByEmail(email1.getEmail())};
		for(int i = 0; i< expected.length; i++){
			assertEquals(expected[i], actual[i]);
		}
	}

	@Before
	public void setUp() throws Exception {
		haveManyEmailMember = new Member();
		haveManyEmailMember.setAccountExpired(false);
		haveManyEmailMember.setAccountLocked(false);
		haveManyEmailMember.setEnabled(true);
		haveManyEmailMember.setActive(true);
		haveManyEmailMember.setPasswordExpired(false);
		haveManyEmailMember.setUsername("huong.nguyenvan@axonactive.vn");
		haveManyEmailMember.setFirstName("Huong");
		if (memberDao.save(haveManyEmailMember)) {
			LOGGER.info("member " + haveManyEmailMember.getMemberId() + " is created");
		} else {
			LOGGER.warn("member " + haveManyEmailMember.getMemberId() + " is not created");
		}

		email1 = new MemberEmail();
		email1.setEmail("vnnvanhuong@gmail.com");
		email1.setMember(haveManyEmailMember);
		email1.setActive(true);
		if (memberEmailDao.create(email1)) {
			LOGGER.info("email " + email1.getEmailId() + " is created");
		} else {
			LOGGER.warn("email " + email1.getEmailId() + " is not created");
		}

		email2 = new MemberEmail();
		email2.setEmail("van_huong1491@yahoo.com.vn");
		email2.setMember(haveManyEmailMember);
		email2.setActive(true);
		if (memberEmailDao.create(email2)) {
			LOGGER.info("email " + email2.getEmailId() + " is created");
		} else {
			LOGGER.warn("email " + email2.getEmailId() + " is not created");
		}

		email3 = new MemberEmail();
		email3.setEmail("nvhuong406@student.ctu.edu.vn");
		email3.setMember(haveManyEmailMember);
		email3.setActive(false);
		if (memberEmailDao.create(email3)) {
			LOGGER.info("email " + email3.getEmailId() + " is created");
		} else {
			LOGGER.warn("email " + email3.getEmailId() + " is not created");
		}

		noManyEmailMember = new Member();
		noManyEmailMember.setAccountExpired(false);
		noManyEmailMember.setAccountLocked(false);
		noManyEmailMember.setEnabled(true);
		noManyEmailMember.setActive(true);
		noManyEmailMember.setPasswordExpired(false);
		noManyEmailMember.setUsername("khang.le@axonactive.vn");
		noManyEmailMember.setFirstName("Khang");
		if (memberDao.save(noManyEmailMember)) {
			LOGGER.info("member " + noManyEmailMember.getMemberId() + " is created");
		} else {
			LOGGER.warn("member " + noManyEmailMember.getMemberId() + " is not created");
		}

		memberEmailList = new ArrayList<MemberEmail>();
		LOGGER.info("init member email list done");

	}

	@After
	public void tearDown() throws Exception {
		if (memberDao.delete(haveManyEmailMember)) {
			LOGGER.info("member " + haveManyEmailMember.getMemberId() + " is deleted");
		} else {
			LOGGER.warn("member " + haveManyEmailMember.getMemberId() + " is not deleted");
		}

		if (memberDao.delete(noManyEmailMember)) {
			LOGGER.info("member " + noManyEmailMember.getMemberId() + " is deleted");
		} else {
			LOGGER.warn("member " + noManyEmailMember.getMemberId() + " is not deleted");
		}

		if (memberEmailDao.delete(email1)) {
			LOGGER.info("email " + email1.getEmailId() + " is deleted");
		} else {
			LOGGER.warn("email " + email1.getEmailId() + " is not deleted");
		}

		if (memberEmailDao.delete(email2)) {
			LOGGER.info("email " + email2.getEmailId() + " is deleted");
		} else {
			LOGGER.warn("email " + email2.getEmailId() + " is not deleted");
		}

		if (memberEmailDao.delete(email3)) {
			LOGGER.info("email " + email3.getEmailId() + " is deleted");
		} else {
			LOGGER.warn("email " + email3.getEmailId() + " is not deleted");
		}
		memberEmailList.clear();
		LOGGER.info("clear member email list done");

	}

}
