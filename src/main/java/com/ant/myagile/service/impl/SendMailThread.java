package com.ant.myagile.service.impl;

import org.apache.log4j.Logger;

import com.ant.myagile.service.MailService;

public class SendMailThread implements Runnable 
{
    	private static final Logger logger = Logger.getLogger(SendMailThread.class);
    	
	protected MailService mailService;
	private Thread thread;
	private String subject;
	private String content;
	private String threadName;

	public SendMailThread(MailService mailService, String threadName, String subject, String content) 
	{
		super();
		this.subject = subject;
		this.content = content;
		this.threadName = threadName;
		this.mailService = mailService;
	}

	@Override
	public void run() 
	{
		try {
			mailService.sendMailToAdmin(subject, content);
		} catch (Exception e) {
			logger.error("Thread " + threadName + " interrupted. Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void start() 
	{
		if (thread == null) {
			thread = new Thread(this, threadName);
			thread.start();
		}
	}

}
