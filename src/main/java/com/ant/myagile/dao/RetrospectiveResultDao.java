package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Project;
import com.ant.myagile.model.RetrospectiveResult;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;

public interface RetrospectiveResultDao {
    List<RetrospectiveResult> findAllRetrospective();

    boolean addRetrospective(RetrospectiveResult retro);

    boolean updateRetrospective(RetrospectiveResult retro);

    boolean updateStatus(RetrospectiveResult retro);

    RetrospectiveResult getRetrospectiveById(long id);

    List<RetrospectiveResult> findRetrospectiveByTeamAndProject(Team team, Project project);
    
    List<RetrospectiveResult> findRetrospectiveByProject(long projectId);

    boolean deleteRetrospective(Long id);
    
    List<RetrospectiveResult> getRetrospectiveResultBySprint(Sprint sprint);

}

