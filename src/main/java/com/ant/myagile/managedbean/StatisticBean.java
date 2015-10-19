package com.ant.myagile.managedbean;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.primefaces.json.JSONArray;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Holiday;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.SprintStatistic;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.HolidayService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.SprintStatisticService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("statisticBean")
@Scope("session")
public class StatisticBean
{
	public static final int DEFAULT_SPRINT_DAYS = 14;
	public static final int WORKING_DAYS = 5;
	public static final Logger LOGGER = Logger.getLogger(StatisticBean.class);
	
	private Sprint sprint;
	private List<Sprint> sprintList;
	private List<Team> teamList;
	private Sprint selectedSprint;
	private SprintStatistic statistic;
	private String selectedChart = "velocity";
	private JSONObject statisticAllDataJSON = new JSONObject();
	private Sprint selectedDropDownSprint;
	private String selectedDropDownSprintId;
	private String selectedDropDownTeamId;

	@Autowired
	private TeamService teamService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	Utils utils;
	@Autowired
	private SprintStatisticService sprintStatisticService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private HolidayService holidayService;
	@Autowired
	private FeedbackBean feedbackBean;
	@Autowired
	private ProjectService projectService;

	private CartesianChartModel combinedModel;
	private ChartSeries seriesPlan;
	private ChartSeries seriesDelivered;
	private String[][] arrayAllStatistics;

	private Date selectedHoliday;
	private String selectedLeaveType;
	private String reasonLeave;
	private List<Holiday> holidayList;
	private float totalHoliday = 0;

	/**
	 * Get all holidays of login user and display it in client
	 * 
	 * @return
	 */
	public String getHolidayListOfMember() 
	{
		List<Holiday> holidayListOfMember = holidayService.findHolidayByMemberInSprint(this.utils.getLoggedInMember(), this.sprint);
		ArrayList<String> dataJSON = new ArrayList<String>();
		
		if (holidayListOfMember != null) 
		{
			for (int i = 0; i < holidayListOfMember.size(); i++) 
			{
				dataJSON.add(Utils.dateToString(holidayListOfMember.get(i).getLeaveDate()).split(" ")[0]);
			}
		}
		JSONArray arr = new JSONArray(dataJSON);
		return arr.toString();
	}

	/**
	 * Handle when user change date
	 */
	public void handleChangeHoliday() 
	{
		try 
		{
			JSFUtils.resetForm("holidayForm");
			Member memberOwner = this.utils.getLoggedInMember();
			Holiday holiday = holidayService.findHolidayByDateAndMemberInSprint(selectedHoliday, memberOwner, this.sprint);
			
			if (holiday != null) {
				this.selectedLeaveType = String.valueOf(holiday.getLeaveType());
				this.reasonLeave = holiday.getReason();
			} else {
				this.selectedLeaveType = null;
				this.reasonLeave = "";
			}
		} catch (Exception e) {
			LOGGER.error("Error: " + e.getMessage());
		}
	}

	/**
	 * Update holiday list
	 */
	public void updateHolidayList()
	{
		try
		{
			this.holidayList = holidayService.findHolidayBySprint(this.sprint);
			this.totalHoliday = 0;
			
			if (holidayList != null) 
			{
				for (int i = 0; i < holidayList.size(); i++) 
				{
					if (holidayList.get(i).getLeaveType().equals(Holiday.StatusType.FULLDAY)) {
						totalHoliday += 1;
					} else {
						totalHoliday += 0.5;
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error: " + e.getMessage());
		}
	}

	/**
	 * Save holiday of user into db
	 */
	public void saveHoliday() 
	{
		try {
			Holiday holidayInDate = holidayService.findHolidayByDateAndMemberInSprint(selectedHoliday, utils.getLoggedInMember(), this.sprint);
			if (holidayInDate == null) {
				holidayInDate = new Holiday();
			}

			holidayInDate.setLeaveDate(selectedHoliday);
			holidayInDate.setMember(utils.getLoggedInMember());
			holidayInDate.setReason(reasonLeave);
			holidayInDate.setLeaveType(Holiday.StatusType.valueOf(this.selectedLeaveType));
			holidayInDate.setSprint(this.sprint);

			holidayService.save(holidayInDate);
			updateHolidayList();

		} catch (Exception e) {
			LOGGER.error("Error: " + e.getMessage());
		}
	}

	/**
	 * Remove holiday of user
	 */
	public void removeHoliday() 
	{
		try {
			Holiday holiday = holidayService.findHolidayByDateAndMemberInSprint(selectedHoliday, this.utils.getLoggedInMember(), this.sprint);
			holidayService.delete(holiday);
			this.selectedLeaveType = null;
			this.reasonLeave = "";
			updateHolidayList();
		} catch (Exception e) {
			LOGGER.error("Error: " + e.getMessage());
		}
	}

	private void prepareChart()
	{
		combinedModel = new CartesianChartModel();
		seriesPlan = new ChartSeries();
		seriesDelivered = new ChartSeries();
		if (selectedChart.equals("velocity")) {
			seriesPlan.setLabel("Planned velocity");
			seriesDelivered.setLabel("Accepted velocity");
		} else if (selectedChart.equals("story")) {
			seriesPlan.setLabel("Planned story");
			seriesDelivered.setLabel("Accepted story");
		} else if (selectedChart.equals("point")) {
			seriesPlan.setLabel("Planned point");
			seriesDelivered.setLabel("Accepted point");
		}
	}

	public void createDataForChart(SprintStatistic statistic, Sprint sprint, ChartSeries plan, ChartSeries delivered) 
	{
		if (statistic != null) 
		{
			if (this.selectedChart.equals("velocity")) {
				this.seriesPlan.set(sprint.getSprintName(), Float.parseFloat(Utils.formatFloatNumber(statistic.getVelocityOfSprintPlan(), "#0.00")));
				this.seriesDelivered.set(sprint.getSprintName(), Float.parseFloat(Utils.formatFloatNumber(statistic.getVelocityOfSprintDelivered(), "#0.00")));
			} else if (this.selectedChart.equals("story")) {
				this.seriesPlan.set(sprint.getSprintName(), statistic.getUserStoryPlan());
				this.seriesDelivered.set(sprint.getSprintName(), statistic.getUserStoryDelivered());
			} else if (this.selectedChart.equals("point")) {
				this.seriesPlan.set(sprint.getSprintName(), statistic.getPointPlan());
				this.seriesDelivered.set(sprint.getSprintName(), statistic.getPointDelivered());
			}
		} else {
			this.seriesPlan.set(sprint.getSprintName(), 0);
			this.seriesDelivered.set(sprint.getSprintName(), 0);
		}
	}

	public void createDataForGrid(SprintStatistic statistic, int index, String[][] dataJson) 
	{
		if (statistic != null) 
		{
			dataJson[index][0] = statistic.getSprintSize() + "";
			dataJson[index][1] = statistic.getAvailableManDay() + "";
			dataJson[index][2] = statistic.getTeamSize() + "";
			dataJson[index][3] = statistic.getUserStoryPlan() + "";
			dataJson[index][4] = statistic.getUserStoryDelivered() + "";
			dataJson[index][5] = statistic.getPointPlan() + "";
			dataJson[index][6] = statistic.getPointDelivered() + "";
			dataJson[index][7] = Utils.formatFloatNumber(statistic.getVelocityOfSprintPlan(), "#0.00");
			dataJson[index][8] = Utils.formatFloatNumber(statistic.getVelocityOfSprintDelivered(), "#0.00");
		}
	}

	public void handleStatisticAllSprint() 
	{
		try {
			prepareChart();
			this.arrayAllStatistics = new String[this.sprintList.size()][9];
			if (this.sprintList != null && this.sprintList.size() > 0) {
				for (int i = this.sprintList.size() - 1; i >= 0; i--) {
					SprintStatistic statistic = this.sprintStatisticService.findSprintStatisticBySprint(this.sprintList.get(i));
					createDataForChart(statistic, this.sprintList.get(i), this.seriesPlan, this.seriesDelivered);
					createDataForGrid(statistic, i, this.arrayAllStatistics);
				}
				this.statisticAllDataJSON.put("data", this.arrayAllStatistics);
				this.combinedModel.addSeries(this.seriesPlan);
				this.combinedModel.addSeries(this.seriesDelivered);
			}
		} catch (Exception e) {
			LOGGER.error("Error: " + e.getMessage());
		}
	}

	public void redirectToAllSprintStatisticPage() 
	{
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("statisticsall");
		} catch (IOException e) {
			LOGGER.error("Error: " + e.getMessage());
		}
	}

	public void initPreViewStatisticsAll() throws IOException 
	{
		try {
			initPreview();
			handleStatisticAllSprint();
		} catch (Exception e) {
			LOGGER.error("Error: " + e.getMessage());
		}
	}

	public void initPreview() throws IOException 
	{
		if (JSFUtils.isPostbackRequired()) 
		{
			try 
			{
				List<Team> allTeams = this.teamService.findTeamsByMemberAndOwner(utils.getLoggedInMember());
				excludeHiddenTeamsForSelection(allTeams);
				
				if (this.teamList != null && !this.teamList.isEmpty()) 
				{
					if (this.selectedDropDownTeamId != null) 
					{
						getSelectedDropDownTeamId();
						setSprintList(this.sprintService.findSprintsByTeamId(Long.valueOf(this.selectedDropDownTeamId)));
					} else if (this.sprintList == null || this.sprintList.isEmpty()) {
						setSprintList(this.sprintService.findSprintsByTeamId(this.teamList.get(0).getTeamId()));
					}

					if (this.sprintList != null && this.sprintList.size() > 0) {
						setSelectedDropDownSprint(sprintService.findSprintById(Long.valueOf(getSelectedDropDownSprintId())));
						this.sprint = sprintService.findSprintById(Long.valueOf(getSelectedDropDownSprintId()));
						this.selectedHoliday = sprint.getDateEnd();
					}

					if (this.sprint == null) {
						this.sprint = new Sprint();
					}
					if (this.sprint.getTeam() == null) {
						this.sprint.setTeam(this.teamList.get(0));
					}
				}
			} catch (Exception e) {
				FacesContext.getCurrentInstance().getExternalContext().redirect("/myagile");
			}

			initStatisticsInformation();
			updateHolidayList();
		}
	}

	/**
	 * Calculate and initialize information about point plan, point delivered
	 * userstory plan, userstory delivered velocity of sprint plan, velocity of
	 * sprint delivered
	 * 
	 * @throws IOException
	 */
	public void initStatisticsInformation() throws IOException 
	{
		SprintStatistic sprintStatistic = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		this.sprintStatisticService.initStatisticsInformation(sprintStatistic);
	}

	public void handleChangeTeamFromStatisticsPage() 
	{
		resetStatisticForm();
		long teamId = Long.valueOf(this.selectedDropDownTeamId);
		List<Project> projectList = this.projectService.filterTeamProjectsWithMemberId(teamId, utils.getLoggedInMember().getMemberId());
		if(projectList.size() > 0){
			feedbackBean.setSelectedProjectId(String.valueOf(projectList.get(0).getProjectId()));
		}
	
		this.sprintList = this.sprintService.findSprintsByTeamId(teamId);
		if (this.sprintList != null && !this.sprintList.isEmpty()) {
			setSelectedDropDownSprint(this.sprintList.get(0));
			setSelectedDropDownSprintId(String.valueOf(this.sprintList.get(0).getSprintId()));
			this.sprint = sprintService.findSprintById(Long.valueOf(selectedDropDownSprintId));
		} else {
			setSelectedDropDownSprint(new Sprint());
			setSelectedDropDownSprintId("-1");
			this.sprint = new Sprint();
		}

		resetHolidayForm();

	}

	/**
	 * Set value for selectedSprint on Sprint Statistic and Update Sprint
	 * Statistic Form
	 */
	public void handleChangeSprintDropDown()
	{
		setSelectedDropDownSprint(this.sprintService.findSprintById((Long) JSFUtils.getManagedBeanValue("issueBean.issue.sprint.sprintId")));
		JSFUtils.resetForm("statisticForm");
	}

	public void handleChangeSprintDropDownFromStatisticsPage()
	{
		setSelectedDropDownSprint(this.sprintService.findSprintById(Long.valueOf(this.selectedDropDownSprintId)));
		this.sprint = sprintService.findSprintById(Long.valueOf(selectedDropDownSprintId));
		resetStatisticForm();
		resetHolidayForm();
	}

	/**
	 * Reset all value in Sprint Statistic Form
	 */
	public void resetStatisticForm() {
		JSFUtils.resetForm("statisticForm");
	}

	/**
	 * Reset all value in Holiday Form
	 */
	public void resetHolidayForm()
	{
		JSFUtils.resetForm("holidayForm");
		this.selectedLeaveType = null;
		this.reasonLeave = "";
		this.selectedHoliday = sprint.getDateEnd();
		updateHolidayList();
		handleChangeHoliday();
	}

	/**
	 * Update team size of sprint statistic, then calculate and update Available
	 * man day and Velocity of Sprint
	 */
	public void updateTeamSizeStatistic()
	{
		int dateSprintWork = 1;
		Date temp = this.selectedDropDownSprint.getDateStart();
		while (temp.getTime() < this.selectedDropDownSprint.getDateEnd().getTime()) 
		{
			if (checkDayWork(temp)) {
				dateSprintWork++;
			}
			temp = Utils.addDays(temp, 1);
		}
		
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		sta.setTeamSize(this.statistic.getTeamSize() > 0 ? this.statistic.getTeamSize() : 0);
		int manDay = sta.getTeamSize() * dateSprintWork;
		sta.setAvailableManDay(manDay);
		
		if (manDay != (float) 0.0) 
		{
			float velocityOfSprintPlan = this.statistic.getPointPlan() / manDay;
			float velocityOfSprintDelivered = this.statistic.getPointDelivered() / manDay;
			sta.setVelocityOfSprintPlan(velocityOfSprintPlan);
			sta.setVelocityOfSprintDelivered(velocityOfSprintDelivered);
		} else {
			sta.setVelocityOfSprintPlan(0);
			sta.setVelocityOfSprintDelivered(0);
		}
		this.sprintStatisticService.update(sta);
		JSFUtils.resetForm("statisticForm");
		JSFUtils.addCallBackParam("save", true);
	}

	public void updateSprintSizeStatistic()
	{
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		sta.setSprintSize(this.statistic.getSprintSize() > 0 ? this.statistic.getSprintSize() : 0);
		this.sprintStatisticService.update(sta);
		JSFUtils.resetForm("statisticForm");
	}

	/**
	 * Update Available man day, then calculate and update Velocity of Sprint
	 */
	public void updateAvailableManDayStatistic() 
	{
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		sta.setAvailableManDay(this.statistic.getAvailableManDay() > (float) 0.0 ? this.statistic.getAvailableManDay() : (float) 0.0);
		
		if (sta.getAvailableManDay() != (float) 0.0) 
		{
			float velocityOfSprintPlan = this.statistic.getPointPlan() / sta.getAvailableManDay();
			float velocityOfSprintDelivered = this.statistic.getPointDelivered() / sta.getAvailableManDay();
			sta.setVelocityOfSprintPlan(velocityOfSprintPlan);
			sta.setVelocityOfSprintDelivered(velocityOfSprintDelivered);
		} else {
			sta.setVelocityOfSprintPlan(0);
			sta.setVelocityOfSprintDelivered(0);
		}
		sta.setVelocityOfSprintPlan(sta.getAvailableManDay() == (float) 0.0 ? (float) 0.0 : this.statistic.getPointPlan() / sta.getAvailableManDay());
		sta.setVelocityOfSprintDelivered(sta.getAvailableManDay() == (float) 0.0 ? (float) 0.0 : this.statistic.getPointDelivered() / sta.getAvailableManDay());
		this.sprintStatisticService.update(sta);
		JSFUtils.resetForm("statisticForm");
		JSFUtils.addCallBackParam("save", true);
	}

	public void updateNoteStatistic() 
	{
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		sta.setNote((this.statistic.getNote()));
		this.sprintStatisticService.update(sta);
		JSFUtils.resetForm("statisticForm");
	}

	public void updateUnitTeamSize()
	{
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		sta.setUnitTeamSize(this.statistic.getUnitTeamSize().equals("") ? "developer" : this.statistic.getUnitTeamSize());
		this.sprintStatisticService.update(sta);
		JSFUtils.resetForm("statisticForm");
	}

	public void updateUnitSprintSize() 
	{
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		sta.setUnitSprintSize(this.statistic.getUnitSprintSize().equals("") ? "week" : this.statistic.getUnitSprintSize());
		this.sprintStatisticService.update(sta);
		JSFUtils.resetForm("statisticForm");
	}

	public void updateUnitManDay() 
	{
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		sta.setUnitManDay(this.statistic.getUnitManDay().equals("") ? "day" : this.statistic.getUnitManDay());
		this.sprintStatisticService.update(sta);
		JSFUtils.resetForm("statisticForm");
	}

	public void updateUnitPoint() 
	{
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		sta.setUnitPoint(this.statistic.getUnitPoint().equals("") ? "point" : this.statistic.getUnitPoint());
		this.sprintStatisticService.update(sta);
		JSFUtils.resetForm("statisticForm");
		JSFUtils.addCallBackParam("save", true);
	}

	public void updateUnitStory()
	{
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		sta.setUnitStory(this.statistic.getUnitStory().equals("") ? "story" : this.statistic.getUnitStory());
		this.sprintStatisticService.update(sta);
		JSFUtils.resetForm("statisticForm");
		JSFUtils.addCallBackParam("save", true);
	}

	public void cancelUpdate()
	{
		JSFUtils.resetForm("statisticForm");
	}

	/**
	 * Automatically generate a sprint statistic based on current info and save
	 * it into database
	 */
	public void generateStatistic() 
	{
		try {
			SprintStatistic sta = new SprintStatistic();
			int dateSprintWork = 1;
			Date temp = this.selectedDropDownSprint.getDateStart();
			int teamSize = this.memberService.findDevelopmentMembersByTeamId(this.selectedDropDownSprint.getTeam().getTeamId()).size();
			
			while (temp.getTime() < this.selectedDropDownSprint.getDateEnd().getTime()) 
			{
				if (checkDayWork(temp)) {
					dateSprintWork++;
				}
				temp = Utils.addDays(temp, 1);
			}
			
			int manDay = teamSize * dateSprintWork;
			sta.setSprint(this.selectedDropDownSprint);
			sta.setAvailableManDay(manDay);
			sta.setSprintSize(Math.round(dateSprintWork / WORKING_DAYS) > 2 ? (int) Math.round(dateSprintWork / WORKING_DAYS) : 2);
			sta.setTeamSize(teamSize);
			sta.setNote("");
			this.sprintStatisticService.initStatisticsInformation(sta);
		} catch (Exception e) {
		    LOGGER.error("generateStatistic error: "+ e.getMessage());
		}

	}

	/**
	 * Check date is working day or not
	 * 
	 * @param date
	 *            - Date
	 * @return true if it is working date, false in otherwise
	 */
	public boolean checkDayWork(Date date) 
	{
		SimpleDateFormat formatter = new SimpleDateFormat("EEE");
		String text = formatter.format(date);
		if (text.equals("Sun") || text.equals("Sat")) {
			return false;
		}
		return true;
	}

	/**
	 * Generate sprint statistic again based on current info and update it in
	 * database
	 */
	public void regenerateStatistic() 
	{
		SprintStatistic sta = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
		int dateSprintWork = 1;
		Date temp = this.selectedDropDownSprint.getDateStart();
		int teamSize = this.memberService.findDevelopmentMembersByTeamId(this.selectedDropDownSprint.getTeam().getTeamId()).size();
		
		while (temp.getTime() < this.selectedDropDownSprint.getDateEnd().getTime()) 
		{
			if (checkDayWork(temp)) {
				dateSprintWork++;
			}
			temp = Utils.addDays(temp, 1);
		}
		
		int manDay = teamSize * dateSprintWork;
		sta.setSprint(this.selectedDropDownSprint);
		sta.setAvailableManDay(manDay);
		sta.setSprintSize(Math.round(dateSprintWork / WORKING_DAYS) > 2 ? (int) Math.round(dateSprintWork / WORKING_DAYS) : 2);
		sta.setTeamSize(teamSize);
		this.sprintStatisticService.initStatisticsInformation(sta);
	}

	public Sprint getSprint()
	{
		if (this.sprint == null) {
			this.sprint = new Sprint();
		}
		return this.sprint;
	}

	public void setSprint(Sprint sprint) 
	{
		this.sprint = sprint;
	}

	public List<Sprint> getSprintList() 
	{
		if (this.sprintList == null) {
			this.sprintList = new ArrayList<Sprint>();
		}
		return this.sprintList;
	}

	public Sprint getLastSprint() 
	{
		if (this.sprintList != null && this.sprintList.size() > 0) {
			if (this.sprintList.get(0) != null) {
				return this.sprintList.get(0);
			}
		}
		return null;
	}

	public void setSprintList(List<Sprint> sprintList)
	{
		this.sprintList = sprintList;
	}

	public List<Team> getTeamList()
	{
		if (this.teamList == null) {
			this.teamList = new ArrayList<Team>();
		}
		return this.teamList;
	}

	public void setTeamList(List<Team> teamList) 
	{
	    excludeHiddenTeamsForSelection(teamList);
	}

	public Sprint getSelectedSprint() {
		if (this.selectedSprint == null) {
			this.selectedSprint = new Sprint();
		}
		return this.selectedSprint;
	}

	public void setSelectedSprint(Sprint selectedSprint) {
		this.selectedSprint = selectedSprint;
	}

	public TeamService getTeamService() {
		return this.teamService;
	}

	public void setTeamService(TeamService teamService) {
		this.teamService = teamService;
	}

	public SprintService getSprintService() {
		return this.sprintService;
	}

	public void setSprintService(SprintService sprintService) {
		this.sprintService = sprintService;
	}

	public Sprint getSelectedDropDownSprint() {
		if (this.selectedDropDownSprint == null) {
			return new Sprint();
		}
		return this.selectedDropDownSprint;
	}

	public void setSelectedDropDownSprint(Sprint selectedDropDownSprint) {
		this.selectedDropDownSprint = selectedDropDownSprint;
	}

	public SprintStatistic getStatistic() 
	{
		try {
			this.statistic = this.sprintStatisticService.findSprintStatisticBySprint(this.selectedDropDownSprint);
			return this.statistic;
		} catch (Exception e) {
			return null;
		}
	}

	public void setStatistic(SprintStatistic statistic) {
		this.statistic = statistic;
	}

	public String getSelectedDropDownSprintId() 
	{
		if (this.selectedDropDownSprintId == null) {
			if (getSprintList() != null && getSprintList().size() > 0) {
				this.selectedDropDownSprintId = String.valueOf(getSprintList().get(0).getSprintId());
			} else {
				this.selectedDropDownSprintId = "-1";
			}
		}
		return this.selectedDropDownSprintId;
	}

	public void setSelectedDropDownSprintId(String selectedDropDownSprintId) {
		this.selectedDropDownSprintId = selectedDropDownSprintId;
	}

	public String getSelectedDropDownTeamId()
	{
		if (this.selectedDropDownTeamId == null || this.selectedDropDownTeamId.equals("-1"))
		{
			if (getTeamList() != null && getTeamList().size() > 0) {
				this.selectedDropDownTeamId = String.valueOf(getTeamList().get(0).getTeamId());
			} else {
				this.selectedDropDownTeamId = "-1";
			}
		}
		return this.selectedDropDownTeamId;
	}

	public void setSelectedDropDownTeamId(String selectedDropDownTeamId) {
		this.selectedDropDownTeamId = selectedDropDownTeamId;
	}

	public String getSelectedChart() {
		return this.selectedChart;
	}

	public void setSelectedChart(String selectedChart) {
		this.selectedChart = selectedChart;
	}

	public JSONObject getStatisticAllDataJSON() {
		return this.statisticAllDataJSON;
	}

	public void setStatisticAllDataJSON(JSONObject statisticAllDataJSON) {
		this.statisticAllDataJSON = statisticAllDataJSON;
	}

	public CartesianChartModel getCombinedModel() {
		return this.combinedModel;
	}

	public void setCombinedModel(CartesianChartModel combinedModel) {
		this.combinedModel = combinedModel;
	}

	public Date getSelectedHoliday() {
		return selectedHoliday;
	}

	public String getSelectedHolidayFormat() {
		return Utils.toShortDate(selectedHoliday);
	}

	public void setSelectedHoliday(Date selectedHoliday) {
		this.selectedHoliday = selectedHoliday;
	}

	public String getSelectedLeaveType() {
		return selectedLeaveType;
	}

	public void setSelectedLeaveType(String selectedLeaveType) {
		this.selectedLeaveType = selectedLeaveType;
	}

	public String getReasonLeave() {
		return reasonLeave;
	}

	public void setReasonLeave(String reasonLeave) {
		this.reasonLeave = reasonLeave;
	}

	public List<Holiday> getHolidayList() {
		return holidayList;
	}

	public void setHolidayList(List<Holiday> holidayList) {
		this.holidayList = holidayList;
	}

	public float getTotalHoliday() {
		return totalHoliday;
	}

	public void setTotalHoliday(float totalHoliday) {
		this.totalHoliday = totalHoliday;
	}

	public FeedbackBean getFeedbackBean() {
		return feedbackBean;
	}

	public void setFeedbackBean(FeedbackBean feedbackBean) {
		this.feedbackBean = feedbackBean;
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
