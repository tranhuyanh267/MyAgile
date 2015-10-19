package com.ant.myagile.job;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.ant.myagile.service.MailService;
import com.ant.myagile.utils.Utils;

/**
 * MailJob execute every two minutes. Read all UNREAD email from inbox of
 * my-agile@axonactive.vn, analyze subject to get the project name, read the
 * content to extract the name and description of userstory
 * 
 * @author sutnt
 * @since 24.10.2013
 */
@QuartzJob(name = "MailJob", cronExp = "0 0 0/1 * * ?")
public class MailJob extends QuartzJobBean {
	private static final Logger LOGGER = Logger.getLogger(MailJob.class);

	@Autowired
	MailService mailService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		LOGGER.debug("Start job get mail!");
		String environment = Utils.getConfigParam("myagile.application.environment");
		if (environment.equals("Testing") || environment.equals("Release")) {
			try {
				mailService.readMail();
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
			}
		}
	}
}