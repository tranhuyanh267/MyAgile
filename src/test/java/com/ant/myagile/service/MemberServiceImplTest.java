package com.ant.myagile.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberEmail;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
public class MemberServiceImplTest {
	private static final Logger LOGGER = Logger.getLogger(MemberServiceImplTest.class);
	
	@Autowired
	MemberService memberService;
	
	@Autowired
	private MemberEmailService memberEmailService;
	
	private Member haveManyEmailMember;
	private Member noManyEmailMember;
	MemberEmail email1;
	MemberEmail email2;
	MemberEmail email3;
	private List<MemberEmail> memberEmailList;
	
	@Test
	public void testFindDevelopmentMembersByTeamId() {
		long teamId = 1;
		List<Member> memberList = memberService.findDevelopmentMembersByTeamId(teamId);
		assertNotNull(memberList);
		assertTrue((memberList.size() >= 1));
	}
	
	@Test
	public void testFindDevelopmentMembersByTeamIdWithExclude() {
		long teamId = 1;
		long exludeId = 10;
		List<Member> memberList = memberService.findDevelopmentMembersByTeamId(teamId,exludeId);
		assertNotNull(memberList);
		assertTrue((memberList.size() >= 1));
		for(int i = 0;i<memberList.size();i++) {
			assertFalse((memberList.get(i).getMemberId() == exludeId));
		}
	}
	
	@Test
	public void testFindMemberByAlternateEmail() {
		Member[] expected = {null, haveManyEmailMember,null,null};
		Member[] actual = {null,
				memberService.findMemberByAlternateEmail("vnnvanhuong@gmail.com"),//correct
				memberService.findMemberByAlternateEmail("mailNotExists@mail.com"),//not exist
				memberService.findMemberByAlternateEmail("nvhuong406@student.ctu.edu.vn")};//not active
		for(int i = 0; i< expected.length; i++){
			assertEquals(expected[i], actual[i]);
		}
	}
	
	// run with each test function runs
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
			if (memberService.save(haveManyEmailMember)) {
				LOGGER.info("member " + haveManyEmailMember.getMemberId() + " is created");
			} else {
				LOGGER.warn("member " + haveManyEmailMember.getMemberId() + " is not created");
			}

			email1 = new MemberEmail();
			email1.setEmail("vnnvanhuong@gmail.com");
			email1.setMember(haveManyEmailMember);
			email1.setActive(true);
			if (memberEmailService.create(email1)) {
				LOGGER.info("email " + email1.getEmailId() + " is created");
			} else {
				LOGGER.warn("email " + email1.getEmailId() + " is not created");
			}

			email2 = new MemberEmail();
			email2.setEmail("van_huong1491@yahoo.com.vn");
			email2.setMember(haveManyEmailMember);
			email2.setActive(true);
			if (memberEmailService.create(email2)) {
				LOGGER.info("email " + email2.getEmailId() + " is created");
			} else {
				LOGGER.warn("email " + email2.getEmailId() + " is not created");
			}

			email3 = new MemberEmail();
			email3.setEmail("nvhuong406@student.ctu.edu.vn");
			email3.setMember(haveManyEmailMember);
			email3.setActive(false);
			if (memberEmailService.create(email3)) {
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
			if (memberService.save(noManyEmailMember)) {
				LOGGER.info("member " + noManyEmailMember.getMemberId() + " is created");
			} else {
				LOGGER.warn("member " + noManyEmailMember.getMemberId() + " is not created");
			}

			memberEmailList = new ArrayList<MemberEmail>();
			LOGGER.info("init member email list done");

		}

		@After
		public void tearDown() throws Exception {
			if (memberService.delete(haveManyEmailMember)) {
				LOGGER.info("member " + haveManyEmailMember.getMemberId() + " is deleted");
			} else {
				LOGGER.warn("member " + haveManyEmailMember.getMemberId() + " is not deleted");
			}

			if (memberService.delete(noManyEmailMember)) {
				LOGGER.info("member " + noManyEmailMember.getMemberId() + " is deleted");
			} else {
				LOGGER.warn("member " + noManyEmailMember.getMemberId() + " is not deleted");
			}

			if (memberEmailService.delete(email1)) {
				LOGGER.info("email " + email1.getEmailId() + " is deleted");
			} else {
				LOGGER.warn("email " + email1.getEmailId() + " is not deleted");
			}

			if (memberEmailService.delete(email2)) {
				LOGGER.info("email " + email2.getEmailId() + " is deleted");
			} else {
				LOGGER.warn("email " + email2.getEmailId() + " is not deleted");
			}

			if (memberEmailService.delete(email3)) {
				LOGGER.info("email " + email3.getEmailId() + " is deleted");
			} else {
				LOGGER.warn("email " + email3.getEmailId() + " is not deleted");
			}
			memberEmailList.clear();
			LOGGER.info("clear member email list done");

		}
}
