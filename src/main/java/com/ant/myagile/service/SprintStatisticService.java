package com.ant.myagile.service;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.SprintStatistic;

public interface SprintStatisticService {
	boolean save(SprintStatistic sprintStatistic);
	boolean update(SprintStatistic sprintStatistic);
	boolean delete(SprintStatistic sprintStatistic);
	SprintStatistic findSprintStatisticBySprint(Sprint sprint);
	
	
	/**
	 * Calculate value for property of sprint statistic
	 * @param sprintStatistic
	 * @return
	 */
	SprintStatistic initStatisticsInformation(SprintStatistic sprintStatistic);
}
