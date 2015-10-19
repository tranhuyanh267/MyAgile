package com.ant.myagile.dao;


import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Message;

@ContextConfiguration(locations = {"classpath:test-context.xml"}, loader = ContextLoader.class)
public class MessageDaoImplementTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	@Autowired
	private MessageDao messageDao;
	
	@Autowired
	private MemberDao memberDao;

	
	@Test
	public void testFindMembersByTheirUnreadConversation() {
		Member sebi = memberDao.findMemberById(3); 
		Member truc = memberDao.findMemberById(11); 
		Member ngo = memberDao.findMemberById(14); 
		
		Message msgOne = new Message();
		msgOne.setRead(true);
		msgOne.setSender(truc);
		msgOne.setRecipient(sebi);
		msgOne.setSendDate(new Date());
		msgOne.setContent("Hi first times from Truc");
		messageDao.saveMessage(msgOne);
		
		Message msgTwo = new Message();
		msgTwo.setRead(false);
		msgTwo.setSender(truc);
		msgTwo.setRecipient(sebi);
		msgTwo.setSendDate(new Date());
		msgTwo.setContent("Hi second times from Truc");
		messageDao.saveMessage(msgTwo);
		
		Message msgThree = new Message();
		msgThree.setRead(false);
		msgThree.setSender(truc);
		msgThree.setRecipient(sebi);
		msgThree.setSendDate(new Date());
		msgThree.setContent("Hi third times from Truc");
		messageDao.saveMessage(msgThree);
		
		List<Member> resultFirstTimes = messageDao.findMembersByTheirUnreadConversation(sebi.getMemberId());
		assertEquals(resultFirstTimes.size(), 1);
		
		Message msgFour = new Message();
		msgFour.setRead(false);
		msgFour.setSender(ngo);
		msgFour.setRecipient(sebi);
		msgFour.setSendDate(new Date());
		msgFour.setContent("Hi fourth times from Ngo");
		messageDao.saveMessage(msgFour);
		
		List<Member> resultSecondTimes = messageDao.findMembersByTheirUnreadConversation(sebi.getMemberId());
		assertEquals(resultSecondTimes.size(), 2);
	}
}
