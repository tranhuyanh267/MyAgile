package com.ant.myagile.service;

import org.junit.Test;

import com.ant.myagile.service.MailService;
import com.ant.myagile.service.impl.MailServiceImpl;

import junit.framework.TestCase;

public class MailServiceTest extends TestCase {
	private MailService mailService;
	
	protected void setUp() throws Exception {
	    mailService = new MailServiceImpl();
	  }
	
	@Test
	public void testSendMail() throws Exception {
		assertEquals(true, mailService.sendMail("Test", "Test send email", "ngodinhminh@axonactive.com.vn"));
	}
}
