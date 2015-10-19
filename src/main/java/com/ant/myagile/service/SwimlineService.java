package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Swimline;

public interface SwimlineService {
	long save(Swimline swimline);
    boolean delete(Swimline swimline);
    boolean update(Swimline swimline);
    Swimline getSwimlineBySprint(long sprintId);
    List<Swimline> getAllSwimlineBySprint(Sprint sprint);
    void deleteAllSwimlineInSprint(Sprint sprint);
}
