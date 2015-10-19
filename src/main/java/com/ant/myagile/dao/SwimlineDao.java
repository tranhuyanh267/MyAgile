package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Swimline;

public interface SwimlineDao {
 	long save(Swimline swimline);
    boolean delete(Swimline swimline);
    boolean update(Swimline swimline);
    Swimline getSwimlineBySprint(long sprintId);
    List<Swimline> getAllSwimlineBySprint(Sprint sprint);
}
