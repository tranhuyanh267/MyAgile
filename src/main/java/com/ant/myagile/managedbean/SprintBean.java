package com.ant.myagile.managedbean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Swimline;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.KanbanStatusService;
import com.ant.myagile.service.KanbanSwimlineService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.service.SwimlineService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("sprintBean")
@Scope("session")
public class SprintBean 
{
	public static final int DEFAULT_SPRINT_DAYS = 14; //two weeks
	public static Logger log = Logger.getLogger(SprintBean.class);

	private Sprint sprint;
	private boolean reuseSetting = true;
	private List<Sprint> sprintList;
	private List<Team> teamList;
	private Sprint selectedSprint;
	private boolean suggestted;
	private Sprint selectedDropDownSprint;
	private String selectedDropDownSprintId;
	private String selectedDropDownTeamId;
	@Autowired
	private TeamService teamService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private KanbanStatusService kanbanStatusService;
	@Autowired
	private KanbanSwimlineService kanbanSwimlineService;
	@Autowired
	Utils utils;
	@Autowired
	private SwimlineService swimlineService;

	public void initPreview() throws IOException 
	{
		if (JSFUtils.isPostbackRequired()) 
		{
			try 
			{
				List<Team> allTeams = teamService.findTeamsByMemberAndOwner(utils.getLoggedInMember());
				setTeamList(allTeams);
				
				if (teamList != null && !teamList.isEmpty()) 
				{
					if (selectedDropDownTeamId != null) {
						setSprintList(sprintService.findSprintsByTeamId(Long.valueOf(selectedDropDownTeamId)));
					} else if (sprintList == null || sprintList.isEmpty()) {
						setSprintList(sprintService.findSprintsByTeamId(teamList.get(0).getTeamId()));
					}
					if (sprint == null) {
						sprint = new Sprint();
					}
					if (sprint.getTeam() == null) {
						sprint.setTeam(teamList.get(0));
					}
					suggestDate();
				}
			} catch (Exception e) {
				FacesContext.getCurrentInstance().getExternalContext().redirect("/myagile");
			}
		}

	}

	@SuppressWarnings("static-access")
	public void redirectToNewSprintPage() 
	{
		String stringTeamId = JSFUtils.getRequestParameter("teamSelect_input");
		String stringSprintId = JSFUtils.getRequestParameter("sprintSelect_input");
		
		try 
		{
			Long teamId = Long.parseLong(stringTeamId);
			sprint = new Sprint();
			sprint.setTeam(teamService.findTeamByTeamId(teamId));
			sprintList = sprintService.findSprintsByTeamId(teamId);
			suggestted = false;
			suggestDate();
			FacesContext.getCurrentInstance().getExternalContext().redirect("sprint/managesprint");
			setSelectedDropDownSprint(sprintService.findSprintById(Long.parseLong(stringSprintId)));
		} catch (Exception e) {
			JSFUtils.addWarningMessage("msgs", utils.getMessage("myagile.sprint.ErrorSelectTeam", null));
		}
	}


	@SuppressWarnings("static-access")
	public String redirectToEditSprintPage() 
	{
		String stringSprintId = JSFUtils.getRequestParameter("sprintSelect_input");
		
		if (stringSprintId == null || stringSprintId.equals("")) {
			JSFUtils.addWarningMessage(null, utils.getMessage("myagile.sprint.ErrorSelectSprint", null));
		} else {
			sprint = sprintService.findSprintById(sprint.getSprintId());
			return "sprint?faces-redirect=true";
		}
		return null;
	}

	/**
	 * Give suggestion about date start and date end of new Sprint
	 */
	public void suggestDate() 
	{
		if (!suggestted) 
		{
			getSprintList();
			if (sprintList.size() == 0) {
				return;
			}
			Sprint latestSprint = sprintList.get(0);
			Date suggestStartDate = Utils.addDays(latestSprint.getDateEnd(), 1);
			sprint.setDateStart(suggestStartDate);
			Date suggestEndDate = Utils.addDays(suggestStartDate, DEFAULT_SPRINT_DAYS);
			sprint.setDateEnd(suggestEndDate);
			suggestted = false;
		}
	}

	/**
	 * Create new sprint with information entered by user, and save into
	 * database
	 */
	@SuppressWarnings("static-access")
	public void saveSprint() 
	{
		UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
		UIComponent component = viewRoot.findComponent("sprintForm");
		
		if (sprint.getTeam() == null) {
			JSFUtils.addWarningMessage("msgs", utils.getMessage("myagile.sprint.ErrorSelectTeam", null));
		} else {
			Map<String, String> mapValid = new HashMap<String, String>();
			mapValid = sprintService.validateSprintDate(sprint.getTeam().getTeamId(), sprint);
			if (mapValid.size() > 0) {
				if (mapValid.containsKey("dateStart")) {
					JSFUtils.addErrorMessage("startDate", mapValid.get("dateStart"));
				}
				if (mapValid.containsKey("dateEnd")) {
					JSFUtils.addErrorMessage("endDate", mapValid.get("dateEnd"));
				}
				if (mapValid.containsKey("dateStartEdit")) {
					JSFUtils.addErrorMessage("startDate", mapValid.get("dateStartEdit"));
				}
				if (mapValid.containsKey("dateEndEdit")) {
					JSFUtils.addErrorMessage("endDate", mapValid.get("dateEndEdit"));
				}
			} else if (sprint.getSprintId() != null) {
				sprintService.update(sprint);
				JSFUtils.addCallBackParam("edit", true);
				JSFUtils.addCallBackParam("save", false);
				resetValue();
			} else {
				sprintService.save(sprint);
				if (reuseSetting && getLastSprint() != null) {
					statusService.reuseKanbanSetting(teamService.findTeamByTeamId(sprint.getTeam().getTeamId()), sprint, getLastSprint());
					//check swimline exist in sprint
					Swimline statusSwimline =  swimlineService.getSwimlineBySprint(getLastSprint().getSprintId());
					if(statusSwimline != null){
						//add status swimline
						Swimline addStatusSwimline = new Swimline();
						addStatusSwimline.setName("Swimline");
						addStatusSwimline.setSprint(this.sprint);
						swimlineService.save(addStatusSwimline);
					}
				} else {
					statusService.createStatusTodoAndDoneForSprint(teamService.findTeamByTeamId(sprint.getTeam().getTeamId()), sprint);
				}
				JSFUtils.addCallBackParam("edit", false);
				JSFUtils.addCallBackParam("save", true);
				resetValue();
			}
			sprintList = sprintService.findSprintsByTeamId(sprint.getTeam().getTeamId());
			selectedDropDownSprint = sprintList.get(0);

			if (component != null) {
				resetForm();
			}
		}
	}

	/**
	 * Find all teams which are joined by logged in member and teams are created
	 * by logged in member
	 * 
	 * @return List of Team
	 */
	public List<Team> findTeamsByUser() 
	{
	    setTeamList(teamService.findTeamsByMemberAndOwner(utils.getLoggedInMember()));
	    return this.teamList;
	}

	/**
	 * Execute changing in Sprint drop down list when selecting Team
	 */
	public void handleChangeTeam() 
	{
		resetForm();
		long teamId = sprint.getTeam().getTeamId();
		sprintList = sprintService.findSprintsByTeamId(teamId);
		sprint = new Sprint();
		sprint.setTeam(teamService.findTeamByTeamId(teamId));
		
		if (sprintList != null && !sprintList.isEmpty()) {
			setSelectedDropDownSprint(sprintList.get(0));
		} else {
			setSelectedDropDownSprint(new Sprint());
		}
		suggestted = false;
		suggestDate();
	}

	/**
	 * Execute and give back suggested date end when changing date start
	 * 
	 * @param event
	 *            - SelectEvent
	 */
	public void handleDateStartSelect(SelectEvent event)
	{
		Date startDate = (Date) event.getObject();
		Date suggestDate = Utils.addDays(startDate, 14);
		sprint.setDateEnd(suggestDate);
	}

	public void createNewSprint() 
	{
		resetForm();
		long teamId = sprint.getTeam().getTeamId();
		sprint = new Sprint();
		sprint.setTeam(teamService.findTeamByTeamId(teamId));
		JSFUtils.addCallBackParam("create", true);
		suggestted = false;
		suggestDate();
	}

	public void updateForm() 
	{
		resetForm();
		sprint = sprintService.findSprintById(selectedSprint.getSprintId());
		JSFUtils.addCallBackParam("edit", true);
	}

	/**
	 * Reset all value in Sprint New - Edit Form
	 */
	public void resetForm() 
	{
		JSFUtils.resetForm("sprintForm");
	}

	public void resetValue() 
	{
		sprint.setSprintId(null);
		sprint.setDateEnd(null);
		sprint.setDateStart(null);
		sprint.setSprintName(null);
		sprint.setStatus(null);
	}

	public TimeZone getTimeZone()
	{
		TimeZone timeZone = null;
		timeZone = TimeZone.getDefault();
		return timeZone;
	}

	public Sprint getSprint() 
	{
		if (sprint == null) {
			sprint = new Sprint();
		}
		return sprint;
	}

	public void setSprint(Sprint sprint) {
		this.sprint = sprint;
	}

	public List<Sprint> getSprintList() 
	{
		if (sprintList == null) {
			sprintList = new ArrayList<Sprint>();
		}
		return sprintList;
	}

	public Sprint getLastSprint() {
		if (sprintList != null && sprintList.size() > 0) {
			if (sprintList.get(0) != null) {
				return sprintList.get(0);
			}
		}
		return null;
	}

	public void setSprintList(List<Sprint> sprintList) {
		this.sprintList = sprintList;
	}

	public List<Team> getTeamList() 
	{
		if (this.teamList == null) {
			excludeHiddenTeamsForSelection(teamService.findTeamsByMemberAndOwner(utils.getLoggedInMember()));
		}
		
		return this.teamList;
	}

	public void setTeamList(List<Team> teamList) 
	{
	    excludeHiddenTeamsForSelection(teamList);
	}

	public Sprint getSelectedSprint() 
	{
		if (selectedSprint == null) {
			selectedSprint = new Sprint();
		}
		return selectedSprint;
	}

	public void setSelectedSprint(Sprint selectedSprint) {
		this.selectedSprint = selectedSprint;
	}

	public TeamService getTeamService() {
		return teamService;
	}

	public void setTeamService(TeamService teamService) {
		this.teamService = teamService;
	}

	public SprintService getSprintService() {
		return sprintService;
	}

	public void setSprintService(SprintService sprintService) {
		this.sprintService = sprintService;
	}

	public boolean isReuseSetting() {
		return reuseSetting;
	}

	public void setReuseSetting(boolean reuseSetting) {
		this.reuseSetting = reuseSetting;
	}

	public Sprint getSelectedDropDownSprint() 
	{
		if (selectedDropDownSprint == null) {
			return new Sprint();
		}
		return selectedDropDownSprint;
	}

	public void setSelectedDropDownSprint(Sprint selectedDropDownSprint) {
		this.selectedDropDownSprint = selectedDropDownSprint;
	}

	public String getSelectedDropDownSprintId() 
	{
		if (selectedDropDownSprintId == null) 
		{
			if (getSprintList() != null && getSprintList().size() > 0) {
				selectedDropDownSprintId = String.valueOf(getSprintList().get(0).getSprintId());
			} else {
				selectedDropDownSprintId = "-1";
			}
		}
		return selectedDropDownSprintId;
	}

	public void setSelectedDropDownSprintId(String selectedDropDownSprintId) {
		this.selectedDropDownSprintId = selectedDropDownSprintId;
	}

	public String getSelectedDropDownTeamId()
	{
		if (selectedDropDownTeamId == null) 
		{
			if (getTeamList() != null && getTeamList().size() > 0) {
				selectedDropDownTeamId = String.valueOf(getTeamList().get(0).getTeamId());
			} else {
				selectedDropDownTeamId = "-1";
			}
		}
		return selectedDropDownTeamId;
	}

	public void setSelectedDropDownTeamId(String selectedDropDownTeamId) {
		this.selectedDropDownTeamId = selectedDropDownTeamId;
	}
	
	private void excludeHiddenTeamsForSelection(List<Team> allTeams)
	{
	    this.teamList = new ArrayList<Team>();
	    
	    if (allTeams != null && allTeams.size() > 0)
	    {
		for (Team team : allTeams) 
		{
		    if (team != null && team.getValidTo() == null) //validTo = null => team is not hidden. 
		    {
			this.teamList.add(team);
		    }
		}
	    }
	}
}
