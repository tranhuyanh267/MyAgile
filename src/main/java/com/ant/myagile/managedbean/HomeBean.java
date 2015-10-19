package com.ant.myagile.managedbean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.converter.ClientTimeOffsetConverter;
import com.ant.myagile.model.History;
import com.ant.myagile.model.KanbanHistory;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.KanbanHistoryService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.Utils;

@Component("homeBean")
@Scope("session")
public class HomeBean{

	@Autowired
	private ProjectService projectService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private Utils utils;
	@Autowired
	private MeetingVideoBean meetingVideoBean;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private RetrospectiveResultBean retrospectiveResultBean;
	@Autowired
	private ChartBean chartBean;
	@Autowired
	private ProductBacklogHomeBean productBacklogBean;
	@Autowired
	HistoryService historyService;
	@Autowired
	private KanbanHistoryService kanbanHistoryService;

	public static final int NUM_HISTORY = 5 ;
	public static final int START_ROW = 0;
	public static final int HOUR_ENDDAY = 23;
	public static final int MIN_ENDDAY = 59;
	public static final int ZERO = 0;
	private List<Project> projects;
	private List<Team> teams;
	private List<Sprint> sprints;	
	private int numRowOfTeam;
	private int numRowOfProject;
	private long projectId;
	private long teamId;
	private long sprintId;
	private Sprint selectedSprint;
	private boolean displayMeetingVideo;
	private boolean displayChart;
	private List<History> historyOfProject;
	private List<History> historyOfTeam;
	private boolean showMoreBtnP = false;
	private boolean showMoreBtnT = false;
	private List<Object> historyOfIssue;
	private int numRowIssueHistory;
	private int sizeOfHistoryIssue;
	private int startRowHistoryIssue = START_ROW;
	private int endRowHistoryIssue = NUM_HISTORY;
	private boolean showMoreIssueHistoryButton;
	private Date dateStartFilterHistoryIssue;
	private Date dateEndFilterHistoryIssue;
	private List<Date> minAndMaxDate;
	private List<KanbanHistory> kanbanIssueHistories = new ArrayList<KanbanHistory>();
	private int startRowHistoryKanbanIssue = START_ROW;
	private int endRowHistoryKanbanIssue = NUM_HISTORY;
	private boolean showMoreKanbanIssueHistoryButton = false;
	private Date dateStartFilterHistoryKanbanIssue;
	private Date dateEndFilterHistoryKanbanIssue;
	private List<Date> minAndMaxDateKanbanIssue;
	private static final Logger LOGGER = Logger.getLogger(HomeBean.class);


	

	
	public void syncBeanData() {
		if (!RequestContext.getCurrentInstance().isAjaxRequest()) {
			this.projects = this.projectService.findByMemberAndOwner(this.utils.getLoggedInMember().getMemberId());
			verifyProjectId();
			verifyTeamId();
			verifySprintId();
			this.chartBean.setSprintId(this.sprintId);
			this.productBacklogBean.setValueForUsList();
			setDefaultHistoryList();
			handleChangeForIssueHistory();
			setDefaultIssueHistoryList();
			findDateRangeOfKanbanIssueHistoryByTeam();
			loadKanbanHistoryDefault();
			
		}

	}
	
	private void loadKanbanHistoryDefault() {
		kanbanIssueHistories = kanbanHistoryService.findByTeamAndFilter(this.teamId,this.startRowHistoryKanbanIssue,this.endRowHistoryKanbanIssue + 1,dateStartFilterHistoryKanbanIssue,dateEndFilterHistoryKanbanIssue);
		int sizeKanbanIssueHistory = kanbanIssueHistories.size();
		if(sizeKanbanIssueHistory > endRowHistoryKanbanIssue) {
			showMoreKanbanIssueHistoryButton = true;
			kanbanIssueHistories.remove(sizeKanbanIssueHistory - 1);
		} else {
			showMoreKanbanIssueHistoryButton = false;
		}
	}
	
	private void findDateRangeOfKanbanIssueHistoryByTeam(){
		List<Date> list = kanbanHistoryService.findDateRangeOfKanbanIssueHistoryByTeamId(this.teamId);
		this.minAndMaxDateKanbanIssue = list;
		if(list != null && !list.isEmpty()){
			dateStartFilterHistoryKanbanIssue = list.get(0);
			dateEndFilterHistoryKanbanIssue = list.get(1);
		}
	}
	
	public void getMoreHistoryKanbanIssue() {
		this.startRowHistoryKanbanIssue += this.endRowHistoryKanbanIssue;
		List<KanbanHistory> loadMoreKanbanHistory = kanbanHistoryService.findByTeamAndFilter(this.teamId,this.startRowHistoryKanbanIssue,this.endRowHistoryKanbanIssue + 1,dateStartFilterHistoryKanbanIssue,dateEndFilterHistoryKanbanIssue);
		if(loadMoreKanbanHistory.size() > endRowHistoryKanbanIssue) {
			loadMoreKanbanHistory.remove(loadMoreKanbanHistory.size() - 1);
			kanbanIssueHistories.addAll(loadMoreKanbanHistory);
			showMoreKanbanIssueHistoryButton = true;
		} else {
			kanbanIssueHistories.addAll(loadMoreKanbanHistory);
			showMoreKanbanIssueHistoryButton = false;
		}
	}
	
	public void handleChangeDateStartKanbanIssueHistory() {
		this.startRowHistoryIssue = START_ROW;
		this.endRowHistoryIssue = NUM_HISTORY;
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(dateStartFilterHistoryKanbanIssue);
		cal.add(Calendar.HOUR, ZERO);
		cal.add(Calendar.MINUTE, ZERO);
		cal.add(Calendar.SECOND, ZERO);
		Date newDate = cal.getTime();
		dateStartFilterHistoryKanbanIssue = newDate;
		kanbanIssueHistories = kanbanHistoryService.findByTeamAndFilter(this.teamId,this.startRowHistoryKanbanIssue,this.endRowHistoryKanbanIssue + 1,dateStartFilterHistoryKanbanIssue,dateEndFilterHistoryKanbanIssue);
		int sizeKanbanIssueHistory = kanbanIssueHistories.size();
		if(sizeKanbanIssueHistory > endRowHistoryKanbanIssue) {
			showMoreKanbanIssueHistoryButton = true;
			kanbanIssueHistories.remove(sizeKanbanIssueHistory - 1);
		} else {
			showMoreKanbanIssueHistoryButton = false;
		}
	}
	
	public void handleChangeDateEndKanbanIssueHistory() {
		this.startRowHistoryIssue = START_ROW;
		this.endRowHistoryIssue = NUM_HISTORY;
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(dateEndFilterHistoryKanbanIssue);
		cal.add(Calendar.HOUR, HOUR_ENDDAY);
		cal.add(Calendar.MINUTE, MIN_ENDDAY);
		cal.add(Calendar.SECOND, MIN_ENDDAY);
		Date newDate = cal.getTime();
		dateEndFilterHistoryKanbanIssue = newDate;
		
		kanbanIssueHistories = kanbanHistoryService.findByTeamAndFilter(this.teamId,this.startRowHistoryKanbanIssue,this.endRowHistoryKanbanIssue + 1,dateStartFilterHistoryKanbanIssue,dateEndFilterHistoryKanbanIssue);
		int sizeKanbanIssueHistory = kanbanIssueHistories.size();
		if(sizeKanbanIssueHistory > endRowHistoryKanbanIssue) {
			showMoreKanbanIssueHistoryButton = true;
			kanbanIssueHistories.remove(sizeKanbanIssueHistory - 1);
		} else {
			showMoreKanbanIssueHistoryButton = false;
		}
	}
	
	/**
	 * Update selected Project in HomeBean and Product Backlog Bean, and update selected Team value in Team drop down list in Home Bean
	 * @param selectedProjectId - ID of selected project, long number format
	 */
	public void selectProject(long selectedProjectId) {
		this.projectId = selectedProjectId;
		this.teams = this.teamService.findTeamsByProjectIdAndMemberId(this.projectId, this.utils.getLoggedInMember().getMemberId());
		try {
			selectTeam(this.teams.get(0).getTeamId());
		} catch (Exception e) {
			selectTeam(0);
		}
		this.productBacklogBean.setProjectId(selectedProjectId);
		this.productBacklogBean.setValueForUsList();
		setDefaultHistoryList();
	}
	/**
	 * Update selected Team value and default selected Sprint in Home Bean.
	 * Besides, set default value for selected Project and selected Team in Retrospective Result Bean.
	 * @param selectedTeamId - ID of selected Team, long number format
	 */
	public void selectTeam(long selectedTeamId) {
		this.teamId = selectedTeamId;
		this.sprints = this.sprintService.findSprintsByTeamId(this.teamId);
		try {
			selectSprint(this.sprints.get(0).getSprintId());
		} catch (Exception e) {
			selectSprint(0);
		}

		if (this.projectId != 0 && this.teamId != 0) {
			this.retrospectiveResultBean.setProjectID(this.projectId + "");
			this.retrospectiveResultBean.setTeamID(this.teamId + "");
		}
		setDefaultHistoryList();
	}
	/**
	 * Update selected Sprint in Home Bean.
	 * Besides, set value for selected Sprint in Chart Bean and update Meeting Date in Meeting Video Bean.
	 * @param selectedSprintId - ID of Sprint, long number format
	 */
	public void selectSprint(long selectedSprintId) {
		this.sprintId = selectedSprintId;

		setSelectedSprint(this.sprintService.findSprintById(this.sprintId));
		this.chartBean.setSprintId(this.sprintId);
		try {
			this.meetingVideoBean.setMeetingDate(this.selectedSprint.getDateStart());
			this.meetingVideoBean.meetingDateChange(this.sprintId);
		} catch (Exception e) {
		    LOGGER.debug(e);
		}
	}

	public void verifyProjectId() {
		try {
			boolean isContain = false;
			for (Project p : this.projects) {
				if (p.getProjectId() == this.projectId) {
					isContain = true;
					break;
				}
			}
			if (!isContain) {
				selectProject(this.projects.get(0).getProjectId());
			} else {
				this.teams = this.teamService.findTeamsByProjectIdAndMemberId(this.projectId, this.utils.getLoggedInMember().getMemberId());
			}
		} catch (Exception e) {
			selectProject(0);
		}

		if (this.projectId != 0 && this.teamId != 0) {
			this.retrospectiveResultBean.setProjectID(this.projectId + "");
			this.retrospectiveResultBean.setTeamID(this.teamId + "");
		}
	}

	public void verifyTeamId() {
		try {
			boolean isContain = false;
			for (Team t : this.teams) {
				if (t.getTeamId() == this.teamId) {
					isContain = true;
					break;
				}
			}
			if (!isContain) {
				selectTeam(this.teams.get(0).getTeamId());
			} else {
				this.sprints = this.sprintService.findSprintsByTeamId(this.teamId);
			}
		} catch (Exception e) {
			selectTeam(0);
		}
	}

	public void verifySprintId() {
		try {
			boolean isContain = false;
			for (Sprint sp : this.sprints) {
				if (sp.getSprintId() == this.sprintId) {
					isContain = true;
					break;
				}
			}
			if (!isContain) {
				selectSprint(this.sprints.get(0).getSprintId());
			}
		} catch (Exception e) {
			selectSprint(0);
		}
	}

	public boolean isDisplayMeetingVideo() {
		if (this.projects == null || this.teams == null || this.sprints == null) {
			this.displayMeetingVideo = false;
		} else {
			if (this.projects.size() == 0 || this.teams.size() == 0 || this.sprints.size() == 0) {
				this.displayMeetingVideo = false;
			} else {
				this.displayMeetingVideo = true;
			}
		}
		return this.displayMeetingVideo;
	}

	public void setDisplayMeetingVideo(boolean displayMeetingVideo) {
		this.displayMeetingVideo = displayMeetingVideo;
	}

	public boolean isDisplayChart() {
		return this.displayChart;
	}

	public void setDisplayChart(boolean displayChart) {
		this.displayChart = displayChart;
	}

	public void setDefaultHistoryList() {
		this.numRowOfTeam = ZERO;
		this.numRowOfProject = 0;
		this.historyOfProject = this.historyService.findHistoryByContainerWithNumberRow("Project", this.projectId, getNumRowOfProject());
		this.historyOfTeam = this.historyService.findHistoryByContainerWithNumberRow("Team", this.teamId, getNumRowOfTeam());
		ClientTimeOffsetConverter.transformServerTimeToClientTime(this.historyOfProject);
		ClientTimeOffsetConverter.transformServerTimeToClientTime(this.historyOfTeam);
		int size = this.historyOfProject.size();
		if (size > getNumRowOfProject() + NUM_HISTORY) {
			this.historyOfProject.remove(size - 1);
			setShowMoreBtnP(true);
		} else {
			setShowMoreBtnP(false);
		}
		size = this.historyOfTeam.size();
		if (size > getNumRowOfTeam() + NUM_HISTORY) {
			this.historyOfTeam.remove(size - 1);
			setShowMoreBtnT(true);
		} else {
			setShowMoreBtnT(false);
		}
	}

	public void getMoreHistoryProject() {
		List<History> listHistoryTemp = new ArrayList<History>();
		setNumRowOfProject(NUM_HISTORY);
		listHistoryTemp = this.historyService.findHistoryByContainerWithNumberRow("Project", this.projectId, getNumRowOfProject());
		ClientTimeOffsetConverter.transformServerTimeToClientTime(listHistoryTemp);
		this.historyOfProject.addAll(listHistoryTemp);
		int size = this.historyOfProject.size();
		if (size > getNumRowOfProject() + NUM_HISTORY) {
			this.historyOfProject.remove(size - 1);
			setShowMoreBtnP(true);
		} else {
			setShowMoreBtnP(false);
		}

	}

	public void getMoreHistoryTeam() {
		List<History> listHistoryTemp = new ArrayList<History>();
		setNumRowOfTeam(NUM_HISTORY);
		listHistoryTemp = this.historyService.findHistoryByContainerWithNumberRow("Team", this.teamId, getNumRowOfTeam());
		ClientTimeOffsetConverter.transformServerTimeToClientTime(listHistoryTemp);
		this.historyOfTeam.addAll(listHistoryTemp);
		int size = this.historyOfTeam.size();
		if (size > getNumRowOfTeam() + NUM_HISTORY) {
			this.historyOfTeam.remove(size - 1);
			setShowMoreBtnT(true);
		} else {
			setShowMoreBtnT(false);
		}
	}
	/**
	 * Get 5 more history of issue and set button Show more is display or not
	 */
	public void getMoreHistoryIssue(){		
		this.startRowHistoryIssue += this.endRowHistoryIssue;
		setHourForEndDateIssueHistory();
		List<Object> tempList = historyService.findIssueHistoryBySprintWithStartAndEndRow(getSprintId(), getStartRowHistoryIssue(), getEndRowHistoryIssue()+1,getDateStartFilterHistoryIssue(), getDateEndFilterHistoryIssue());
		int size = this.historyOfIssue.size();
		ClientTimeOffsetConverter.setOffsetTimeForIssueHistory(tempList, size);
		this.historyOfIssue.addAll(tempList);	
		this.numRowIssueHistory = size;
		if(size > this.endRowHistoryIssue + this.startRowHistoryIssue){
			this.historyOfIssue.remove( size - 1 );
			this.showMoreIssueHistoryButton = true;
		}
		else {
			this.showMoreIssueHistoryButton = false;
		}
	}
	/**
	 * Reset value of list issue history when selecting value project, team, and sprint
	 */
	
	public void setDefaultIssueHistoryList()
	{
		this.historyOfIssue = historyService.findIssueHistoryBySprintWithStartAndEndRow(getSprintId(), getStartRowHistoryIssue(), 6,getDateStartFilterHistoryIssue(), getDateEndFilterHistoryIssue());
		int size = 0;
		this.showMoreIssueHistoryButton = false;
		
		if (historyOfIssue != null)
		{
		    size = this.historyOfIssue.size();
		    ClientTimeOffsetConverter.setOffsetTimeForIssueHistory(this.historyOfIssue, size);
        		
		    if(size > this.endRowHistoryIssue)
		    {
        		this.historyOfIssue.remove( size - 1 );
        		this.showMoreIssueHistoryButton = true;
		    }
		}
		
		if(size == 0){
			setStartAndEndDateFilterHistoryIssue(new Date(), new Date());
		}
	}
	/**
	 * Set hour for date end with hour at 23:59:59
	 */
	public void setHourForEndDateIssueHistory(){
		Calendar cal = Calendar.getInstance();	
		cal.clear();
		cal.setTime(this.dateEndFilterHistoryIssue);
		cal.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH), cal.get(Calendar.DATE), HOUR_ENDDAY, MIN_ENDDAY, MIN_ENDDAY);
		Calendar cal2 = Calendar.getInstance();	
		cal2.clear();
		cal2.set(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH), cal.get(Calendar.DATE), ZERO, ZERO, ZERO);
		if(this.dateEndFilterHistoryIssue.getTime() == cal2.getTimeInMillis()){
			setDateEndFilterHistoryIssue(cal.getTime());
		}
	}
	/**
	 * Reset number of start row and end row for issue history list
	 */
	public void resetNumberRowHistoryIssue(){
		setStartRowHistoryIssue(START_ROW);
		setEndRowHistoryIssue(NUM_HISTORY);	
	}

	public void setStartAndEndDateFilterHistoryIssue(Date dateStart, Date dateEnd){
		setDateStartFilterHistoryIssue(dateStart);
		setDateEndFilterHistoryIssue(dateEnd);
	}

	/**
	 * Reset value for start date and end date for filter in view.
	 *  Besides, reset how much history rows shown
	 */
	public void handleChangeForIssueHistory(){
		List<Date> list = historyService.findDateRangeOfIssueHistoryBySprintId(getSprintId());
		this.minAndMaxDate = list;
		if(list != null && !list.isEmpty()){
			setStartAndEndDateFilterHistoryIssue(list.get(0), list.get(1));			
		}
		resetNumberRowHistoryIssue();
		setDefaultIssueHistoryList();
	}

	/**
	 * Set end date selected in view with time at last of date, which is 23:59:59 
	 * to make sure all history created is shown in period time selected in view
	 * @param event - SelectEvent
	 */
	public void handleChangeDateEndIssueHistory(SelectEvent event){
		this.startRowHistoryIssue = START_ROW;
		this.endRowHistoryIssue = NUM_HISTORY;
		Date date = (Date) event.getObject();
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(dateEndFilterHistoryIssue);
		cal.add(Calendar.HOUR, HOUR_ENDDAY);
		cal.add(Calendar.MINUTE, MIN_ENDDAY);
		cal.add(Calendar.SECOND, MIN_ENDDAY);
		date = cal.getTime();
		setDateEndFilterHistoryIssue(date);
		this.historyOfIssue = historyService.findIssueHistoryBySprintWithStartAndEndRow(getSprintId(), this.startRowHistoryIssue, this.endRowHistoryIssue+1,getDateStartFilterHistoryIssue(), getDateEndFilterHistoryIssue());
		int size = this.historyOfIssue.size();
		ClientTimeOffsetConverter.setOffsetTimeForIssueHistory(this.historyOfIssue, size);
		if(size>this.endRowHistoryIssue+this.startRowHistoryIssue){
			this.historyOfIssue.remove(size-1);
			this.showMoreIssueHistoryButton=true;
		}
		else {
			this.showMoreIssueHistoryButton=false;
		}
	}
	/**
	 * Set start date selected in view with time at last of date, which is 00:00:00 
	 * to make sure all history created is shown in period time selected in view
	 * @param event - SelectEvent
	 */
	public void handleChangeDateStartIssueHistory(SelectEvent event){
		this.startRowHistoryIssue = START_ROW;
		this.endRowHistoryIssue = NUM_HISTORY;
		Date date = (Date) event.getObject();
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.setTime(dateEndFilterHistoryIssue);
		cal.add(Calendar.HOUR, ZERO);
		cal.add(Calendar.MINUTE, ZERO);
		cal.add(Calendar.SECOND, ZERO);
		date = cal.getTime();
		setDateEndFilterHistoryIssue(date);
		this.historyOfIssue = historyService.findIssueHistoryBySprintWithStartAndEndRow(getSprintId(), this.startRowHistoryIssue, this.endRowHistoryIssue+1,getDateStartFilterHistoryIssue(), getDateEndFilterHistoryIssue());
		int size = this.historyOfIssue.size();
		ClientTimeOffsetConverter.setOffsetTimeForIssueHistory(this.historyOfIssue, size);
		if(size>this.endRowHistoryIssue+this.startRowHistoryIssue){
			this.historyOfIssue.remove(size-1);
			this.showMoreIssueHistoryButton=true;
		}
		else {
			this.showMoreIssueHistoryButton=false;
		}
	}
	
	public List<Object> getHistoryOfIssue() {
		return this.historyOfIssue;
	}

	public int getNumRowOfTeam() {
		return numRowOfTeam;
	}

	public void setNumRowOfTeam(int numRowOfTeam) {
		this.numRowOfTeam += numRowOfTeam;
	}

	public int getNumRowOfProject() {
		return numRowOfProject;
	}

	public void setNumRowOfProject(int numRowofProject) {
		this.numRowOfProject += numRowofProject;
	}

	public List<Project> getProjects() {
		if(projects == null){
			projects = new ArrayList<Project>();
		}
		return this.projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public List<Team> getTeams() {
		if(teams == null){
			teams = new ArrayList<Team>();
		}
		return this.teams;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}

	public List<Sprint> getSprints() {
		if(sprints == null){
			sprints = new ArrayList<Sprint>();
		}
		return this.sprints;
	}

	public void setSprints(List<Sprint> sprints) {
		this.sprints = sprints;
	}

	public long getProjectId() {
		return this.projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public long getTeamId() {
		return this.teamId;
	}

	public void setTeamId(long teamId) {
		this.teamId = teamId;
	}

	public long getSprintId() {
		return this.sprintId;
	}

	public void setSprintId(long sprintId) {
		this.sprintId = sprintId;
	}

	public Sprint getSelectedSprint() {
		return this.selectedSprint;
	}

	public void setSelectedSprint(Sprint selectedSprint) {
		this.selectedSprint = selectedSprint;
	}

	public List<History> getHistoryOfProject() {
		return this.historyOfProject;
	}

	public void setHistoryOfProject(List<History> historyOfProject) {
		this.historyOfProject = historyOfProject;
	}

	public List<History> getHistoryOfTeam() {
		return historyOfTeam;
	}

	public void setHistoryOfTeam(List<History> historyOfTeam) {
		this.historyOfTeam = historyOfTeam;
	}

	public boolean isShowMoreBtnP() {
		return showMoreBtnP;
	}

	public void setShowMoreBtnP(boolean showMoreBtnP) {
		this.showMoreBtnP = showMoreBtnP;
	}

	public boolean isShowMoreBtnT() {
		return showMoreBtnT;
	}

	public void setShowMoreBtnT(boolean showMoreBtnT) {
		this.showMoreBtnT = showMoreBtnT;
	}


	public void setHistoryOfIssue(List<Object> historyOfIssue) {
		this.historyOfIssue = historyOfIssue;
	}
	public int getStartRowHistoryIssue() {
		return startRowHistoryIssue;
	}
	public int getEndRowHistoryIssue() {
		return endRowHistoryIssue;
	}
	public void setEndRowHistoryIssue(int endRowHistoryIssue) {
		this.endRowHistoryIssue = endRowHistoryIssue;
	}
	public void setStartRowHistoryIssue(int startRowHistoryIssue) {
		this.startRowHistoryIssue = startRowHistoryIssue;
	}
	public boolean isShowMoreIssueHistoryButton() {
		return showMoreIssueHistoryButton;
	}
	public Date getDateStartFilterHistoryIssue() {
		return dateStartFilterHistoryIssue;
	}
	public void setDateStartFilterHistoryIssue(Date dateStartFilterHistoryIssue) {
		this.dateStartFilterHistoryIssue = dateStartFilterHistoryIssue;
	}
	public void setShowMoreIssueHistoryButton(boolean showMoreIssueHistoryButton) {
		this.showMoreIssueHistoryButton = showMoreIssueHistoryButton;
	}
	public Date getDateEndFilterHistoryIssue() {
		return dateEndFilterHistoryIssue;
	}
	public void setDateEndFilterHistoryIssue(Date dateEndFilterHistoryIssue) {
		this.dateEndFilterHistoryIssue = dateEndFilterHistoryIssue;
	}
	public int getSizeOfHistoryIssue() {
		return sizeOfHistoryIssue;
	}
	public void setSizeOfHistoryIssue(int sizeOfHistoryIssue) {
		this.sizeOfHistoryIssue = sizeOfHistoryIssue;
	}

	public List<Date> getMinAndMaxDate() {
		return minAndMaxDate;
	}
	public void setMinAndMaxDate(List<Date> minAndMaxDate) {
		this.minAndMaxDate = minAndMaxDate;
	}
	public int getNumRowIssueHistory() {
		return numRowIssueHistory;
	}
	public void setNumRowIssueHistory(int numRowIssueHistory) {
		this.numRowIssueHistory = numRowIssueHistory;
	}

	public List<KanbanHistory> getKanbanIssueHistories() {
		return kanbanIssueHistories;
	}

	public void setKanbanIssueHistories(List<KanbanHistory> kanbanIssueHistories) {
		this.kanbanIssueHistories = kanbanIssueHistories;
	}

	public boolean isShowMoreKanbanIssueHistoryButton() {
		return showMoreKanbanIssueHistoryButton;
	}

	public void setShowMoreKanbanIssueHistoryButton(
			boolean showMoreKanbanIssueHistoryButton) {
		this.showMoreKanbanIssueHistoryButton = showMoreKanbanIssueHistoryButton;
	}

	public Date getDateStartFilterHistoryKanbanIssue() {
		return dateStartFilterHistoryKanbanIssue;
	}

	public void setDateStartFilterHistoryKanbanIssue(
			Date dateStartFilterHistoryKanbanIssue) {
		this.dateStartFilterHistoryKanbanIssue = dateStartFilterHistoryKanbanIssue;
	}

	public Date getDateEndFilterHistoryKanbanIssue() {
		return dateEndFilterHistoryKanbanIssue;
	}

	public void setDateEndFilterHistoryKanbanIssue(
			Date dateEndFilterHistoryKanbanIssue) {
		this.dateEndFilterHistoryKanbanIssue = dateEndFilterHistoryKanbanIssue;
	}

	public List<Date> getMinAndMaxDateKanbanIssue() {
		return minAndMaxDateKanbanIssue;
	}

	public void setMinAndMaxDateKanbanIssue(List<Date> minAndMaxDateKanbanIssue) {
		this.minAndMaxDateKanbanIssue = minAndMaxDateKanbanIssue;
	}
	
}