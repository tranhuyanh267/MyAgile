package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.SprintStatisticDao;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.SprintStatistic;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.SprintStatisticService;


@Service
@Transactional
public class SprintStatisticServiceImpl implements SprintStatisticService {

	@Autowired
	private SprintStatisticDao sprintStatisticDao;

	@Autowired
	private IssueService issueService; 

	@Override
	public boolean save(SprintStatistic sprintStatistic) {
		return sprintStatisticDao.save(sprintStatistic);
	}

	@Override
	public boolean update(SprintStatistic sprintStatistic) {
		return sprintStatisticDao.update(sprintStatistic);
	}

	@Override
	public boolean delete(SprintStatistic sprintStatistic) {
		return sprintStatisticDao.delete(sprintStatistic);
	}

	@Override
	public SprintStatistic findSprintStatisticBySprint(Sprint sprint) {
		return sprintStatisticDao.findSprintStatisticBySprint(sprint);
	}
	
	@Override
	public SprintStatistic initStatisticsInformation(SprintStatistic sprintStatistic) {
		if (sprintStatistic != null) {
			List<Issue> issues = issueService.findIssuesIsTaskBySprintId(sprintStatistic.getSprint().getSprintId());
			float pointPlan = issueService.calculateIssuesTotalPoints(issues);
			float pointDelivered = issueService.calculateIssuesPointDelivered(issues);
			int userStoryDelivered = issueService.calculateUserStoryDelivered(issues);
			float availableManDay = sprintStatistic.getAvailableManDay();
			if (availableManDay != 0.0F) {
				float velocityOfSprintPlan = pointPlan / availableManDay;
				float velocityOfSprintDelivered = pointDelivered / availableManDay;
				sprintStatistic.setVelocityOfSprintPlan(velocityOfSprintPlan);
				sprintStatistic.setVelocityOfSprintDelivered(velocityOfSprintDelivered);
			} else {
				sprintStatistic.setVelocityOfSprintPlan(0);
				sprintStatistic.setVelocityOfSprintDelivered(0);
			}
			sprintStatistic.setPointPlan(pointPlan);
			sprintStatistic.setPointDelivered(pointDelivered);
			sprintStatistic.setUserStoryPlan(issues.size());
			sprintStatistic.setUserStoryDelivered(userStoryDelivered);
			if (sprintStatistic.getSprintStatisticId() != null) {
				this.update(sprintStatistic);
			} else {
				this.save(sprintStatistic);
			}
			return sprintStatistic;
		}
		return null;
	}
}
