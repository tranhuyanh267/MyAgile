package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;

public interface ProjectDao {
    boolean save(Project project);

    boolean update(Project project);

    boolean delete(Project project);

    Project findById(Long id);

    /**
     * Check project name is existed or not
     * 
     * @param projectName
     * @param ownId
     * @return Project
     */
    Project checkExistProject(String projectName, long ownId);

    List<Project> listAllproject();

    List<Project> findByOwnerId(Long ownerId);

    List<Project> findByMemberId(Long memberId);
    
    List<Project> findByProductOwnerId(Long productOwnerId);
    
    /**
     * all project join and create contain project don't have any team
     */
    List<Project> findAllByMemberId(Long memberId);

    List<Project> findProjectsByTeam(long id);

    List<Project> findAllProjectsByTeam(Team t);

    List<Project> findProjectsAssignedToTeam(Team t);

    List<Project> findLazyProjects(StateLazyLoading lazyLoadingProject);

    List<Project> findLazyByMemberId(Long idUserForProject, StateLazyLoading lazyLoadingUserProject);

    int countTotalLazyProjects(StateLazyLoading lazyLoadingProject);

    int countTotalLazyByMemberId(Long idUserForProject, StateLazyLoading lazyLoadingUserProject);
}
