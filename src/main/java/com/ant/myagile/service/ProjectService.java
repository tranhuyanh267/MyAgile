package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;

public interface ProjectService 
{
    boolean update(Project pro);

    boolean createProjects(Project pro);

    boolean delete(Project project);

    boolean save(Project project);

    Project findProjectById(long id);

    /**
     * Check project name is existed or not
     * 
     * @param projectName
     * @param ownId
     * @return Project
     */
    Project checkExistProject(String projectName, long ownId);

    Project findProjectOfIssue(Issue issue);

    List<Project> findAllProjects();

    List<Project> findByOwnerId(Long ownerId);
    
    List<Project> findByProductOwnerId(Long productOwnerId);

    List<Project> findByMemberId(Long memberId);

    List<Project> findAllByMemberId(Long memberId);

    List<Project> findByMemberAndOwner(Long memberId);

    List<Project> findAllProjectsByTeam(Team t);

    List<Project> findProjectsAssignedToTeam(Team t);

    List<Project> filterTeamProjectsWithMemberId(Long teamId, Long memberId);

    boolean moveFileFromTempDirectoryToRealPath(String newFileName, String oldFileName, String path);

    boolean deleteLogo(String filename);

    boolean deleteLogoRealPath(String filename);

    String renameLogo(String oldFileName, String newFileName);

    String projectImageNameProcess(String projectName);

    boolean deleteProjectAndAllContents(long projectId);

    boolean setFalseArchivedOfProject();

    void updateWithoutHistory(Project project);

    List<Project> findLazyProjects(StateLazyLoading lazyLoadingProject);

    List<Project> findLazyByMemberId(Long idUserForProject, StateLazyLoading lazyLoadingUserProject);

    int countTotalLazyProjects(StateLazyLoading lazyLoadingProject);

    int countTotalLazyByMemberId(Long idUserForProject, StateLazyLoading lazyLoadingUserProject);
}
