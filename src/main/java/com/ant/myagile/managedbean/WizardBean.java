package com.ant.myagile.managedbean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("wizardBean")
@Scope("session")
public class WizardBean 
{
    	private static final Logger LOGGER = Logger.getLogger(WizardBean.class);
	    
	@Autowired
	private TeamService teamService;
	@Autowired
	private Utils utils;
	@Autowired
	private SprintService sprintService;
	
	private String flagTeamId = "";
	private List<Long> selectedTeams;
	private Map<String, Object> teamSprint = new HashMap<String, Object>();
	private List<Team> createdTeamList = new ArrayList<Team>();
	private List<Sprint> sprintList = new ArrayList<Sprint>();
	private List<Team> assignTeam = new ArrayList<Team>();
	private String projectName = "";
	private String owner;
	private List<String> listStyle = new ArrayList<String>();
	private Iterator<Map.Entry<String, Object>> iterator;
	private Map.Entry<String, Object> entry;
	private boolean projectSavedSuccessful = false;

	public int getHightLightTeam() 
	{
		String mailGroup = getFlagTeamId();
		List<Team> teamList = getCombinedTeamList();
		
		for (Team team : teamList) 
		{
			if (team.getMailGroup().equals(mailGroup)) {
				return getCombinedTeamList().indexOf(team);
			}
		}
		
		return 0;
	}

	/**
	 * Initialize teamSprint when user change step from team page to sprint page
	 * Initialize sprintList when user change step form sprint to result
	 * 
	 * @param event
	 * @return
	 */
	public String onFlowProcess(FlowEvent event) 
	{
		RequestContext requestContext = RequestContext.getCurrentInstance();
		
		if (event.getNewStep().equals("sprint") && event.getOldStep().equals("team")) 
		{
			JSFUtils.resetForm("wizardForm");
			if (getCombinedTeamList().size() == 0) {
				requestContext.execute("cfSkip.show()");
			}
			for (int i = 0; i < getCombinedTeamList().size(); i++) {
				if (!checkExistTeam(getIndex(i))) {
					teamSprint.put(getIndex(i), new Sprint());
				}
			}
		}
		
		if (event.getNewStep().equals("sprint") && event.getOldStep().equals("result")) 
		{
			JSFUtils.resetForm("wizardForm");
			if (getCombinedTeamList().size() == 0) {
				requestContext.execute("wiz.loadStep(wiz.cfg.steps[1])");
			}
		}

		if (event.getNewStep().equals("result") && event.getOldStep().equals("sprint"))
		{
			try {
				// Preset sprint list with size of team list
				sprintList = new ArrayList<Sprint>();
				List<Team> combinedTeamList = getCombinedTeamList();
				for (int i = 0; i < combinedTeamList.size(); i++) {
					sprintList.add(new Sprint());
				}

				// Add sprint to sprint List
				iterator = teamSprint.entrySet().iterator();
				while (iterator.hasNext())
				{
					entry = iterator.next();
					Sprint sprint = (Sprint) entry.getValue();
					
					if (sprint.getSprintName() == null || sprint.getSprintName().equals("")) 
					{
						Sprint sprintTemp = new Sprint();
						sprintTemp.setSprintName("No sprint");
						try {
							sprintList.set(searchTeam(entry.getKey()), sprintTemp);
						} catch (Exception e) {
						    LOGGER.error("search team error: " + e.getMessage());
						}
						continue;
					}
					try {
						sprintList.set(searchTeam(entry.getKey()), sprint);
					} catch (Exception e) {
					}
				}

			} catch (Exception e) {
			    LOGGER.error("onFlowProcess error: " + e.getMessage());
			}
		}
		
		return event.getNewStep();
	}

	/**
	 * The first: Save Project from project data temp The second: Save Team form
	 * team data temp The third: Save Sprint of each team from sprint list data
	 * temp
	 */
	public void save() 
	{
	    projectSavedSuccessful = false;
		try 
		{
			// Set project Name
			setProjectName((String) JSFUtils.resolveExpression("#{projectBean.projectName}"));
			// Save team
			for (Team team : createdTeamList) 
			{
				JSFUtils.setExpressionValue("#{teamBean.teamTemp}", team);
				JSFUtils.resloveMethodExpression("#{teamBean.saveTeam}", Void.class, new Class<?>[0], new Object[0]);
			}

			// Save project
			List<Long> idTeamList = new ArrayList<Long>();
			for (Team team : getCombinedTeamList()) 
			{
				idTeamList.add(team.getTeamId());
			}
			
			JSFUtils.setExpressionValue("#{projectBean.selectedTeams}", idTeamList);
			JSFUtils.resloveMethodExpression("#{projectBean.addProjectWizard}", Void.class, new Class<?>[0], new Object[0]);

			// Save sprint
			iterator = teamSprint.entrySet().iterator();
			
			while (iterator.hasNext()) 
			{
				entry = iterator.next();
				Sprint sprint = new Sprint();
				sprint = (Sprint) entry.getValue();
				sprint.setStatus("open");
				
				if (sprint.getSprintName() != null && !sprint.getSprintName().trim().equals("") && sprint.getDateStart() != null && sprint.getDateEnd() != null) 
				{
					String key = entry.getKey();
					Team team = getTeamByMail(key);
					sprint.setTeam(team);
					JSFUtils.setExpressionValue("#{sprintBean.sprint}", sprint);
					JSFUtils.resloveMethodExpression("#{sprintBean.saveSprint}", Void.class, new Class<?>[0], new Object[0]);
				}
			}
			
			resetBean();
			// Redirect to project page
			String projectContextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/project";
			HttpServletRequest origRequest = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			StringBuffer requestURL = origRequest.getRequestURL();
			String contextPath = requestURL.substring(0, requestURL.indexOf(origRequest.getRequestURI())) + projectContextPath;
			projectSavedSuccessful = true;
			JSFUtils.redirect(contextPath);
			
		} catch (Exception e) {
		    LOGGER.error("save error: " + e.getMessage());
		}
	}

	public boolean checkCreatedSprint(String groupMail) 
	{
		iterator = teamSprint.entrySet().iterator();
		while (iterator.hasNext()) {
			entry = iterator.next();
			if (entry.getKey().equals(groupMail)) {
				Sprint sprint = new Sprint();
				sprint = (Sprint) entry.getValue();
				if (sprint.getSprintName() == null || sprint.getSprintName().equals("")) {
					return false;
				}
			}
		}
		return true;
	}

	public void removeSprintWithoutTeam() 
	{
		RequestContext requestContext = RequestContext.getCurrentInstance();
		iterator = teamSprint.entrySet().iterator();
		
		while (iterator.hasNext()) 
		{
			entry = iterator.next();
			if (entry.getKey().equals(flagTeamId)) {
				entry.setValue(new Sprint());
				JSFUtils.setExpressionValue("#{sprintBean.sprint}", new Sprint());
				requestContext.execute("createNewSprint([{name: 'groupMail', value: '" + entry.getKey() + "'}])");
			}
		}
	}

	/**
	 * Show sprint of team when user click into the each team on menu list team.
	 * Assign sprint list of team to #{sprintBean.sprintList} when user click
	 * into the each team on menu list team.
	 * 
	 * @return
	 */
	public int initSprint() 
	{
		try 
		{
			JSFUtils.resetForm("wizardForm");
			String groupMail = JSFUtils.getRequestParameter("groupMail");
			flagTeamId = groupMail;
			iterator = teamSprint.entrySet().iterator();
			
			while (iterator.hasNext())
			{
				entry = iterator.next();
				if (entry.getKey().equals(flagTeamId))
				{
					String key = entry.getKey();
					Team teamTemp = getTeamByMail(key);
					Sprint sprint = (Sprint) entry.getValue();
					
					if (teamTemp.getTeamId() != null) {
						JSFUtils.setExpressionValue("#{sprintBean.sprintList}", sprintService.findSprintsByTeamId(teamTemp.getTeamId()));
					} else {
						Date startDate = new Date();
						sprint.setDateStart(startDate);
						Date suggestDate = Utils.addDays(startDate, 14);
						sprint.setDateEnd(suggestDate);
						JSFUtils.setExpressionValue("#{sprintBean.sprintList}", new ArrayList<Sprint>());
					}
					JSFUtils.setExpressionValue("#{sprintBean.sprint}", sprint);
					JSFUtils.resloveMethodExpression("#{sprintBean.suggestDate}", Void.class, new Class<?>[0], new Object[0]);
				}
			}
			return 1;

		} catch (Exception e) {
		    LOGGER.error("initSprint error: " + e.getMessage());
			return 1;
		}
	}

	public void newTeamLogo() 
	{
		String index = JSFUtils.getRequestParameter("index");
		Team teamTemp = createdTeamList.get(Integer.valueOf(index));
		JSFUtils.setExpressionValue("#{teamBean.teamTemp}", teamTemp);
		JSFUtils.resloveMethodExpression("#{teamBean.newTeamLogo}", Void.class, new Class<?>[0], new Object[0]);
	}

	public void addMoreTeam() 
	{
		Team team = new Team();
		team.setEstablishedDate(new Date());
		createdTeamList.add(team);
	}

	public void removeTeam(int index) 
	{
		try {
			createdTeamList.remove(index);
		} catch (NumberFormatException e) {
		    LOGGER.error("remove team error: " + e.getMessage());
		}
	}

	public void resetAllTeam() {
		createdTeamList.clear();

	}

	public Team findTeamsByTeamId(Long teamId) {
		return teamService.findTeamByTeamId(teamId);
	}

	public List<Team> getTeams() 
	{
		 return excludeHiddenTeamsForSelection(teamService.findTeamsByOwner(utils.getLoggedInMember()));
	}

	public List<Long> getSelectedTeams() {
		return selectedTeams;
	}

	public void setSelectedTeams(List<Long> selectedTeams) {
		this.selectedTeams = selectedTeams;
	}

	public Map<String, Object> getTeamSprint() {
		return teamSprint;
	}

	public void setTeamSprint(Map<String, Object> teamSprint) {
		this.teamSprint = teamSprint;
	}

	public String getFlagTeamId() {
		return flagTeamId;
	}

	public void setFlagTeamId(String flagTeamId) {
		this.flagTeamId = flagTeamId;
	}

	private String getIndex(int i) {
		return getCombinedTeamList().get(i).getMailGroup();
	}

	private Team getTeamByMail(String mail) {
		for (Team team : getCombinedTeamList()) {
			if (team.getMailGroup().equals(mail)) {
				return team;
			}
		}
		return null;
	}

	public List<String> getListStyle() 
	{
		listStyle = new ArrayList<String>();
		
		for (int i = 0; i < createdTeamList.size(); i++) 
		{
			listStyle.add("Create");
		}
		
		for (int i = 0; i < selectedTeams.size(); i++) {
			listStyle.add("Assign");
		}
		
		return listStyle;
	}

	public void setListStyle(List<String> listStyle) {
		this.listStyle = listStyle;
	}

	public String getOwner() {
		owner = utils.getLoggedInMember().getLastName() + " " + utils.getLoggedInMember().getFirstName();
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void removeProjectMessage() {
		projectName = "";
		projectSavedSuccessful = false;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public List<Team> getAssignTeam() 
	{
		assignTeam = new ArrayList<Team>();
		for (Long teamId : selectedTeams) 
		{
			Team t = teamService.findTeamByTeamId(teamId);
			assignTeam.add(t);
		}

		return assignTeam;
	}

	public void setAssignTeam(List<Team> assignTeam) {
		this.assignTeam = assignTeam;
	}

	public List<Sprint> getSprintList() {

		return sprintList;
	}

	public void setSprintList(List<Sprint> sprintList) {
		this.sprintList = sprintList;
	}

	public List<Team> getCombinedTeamList() 
	{
		try {
			List<Team> combinedTeamList = new ArrayList<Team>();
			combinedTeamList.addAll(createdTeamList);
			for (Long teamId : selectedTeams) {
				Team t = teamService.findTeamByTeamId(teamId);
				combinedTeamList.add(t);
			}
			return combinedTeamList;
		} catch (Exception e) {
		    LOGGER.error("getCombinedTeamList error: " + e.getMessage());
		    return new ArrayList<Team>();

		}
	}

	public void resetBean()
	{
		createdTeamList = new ArrayList<Team>();
		flagTeamId = "";
		selectedTeams = new ArrayList<Long>();
		sprintList = new ArrayList<Sprint>();
		JSFUtils.setExpressionValue("#{projectBean.projectName}", "");
		JSFUtils.setExpressionValue("#{projectBean.projectNameOld}", "");
		JSFUtils.setExpressionValue("#{projectBean.description}", "");
		JSFUtils.setExpressionValue("#{projectBean.isPublic}", false);
		JSFUtils.setExpressionValue("#{projectBean.projectSavedSuccessful}", false);
	}

	public void resetWizard() 
	{
		resetBean();
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("project");
		} catch (IOException e) {
		    LOGGER.error("resetWizard error: " + e.getMessage());
		}
	}

	private boolean checkExistTeam(String groupMail) 
	{

		iterator = teamSprint.entrySet().iterator();
		while (iterator.hasNext()) {
			entry = iterator.next();
			if (entry.getKey().equals(groupMail)) {
				return true;
			}
		}
		return false;
	}

	public Team getSelectTeam() 
	{
		Team team = findTeamByGroupMail(flagTeamId);
		if (team == null) {
			team = getCombinedTeamList().get(0);
			return team;
		}
		
		return findTeamByGroupMail(flagTeamId);
	}

	private int searchTeam(String t)
	{
		for (int i = 0; i < getCombinedTeamList().size(); i++) 
		{
			if (getCombinedTeamList().get(i).getMailGroup().equals(t)) 
			{
				return i;
			}
		}
		return -1;
	}

	public List<Team> getCreatedTeamList() {
		return createdTeamList;
	}

	public void setCreatedTeamList(List<Team> listCreatedTeam) {
		createdTeamList = listCreatedTeam;
	}

	public Iterator<Map.Entry<String, Object>> getIterator() {
		return iterator;
	}

	public void setIterator(Iterator<Map.Entry<String, Object>> iterator) {
		this.iterator = iterator;
	}

	public Map.Entry<String, Object> getEntry() {
		return entry;
	}

	public void setEntry(Map.Entry<String, Object> entry) {
		this.entry = entry;
	}
	
	public boolean isProjectSavedSuccessful() {
	    return projectSavedSuccessful;
	}

	public void setProjectSavedSuccessful(boolean projectSavedSuccessful) {
	    this.projectSavedSuccessful = projectSavedSuccessful;
	}

	private Team findTeamByGroupMail(String mailGroup) 
	{
		for (Team team : getCombinedTeamList()) 
		{
			if (team.getMailGroup().equals(mailGroup)) {
				return team;
			}
		}
		return null;
	}

	public boolean checkFirstLoad() 
	{
		List<Team> createdTeam = getCombinedTeamList();
		
		for (Team team : createdTeam)
		{
			if (team.getMailGroup().equals(flagTeamId)) {
				return true;
			}
		}
		return false;
	}
	
	private List<Team> excludeHiddenTeamsForSelection(List<Team> allTeams)
	{
	    List<Team> teamList = new ArrayList<Team>();
	    
	    if (allTeams != null && allTeams.size() > 0)
	    {
		for (Team team : allTeams) 
		{
		    if (team != null && team.getValidTo() == null) //validTo = null => team is not hidden. 
		    {
			teamList.add(team);
		    }
		}
	    }
	    
	    return teamList;
	}
}
