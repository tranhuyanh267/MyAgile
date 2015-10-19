package com.ant.myagile.viewmodel;

import java.util.List;

import com.ant.myagile.model.Project;
import com.ant.myagile.model.TeamMember;

public class TeamMemberProjects 
{
    TeamMember teamMember;
    List<Project> projects;
    String projectsName = "";
    String shortProjectsName = "";
    
    
    public TeamMemberProjects() {
	
    }

    public TeamMemberProjects(TeamMember teamMember, List<Project> projects) 
    {
	this.teamMember = teamMember;
	this.projects = projects;
	initializeDataForProjecsName();
    }

    public TeamMember getTeamMember() 
    {
        return teamMember;
    }
    
    public void setTeamMember(TeamMember teamMember) {
        this.teamMember = teamMember;
    }
    
    public List<Project> getProjects() 
    {
        return projects;
    }
    
    public void setProjects(List<Project> projects) 
    {
        this.projects = projects;
        initializeDataForProjecsName();
    }
    
    
    public String getProjectsName() {
        return projectsName;
    }

    public void setProjectsName(String projectsName) {
        this.projectsName = projectsName;
    }
    
    public String getShortProjectsName() {
        return shortProjectsName;
    }

    public void setShortProjectsName(String shortProjectsName) {
        this.shortProjectsName = shortProjectsName;
    }

    private void initializeDataForProjecsName()
    {
	if (projects != null && projects.size() > 0)
	{
	    for (Project pro : projects) 
	    {
		if (projectsName == null || projectsName.trim().isEmpty() == true){
		    projectsName = pro.getProjectName();
		} else{
		    projectsName += ", " + pro.getProjectName();
		}
	    }
	}
	
	if (projectsName != null && projectsName.length() > 29)
	{
	    shortProjectsName = projectsName.substring(0, 25) + "...";
	} else {
	    shortProjectsName = projectsName;
	}
    }

}
