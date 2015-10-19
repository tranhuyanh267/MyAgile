package com.ant.myagile.managedbean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Feedback;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.FeedbackService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.TeamMemberService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("feedbackBean")
@Scope("session")
public class FeedbackBean {

	@Autowired
	private FeedbackService feedbackService;

	@Autowired
	private SprintService sprintService;

	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private TeamMemberService teamMemberService;
	
	@Autowired
	private TeamService teamService;

	@Autowired
	private Utils utils;

	private Feedback feedbackEdit;

	private String feedbackContent;
	private String selectedProjectId = "-1";

	private boolean isPO = false;

	private Project selectedProject;

	private List<Feedback> feedbackList;
	private List<Project> projectList;
	private Team currentTeam;

	private static final int MIN_FB_LENGTH = 20;
	private static final int MAX_FB_LENGTH = 2000;
	public static final String PRODUCT_OWNER = "PRODUCT_OWNER";

	/**
	 * Add new feedback
	 */
	@SuppressWarnings("static-access")
	public void addNewFeedback() {
		// Strip all html tags when user doesn't input anything.
		if ((Jsoup.parse(this.feedbackContent.replaceAll("&nbsp;", "")).text()).trim().equals("") || this.feedbackContent == null) {
			this.feedbackContent = "";
		}

		if (!StringUtils.isBlank(this.feedbackContent.trim()) && this.feedbackContent.trim().length() > FeedbackBean.MIN_FB_LENGTH
				&& this.feedbackContent.trim().length() <= FeedbackBean.MAX_FB_LENGTH) {
			Member loggedInMember = this.utils.getLoggedInMember();
			String sprintId = JSFUtils.getRequestParameter("sprintId");
			Feedback feedback = new Feedback();
			feedback.setCreatedDate(new Date());
			feedback.setFeedbackContent(this.feedbackContent.trim());
			feedback.setMember(loggedInMember);
			feedback.setSprint(this.sprintService.findSprintById(Long.valueOf(sprintId)));
			feedback.setProject(this.projectService.findProjectById(Long.valueOf(this.selectedProjectId)));

			boolean result = this.feedbackService.saveFeedback(feedback);

			if (result) {
				JSFUtils.addSuccessMessage(null, this.utils.getMessage("myagile.statistic.SubmitFeedbackSuccess", null));
				loadFeedbacksByProjectIdAndSprintId();
				this.feedbackContent = "";
			} else {
				JSFUtils.addErrorMessage(null, this.utils.getMessage("myagile.statistic.ErrorUnknown", null));
			}
		} else {
			Object[] params = { FeedbackBean.MIN_FB_LENGTH, FeedbackBean.MAX_FB_LENGTH };
			JSFUtils.addErrorMessage(null, this.utils.getMessage("myagile.statistic.ErrorMaxlenght", params));
		}

	}

	/**
	 * Load feedbacks by Project Id and Sprint Id
	 * 
	 * @return feedback list
	 */
	public List<Feedback> loadFeedbacksByProjectIdAndSprintId() {
		if (this.feedbackList == null) {
			this.feedbackList = new ArrayList<Feedback>();
		}
		if (!getSelectedProjectId().equals("-1")) {
			setFeedbackList(this.feedbackService.findFeedbacksBySprintIdAndProjectId(Long.valueOf(selectedProjectId),
					Long.valueOf((String) JSFUtils.getManagedBeanValue("statisticBean.selectedDropDownSprintId"))));
		}
		return this.feedbackList;
	}

	/**
	 * Load projects by Team Id and Member Id Also check if logged in member is
	 * PO or not.
	 * 
	 * @param teamId
	 * @return feedback list
	 */
	public List<Project> loadProjectByTeamIdAndMemberId(Long teamId) {
		if(teamId < 0){
			return this.projectList;
		}else{
			Member loggedInMember = this.utils.getLoggedInMember();
			currentTeam = teamService.findTeamByTeamId(teamId);
			this.projectList = this.projectService.filterTeamProjectsWithMemberId(teamId, loggedInMember.getMemberId());
			if (this.projectList.size() > 0) {
				if(selectedProjectId == "-1"){
					this.selectedProjectId = String.valueOf(this.projectList.get(0).getProjectId());
				}
				if ((this.projectList.get(0).getOwner().getMemberId().equals(loggedInMember.getMemberId()))||teamMemberService.findTeamMemberByMemberAndTeam(loggedInMember, currentTeam).getPosition().equals(PRODUCT_OWNER)) {
					this.isPO = true;
				}
			} else {
				this.selectedProjectId = "-1";
			}
			return this.projectList;
		}	
	}

	/**
	 * Check if logged in member is the owner of feedback
	 * 
	 * @param feedback
	 * @return boolean
	 */
	public boolean checkFeedbackOwner(Feedback feedback) {
		if (feedback.getMember().getMemberId().equals(this.utils.getLoggedInMember().getMemberId())) {
			return true;
		}
		return false;

	}

	/**
	 * Handle event when project dropdown list is changed.
	 */
	public void handleProjectChange() {
		setSelectedProject(this.projectService.findProjectById(Long.valueOf(this.selectedProjectId)));
		if (this.selectedProject.getOwner().getMemberId().equals(this.utils.getLoggedInMember().getMemberId())||teamMemberService.findTeamMemberByMemberAndTeam(utils.getLoggedInMember(),currentTeam).getPosition().equals(PRODUCT_OWNER)) {
			this.isPO = true;
		}
	}

	/**
	 * Handle event when feedback edit button is clicked.
	 */
	public void handleFeedbackEdit(Feedback feedback) {
		if (feedback == null) {
			setFeedbackEdit(null);
		} else {
			setFeedbackEdit(feedback);
		}

	}

	public void deleteFeedback(Feedback feedback) {
		this.feedbackService.deleteFeedback(feedback);
	}

	@SuppressWarnings("static-access")
	public void editFeedback(Feedback feedback) {
		// Strip all html tags when user doesn't input anything.
		if ((Jsoup.parse(this.feedbackEdit.getFeedbackContent().replaceAll("&nbsp;", "")).text()).trim().equals("") || this.feedbackEdit.getFeedbackContent() == null) {
			this.feedbackEdit.setFeedbackContent("");
		}

		if (!StringUtils.isBlank(this.feedbackEdit.getFeedbackContent().trim()) && this.feedbackEdit.getFeedbackContent().trim().length() > FeedbackBean.MIN_FB_LENGTH
				&& this.feedbackEdit.getFeedbackContent().trim().length() <= FeedbackBean.MAX_FB_LENGTH) {

			feedback.setFeedbackContent(this.feedbackEdit.getFeedbackContent().trim());
			boolean result = this.feedbackService.updateFeedback(feedback);

			if (result) {
				this.feedbackEdit = null;
			} else {
				JSFUtils.addErrorMessage(null, this.utils.getMessage("myagile.statistic.ErrorUnknown", null));
				this.feedbackEdit = null;
			}
		} else {
			Object[] params = { FeedbackBean.MIN_FB_LENGTH, FeedbackBean.MAX_FB_LENGTH };
			JSFUtils.addErrorMessage(null, this.utils.getMessage("myagile.statistic.ErrorMaxlenght", params));
		}
	}

	public void reset() {
		setFeedbackEdit(null);
		this.isPO = false;
	}

	public String getFeedbackContent() {
		return this.feedbackContent;
	}

	public void setFeedbackContent(String feedbackContent) {
		this.feedbackContent = feedbackContent;
	}

	public List<Feedback> getFeedbackList() {
		loadFeedbacksByProjectIdAndSprintId();
		return this.feedbackList;
	}

	public void setFeedbackList(List<Feedback> feedbackList) {
		this.feedbackList = feedbackList;
	}

	public String getSelectedProjectId() {
		return this.selectedProjectId;
	}

	public void setSelectedProjectId(String selectedProjectId) {
		this.selectedProjectId = selectedProjectId;
	}

	public Project getSelectedProject() {
		return this.selectedProject;
	}

	public void setSelectedProject(Project selectedProject) {
		this.selectedProject = selectedProject;
	}

	public boolean isPO() {
		return this.isPO;
	}

	public void setPO(boolean isPO) {
		this.isPO = isPO;
	}

	public Feedback getFeedbackEdit() {
		return this.feedbackEdit;
	}

	public void setFeedbackEdit(Feedback feedbackEdit) {
		this.feedbackEdit = feedbackEdit;
	}

	public List<Project> getProjectList() {
		return this.projectList;
	}

	public void setProjectList(List<Project> projectList) {
		this.projectList = projectList;
	}

	public Team getCurrentTeam() {
		return currentTeam;
	}

	public void setCurrentTeam(Team currentTeam) {
		this.currentTeam = currentTeam;
	}
	
	
}
