package com.ant.myagile.managedbean;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("swimLineBean")
@Scope("session")
public class SwimlineBean {
	
	private long projectId = 0;
	private UserStory userStory;
	private List<Project> projectList;
	
	@Autowired
	UserStoryService userStoryService;
	@Autowired
	ProjectService projectService;
	@Autowired
	TeamService teamService;
	@Autowired
	Utils utils;

	
	@PostConstruct
	public void init(){
		projectList = new ArrayList<Project>();
		userStory = new UserStory();
	}
	
	/**
	 * initialize default value for preview
	 */
	public void initPreview() {
		if (JSFUtils.isPostbackRequired()) {
			Member memberLogin = this.utils.getLoggedInMember();
			this.projectList = this.projectService.findByMemberAndOwner(memberLogin.getMemberId());
			if (this.projectList != null && !this.projectList.isEmpty()) {
				if (this.projectId == 0) {
					this.projectId = this.projectList.get(0).getProjectId();
				}
			}
			this.userStory.setProject(this.projectService.findProjectById(this.projectId));
		}
	}
	
	public void handleChangeProject() {
		this.userStory.setProject(this.projectService.findProjectById(this.projectId));
	}
	
	public List<Project> getAllProjectsFromTeam(){
		return projectService.findAllProjectsByTeam(teamService.findTeamByTeamId(8460));
	}
	
	/**
	 * Save current user story that can be created or updated
	 */
	public void saveUserStory() {
		if (userStoryService.create(userStory)) {
			//pronct
		}
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public UserStory getUserStory() {
		return userStory;
	}

	public void setUserStory(UserStory userStory) {
		this.userStory = userStory;
	}
	public List<Project> getProjectList() {
		return projectList;
	}
	public void setProjectList(List<Project> projectList) {
		this.projectList = projectList;
	}
}
