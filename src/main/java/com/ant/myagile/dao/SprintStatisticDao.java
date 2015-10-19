package com.ant.myagile.dao;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.SprintStatistic;

public interface SprintStatisticDao {
    boolean save(SprintStatistic sprintStatistic);

    boolean update(SprintStatistic sprintStatistic);

    boolean delete(SprintStatistic sprintStatistic);

    SprintStatistic findSprintStatisticBySprint(Sprint sprint);
}
