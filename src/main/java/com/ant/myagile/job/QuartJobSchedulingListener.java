package com.ant.myagile.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;

public class QuartJobSchedulingListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private Scheduler scheduler;

    private static final Logger LOGGER = Logger.getLogger(QuartJobSchedulingListener.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
	try {
	    ApplicationContext applicationContext = event.getApplicationContext();
	    List<CronTriggerBean> cronTriggerBeans = this.loadCronTriggerBeans(applicationContext);
	    this.scheduleJobs(cronTriggerBeans);
	} catch (Exception e) {

	}
    }

    private List<CronTriggerBean> loadCronTriggerBeans(ApplicationContext applicationContext) {
	Map<String, Object> quartzJobBeans = applicationContext.getBeansWithAnnotation(QuartzJob.class);
	Set<String> beanNames = quartzJobBeans.keySet();
	List<CronTriggerBean> cronTriggerBeans = new ArrayList<CronTriggerBean>();
	for (String beanName : beanNames) {
	    CronTriggerBean cronTriggerBean = null;
	    Object object = quartzJobBeans.get(beanName);
	    try {
		cronTriggerBean = this.buildCronTriggerBean(object);
	    } catch (Exception e) {

	    }

	    if (cronTriggerBean != null) {
		cronTriggerBeans.add(cronTriggerBean);
	    }
	}
	return cronTriggerBeans;
    }

    public CronTriggerBean buildCronTriggerBean(Object job) throws Exception {
	CronTriggerBean cronTriggerBean = null;
	QuartzJob quartzJobAnnotation = AnnotationUtils.findAnnotation(job.getClass(), QuartzJob.class);
	if (Job.class.isAssignableFrom(job.getClass())) {
	    cronTriggerBean = new CronTriggerBean();
	    cronTriggerBean.setCronExpression(quartzJobAnnotation.cronExp());
	    cronTriggerBean.setName(quartzJobAnnotation.name() + "_trigger");
	    JobDetailBean jobDetail = new JobDetailBean();
	    jobDetail.setName(quartzJobAnnotation.name());
	    jobDetail.setJobClass(job.getClass());
	    cronTriggerBean.setJobDetail(jobDetail);
	} else {
	    LOGGER.debug(job.getClass() + " doesn't implemented " + Job.class);
	}
	return cronTriggerBean;
    }

    protected void scheduleJobs(List<CronTriggerBean> cronTriggerBeans) {
	for (CronTriggerBean cronTriggerBean : cronTriggerBeans) {
	    JobDetail jobDetail = cronTriggerBean.getJobDetail();
	    try {
		scheduler.scheduleJob(jobDetail, cronTriggerBean);
	    } catch (SchedulerException e) {
		LOGGER.debug(e);
	    }
	}
    }
}