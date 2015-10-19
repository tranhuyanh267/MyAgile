package com.ant.myagile.job;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.ant.myagile.service.FacebookNewsService;

@QuartzJob(name = "FacebookNewsJob", cronExp = "0 0 1 * * ?")
public class FacebookNewsJob extends QuartzJobBean{

	private static final Logger LOGGER = Logger.getLogger(FacebookNewsJob.class);
	
	@Autowired
	FacebookNewsService facebookNewsService;
	
	@Override
	protected void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		LOGGER.debug("Start facebook news job");
		//Save Facebook new into database
		facebookNewsService.saveFacebookNewsIntoDB();
	}
}
