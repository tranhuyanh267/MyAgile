package com.ant.myagile.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.HistoryDao;
import com.ant.myagile.dao.UserStoryDao;
import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.History;
import com.ant.myagile.model.HistoryFieldChange;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.model.TeamProject;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.utils.Utils;

@Service
@Transactional
public class HistoryServiceImpl implements HistoryService {

	@Autowired
	private HistoryDao historyDao;

	@Autowired
	private StatusService statusService;

	@Autowired
	UserStoryDao userStoryDao;

	@Autowired
	private PointRemainService pointRemainService;

	@Autowired
	private Utils utils;

	Date current;

	private Logger logger = Logger.getLogger(HistoryServiceImpl.class);

	@Override
	public boolean save(History history) {
		logger.error("QQQQQQQQQ save history: containerId= " + history.getContainerId().toString());
		logger.error("QQQQQQQQQ save history: CreatedOn= " + history.getCreatedOn().toString());
		return this.historyDao.save(history);
	}

	@Override
	public boolean update(History history) {
		return this.historyDao.update(history);
	}

	@Override
	public boolean delete(History history) {
		return this.historyDao.delete(history);
	}

	@Override
	public List<History> findHistoryByContainer(String containerType, long containerId) {
		return this.historyDao.findHistoryByContainer(containerType, containerId);
	}

	@Override
	public List<History> findHistoryByContainerWithNumberRow(String containerType, long containerId, int numRow) {
		return this.historyDao.findHistoryByContainerWithNumberRow(containerType, containerId, numRow);
	}

	@SuppressWarnings("static-access")
	public int saveHistoryForIssue(Issue oldIssue, Issue newIssue) {
		History historyUpdate = new History(oldIssue.getIssueId(), History.ISSUE_HISTORY, History.UPDATE_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
		ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
		if (!oldIssue.getSubject().equals(newIssue.getSubject())) {
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Subject", oldIssue.getSubject(), newIssue.getSubject());
			historyFieldChanges.add(updatedField);
		}
		if (oldIssue.getStatus() != null && newIssue.getStatus() != null && !oldIssue.getStatus().getStatusId().equals(newIssue.getStatus().getStatusId())) {
			newIssue.setStatus(this.statusService.findStatusById(newIssue.getStatus().getStatusId()));
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Status", oldIssue.getStatus().getName(), newIssue.getStatus().getName());
			historyFieldChanges.add(updatedField);
		}
		String oldAssigned;
		String newAssigned;
		if (oldIssue.getAssigned() == null) {
			oldAssigned = "None";
		} else {
			oldAssigned = utils.checkName(oldIssue.getAssigned());
		}
		if (newIssue.getAssigned() == null) {
			newAssigned = "None";
		} else {
			newAssigned = utils.checkName(newIssue.getAssigned());
		}
		if (!oldAssigned.equals(newAssigned)) {
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Assigned to", oldAssigned, newAssigned);
			historyFieldChanges.add(updatedField);
		}
		if (!oldIssue.getEstimate().equals(newIssue.getEstimate())) {
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Estime point", this.pointRemainService.getPointEstimateByFormat(oldIssue), this.pointRemainService.getPointEstimateByFormat(newIssue));
			historyFieldChanges.add(updatedField);
		}
		if (!oldIssue.getRemain().equals(newIssue.getRemain())) {
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Remain point", this.pointRemainService.getPointRemainByFormat(oldIssue), this.pointRemainService.getPointRemainByFormat(newIssue));
			historyFieldChanges.add(updatedField);
		}
		if (!oldIssue.getType().equals(newIssue.getType())) {
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Type", oldIssue.getType(), newIssue.getType());
			historyFieldChanges.add(updatedField);
		}
		if (!oldIssue.getPriority().equals(newIssue.getPriority())) {
			String oldPriority, newPriority;
			if (oldIssue.getPriority().equals("")) {
				oldPriority = "None";
			} else {
				oldPriority = oldIssue.getPriority();
			}
			if (newIssue.getPriority().equals("")) {
				newPriority = "None";
			} else {
				newPriority = newIssue.getPriority();
			}
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Priority", oldPriority, newPriority);
			historyFieldChanges.add(updatedField);
		}
		String oldParent, newParent;
		if (oldIssue.getParent() == null) {
			oldParent = "None";
		} else {
			oldParent = oldIssue.getParent().getSubject();
		}
		if (newIssue.getParent() == null) {
			newParent = "None";
		} else {
			newParent = newIssue.getParent().getSubject();
		}
		if (!oldParent.equals(newParent)) {
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "User Story", oldParent, newParent);
			historyFieldChanges.add(updatedField);
		}
		if (!oldIssue.getDescription().equals(newIssue.getDescription())) {
			String oldDescriptionValue = "";
			String newDescriptionValue = "";
			if ((Jsoup.parse(oldIssue.getDescription().replaceAll("&nbsp;", "")).text()).trim().equals("") || oldIssue.getDescription() == null) {
				oldDescriptionValue = "None";
			} else {
				oldDescriptionValue = oldIssue.getDescription().trim();
			}
			if ((Jsoup.parse(newIssue.getDescription().replaceAll("&nbsp;", "")).text()).trim().equals("") || newIssue.getDescription() == null) {
				newDescriptionValue = "None";
			} else {
				newDescriptionValue = newIssue.getDescription().trim();
			}
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Description", oldDescriptionValue, newDescriptionValue);
			historyFieldChanges.add(updatedField);
		}
		if ((oldIssue.getNote()!=null && newIssue.getNote() != null) && (!oldIssue.getNote().equals(newIssue.getNote()))) {
			String oldNoteValue = "";
			String newNoteValue = "";
			if ((Jsoup.parse(oldIssue.getNote().replaceAll("&nbsp;", "")).text()).trim().equals("") || oldIssue.getNote() == null) {
				oldNoteValue = "None";
			} else {
				oldNoteValue = oldIssue.getNote().trim();
			}
			if ((Jsoup.parse(newIssue.getNote().replaceAll("&nbsp;", "")).text()).trim().equals("") || newIssue.getNote() == null) {
				newNoteValue = "None";
			} else {
				newNoteValue = newIssue.getNote().trim();
			}
			HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Note", oldNoteValue, newNoteValue);
			historyFieldChanges.add(updatedField);
		}
		historyUpdate.setHistoryFieldChanges(historyFieldChanges);
		if (historyUpdate.getHistoryFieldChanges().size() > 0) {
			this.historyDao.save(historyUpdate);
		}
		return historyUpdate.getHistoryFieldChanges().size();
	}

	@Override
	public boolean saveHistoryForNewIssue(Issue issue) {
		History historyNew = new History(issue.getIssueId(), History.ISSUE_HISTORY, History.CREATE_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
		return this.historyDao.save(historyNew);
	}

	@Override
	public int saveHistoryForNewProject(Project project) {
		try {
			History historyNew = new History(project.getProjectId(), History.PROJECT_HISTORY, History.CREATE_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
			ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
			HistoryFieldChange updatedField = new HistoryFieldChange(historyNew, "Project", project.getProjectId().toString(), project.getProjectName());
			historyFieldChanges.add(updatedField);
			historyNew.setHistoryFieldChanges(historyFieldChanges);
			if (historyNew.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyNew);
			}
			return historyNew.getHistoryFieldChanges().size();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public int saveHistoryForUpdateProject(Project oldProject, Project newProject) {
		String oldValue = "";
		String newValue = "";
		try {
			History historyUpdate = new History(oldProject.getProjectId(), History.PROJECT_HISTORY, History.UPDATE_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
			HistoryFieldChange updatedField = new HistoryFieldChange();
			// Add to history before save into database
			ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
			if (!oldProject.getProjectName().equals(newProject.getProjectName())) {
				updatedField = new HistoryFieldChange(historyUpdate, "Project Name", oldProject.getProjectName(), newProject.getProjectName());
				historyFieldChanges.add(updatedField);
			}
			if (oldProject.getDescription().trim().equals("") || oldProject.getDescription() == null) {
				oldValue = "None";
			} else {
				oldValue = oldProject.getDescription().trim();
			}
			if (newProject.getDescription().trim().equals("") || newProject.getDescription() == null) {
				newValue = "None";
			} else {
				newValue = newProject.getDescription().trim();
			}
			if (!oldValue.equals(newValue)) {
				updatedField = new HistoryFieldChange(historyUpdate, "Description", oldValue, newValue);
				historyFieldChanges.add(updatedField);
			}

			if (oldProject.getIsPublic() != newProject.getIsPublic()) {
				updatedField = new HistoryFieldChange(historyUpdate, "Publicity", this.convertPublicity(oldProject.getIsPublic()), this.convertPublicity(newProject.getIsPublic()));
				historyFieldChanges.add(updatedField);
			}
			if (oldProject.getImagePath() == null || oldProject.getImagePath().trim().equals("")) {
				oldValue = "None";
			} else {
				oldValue = oldProject.getImagePath();
			}
			if (newProject.getImagePath() == null || newProject.getImagePath().trim().equals("")) {
				newValue = "None";
			} else {
				newValue = newProject.getImagePath();
			}
			if (!oldValue.equals(newValue)) {
				updatedField = new HistoryFieldChange(historyUpdate, "Logo", oldValue, newValue);
				historyFieldChanges.add(updatedField);
			}
			historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			if (historyUpdate.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyUpdate);
			}
			return historyUpdate.getHistoryFieldChanges().size();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public int saveHistoryForTeamProject(TeamProject teamProject, String type, String editPage) {
		if (editPage.equals("Project")) {
			History historyUpdate = new History(teamProject.getProject().getProjectId(), History.PROJECT_HISTORY, "", this.getCurrent(), this.utils.getLoggedInMember());
			ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
			// check team had asign/unAsign to project
			HistoryFieldChange updatedField = new HistoryFieldChange();
			if (type.equals("remove")) {
				updatedField = new HistoryFieldChange(historyUpdate, "Team", null, teamProject.getTeam().getTeamName());
				historyUpdate.setActionType(History.REMOVE_ACTION);
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			}
			if (type.equals("reassign")) {
				updatedField = new HistoryFieldChange(historyUpdate, "Team", null, teamProject.getTeam().getTeamName());
				historyUpdate.setActionType(History.REASSIGN_ACTION);
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			}
			if (type.equals("assign")) {
				updatedField = new HistoryFieldChange(historyUpdate, "Team", null, teamProject.getTeam().getTeamName());
				historyUpdate.setActionType(History.ASSIGN_ACTION);
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			}
			if (historyUpdate.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyUpdate);
			}
			return historyUpdate.getHistoryFieldChanges().size();
		}
		if (editPage.equals("Team")) {
			History historyUpdate = new History(teamProject.getTeam().getTeamId(), History.TEAM_HISTORY, "", this.getCurrent(), this.utils.getLoggedInMember());
			ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
			// check team had asign/unAsign to project
			HistoryFieldChange updatedField = new HistoryFieldChange();
			if (type.equals("remove")) {
				updatedField = new HistoryFieldChange(historyUpdate, "Project", null, teamProject.getProject().getProjectName());

				historyUpdate.setActionType(History.REMOVE_ACTION);
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			}
			if (type.equals("reassign")) {
				updatedField = new HistoryFieldChange(historyUpdate, "Project", null, teamProject.getProject().getProjectName());
				historyUpdate.setActionType(History.REASSIGN_ACTION);
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			}
			if (type.equals("assign")) {
				updatedField = new HistoryFieldChange(historyUpdate, "Project", null, teamProject.getProject().getProjectName());
				historyUpdate.setActionType(History.ASSIGN_ACTION);
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			}
			if (historyUpdate.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyUpdate);
			}
			return historyUpdate.getHistoryFieldChanges().size();
		}
		return 0;
	}

	@Override
	public int saveHistoryForNewTeam(Team team) {
		try {
			History historyNew = new History(team.getTeamId(), History.TEAM_HISTORY, History.CREATE_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
			ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
			HistoryFieldChange updatedField = new HistoryFieldChange(historyNew, "Team", team.getTeamId().toString(), team.getTeamName());
			historyFieldChanges.add(updatedField);
			historyNew.setHistoryFieldChanges(historyFieldChanges);
			if (historyNew.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyNew);
			}
			return historyNew.getHistoryFieldChanges().size();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public int saveHistoryForUpdateTeam(Team oldTeam, Team newTeam) {
		String oldValue = "";
		String newValue = "";
		try {
			History historyUpdate = new History(oldTeam.getTeamId(), History.TEAM_HISTORY, History.UPDATE_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
			HistoryFieldChange updatedField = new HistoryFieldChange();
			// Add to history before save into database
			ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
			if (!oldTeam.getTeamName().equals(newTeam.getTeamName())) {
				updatedField = new HistoryFieldChange(historyUpdate, "Team Name", oldTeam.getTeamName(), newTeam.getTeamName());
				historyFieldChanges.add(updatedField);
			}
			if (!oldTeam.getMailGroup().equals(newTeam.getMailGroup())) {
				updatedField = new HistoryFieldChange(historyUpdate, "Group Mail", oldTeam.getMailGroup(), newTeam.getMailGroup());
				historyFieldChanges.add(updatedField);
			}
			if (oldTeam.getDescription().trim().equals("") || oldTeam.getDescription() == null) {
				oldValue = "None";
			} else {
				oldValue = oldTeam.getDescription().trim();
			}
			if (newTeam.getDescription().trim().equals("") || newTeam.getDescription() == null) {
				newValue = "None";
			} else {
				newValue = newTeam.getDescription().trim();
			}
			if (!oldValue.equals(newValue)) {
				updatedField = new HistoryFieldChange(historyUpdate, "Description", oldValue, newValue);
				historyFieldChanges.add(updatedField);
			}

			if (oldTeam.getLogo() == null || oldTeam.getLogo().trim().equals("")) {
				oldValue = "None";
			} else {
				oldValue = oldTeam.getLogo();
			}
			if (newTeam.getLogo() == null || oldTeam.getLogo().trim().equals("")) {
				newValue = "None";
			} else {
				newValue = newTeam.getLogo();
			}
			if (!oldValue.equals(newValue)) {
				updatedField = new HistoryFieldChange(historyUpdate, "Logo", oldValue, newValue);
				historyFieldChanges.add(updatedField);
			}
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			String oldDate = formatter.format(oldTeam.getEstablishedDate()).trim();
			String newDate = formatter.format(newTeam.getEstablishedDate()).trim();
			if (!oldDate.equals(newDate)) {
				updatedField = new HistoryFieldChange(historyUpdate, "Established Date", oldDate, newDate);
				historyFieldChanges.add(updatedField);
			}
			historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			if (historyUpdate.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyUpdate);
			}
			return historyUpdate.getHistoryFieldChanges().size();
		} catch (Exception e) {
			return 0;
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public int saveHistoryForTeamMember(TeamMember teamMember, String actionType) {
		ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
		try {
			if (actionType.equals("Add")) {
				History historyNew = new History(teamMember.getTeam().getTeamId(), History.TEAM_HISTORY, History.ADD_MEMBER_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
				HistoryFieldChange updatedField = new HistoryFieldChange(historyNew, "Member", teamMember.getMember().getMemberId().toString(), teamMember.getMember().getUsername());
				historyFieldChanges.add(updatedField);
				historyNew.setHistoryFieldChanges(historyFieldChanges);
				if (historyNew.getHistoryFieldChanges().size() > 0) {
					this.historyDao.save(historyNew);
				}
				return historyNew.getHistoryFieldChanges().size();
			}
			if (actionType.equals("Remove")) {
				String fullName = "";
				History historyNew = new History(teamMember.getTeam().getTeamId(), History.TEAM_HISTORY, History.REMOVE_MEMBER_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
				fullName = utils.checkName(teamMember.getMember());
				HistoryFieldChange updatedField = new HistoryFieldChange(historyNew, "Member", teamMember.getMember().getMemberId().toString(), fullName);
				historyFieldChanges.add(updatedField);
				historyNew.setHistoryFieldChanges(historyFieldChanges);
				if (historyNew.getHistoryFieldChanges().size() > 0) {
					this.historyDao.save(historyNew);
				}
				return historyNew.getHistoryFieldChanges().size();
			}
		} catch (Exception e) {
			return 0;
		}
		return 0;
	}

	@Override
	public int saveHistoryForChangeRole(TeamMember oldTeamMember, String newRole) {
		try {
			History historyUpdate = new History(oldTeamMember.getTeam().getTeamId(), History.TEAM_HISTORY, History.ROLE_MEMBER_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
			HistoryFieldChange updatedField = new HistoryFieldChange();
			ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
			if (!oldTeamMember.getPosition().equals(newRole)) {
				updatedField = new HistoryFieldChange(historyUpdate, oldTeamMember.getMember().getLastName() + " " + oldTeamMember.getMember().getFirstName(), oldTeamMember.getPosition(), newRole);
				historyFieldChanges.add(updatedField);
			}
			historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			if (historyUpdate.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyUpdate);
			}
			return historyUpdate.getHistoryFieldChanges().size();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public int saveHistoryForProductBL(UserStory userStory, String actionType, Member author) {
		History historyUpdate = new History(userStory.getProject().getProjectId(), History.PROJECT_HISTORY, History.UPDATE_ACTION, this.getCurrent(), author);
		ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
		HistoryFieldChange updatedField = new HistoryFieldChange();
		if (actionType.equals("Create")) {
			updatedField = new HistoryFieldChange(historyUpdate, "UserStory", userStory.getUserStoryId().toString(), userStory.getName());
			historyUpdate.setActionType(History.ADD_US_ACTION);
			historyFieldChanges.add(updatedField);
			historyUpdate.setHistoryFieldChanges(historyFieldChanges);
		}
		if (actionType.equals("Delete")) {
			updatedField = new HistoryFieldChange(historyUpdate, "UserStory", userStory.getUserStoryId().toString(), userStory.getName());
			historyUpdate.setActionType(History.DELETE_US_ACTION);
			historyFieldChanges.add(updatedField);
			historyUpdate.setHistoryFieldChanges(historyFieldChanges);
		}
		if (historyUpdate.getHistoryFieldChanges().size() > 0) {
			this.historyDao.save(historyUpdate);
		}
		return historyUpdate.getHistoryFieldChanges().size();
	}

	@Override
	public int saveHistoryForUpdateProductBL(UserStory oldUserStory, UserStory newUserStory) {
		String userStoryIdTemp = "userstory - '#" + oldUserStory.getUserStoryId() + "'";
		String oldValue;
		String newValue;
		try {
			History historyUpdate = new History(newUserStory.getProject().getProjectId(), History.PROJECT_HISTORY, History.UPDATE_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
			HistoryFieldChange updatedField = new HistoryFieldChange();
			// Add to history before save into database
			ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
			if (!oldUserStory.getName().trim().equals(newUserStory.getName().trim())) {
				updatedField = new HistoryFieldChange(historyUpdate, "Name of " + userStoryIdTemp, oldUserStory.getName(), newUserStory.getName());
				historyFieldChanges.add(updatedField);
			}
			if ((Jsoup.parse(oldUserStory.getDescription().replaceAll("&nbsp;", "")).text()).trim().equals("") || oldUserStory.getDescription() == null) {
				oldValue = "None";
			} else {
				oldValue = oldUserStory.getDescription().trim();
			}

			if ((Jsoup.parse(newUserStory.getDescription().replaceAll("&nbsp;", "")).text()).trim().equals("") || newUserStory.getDescription() == null) {
				newValue = "None";
			} else {
				newValue = newUserStory.getDescription().trim();
			}
			if (!oldValue.equals(newValue)) {
				updatedField = new HistoryFieldChange(historyUpdate, "Description of " + userStoryIdTemp, "Description", newValue);
				historyFieldChanges.add(updatedField);
			}
			if (oldUserStory.getValue() != newUserStory.getValue()) {
				updatedField = new HistoryFieldChange(historyUpdate, "Value of " + userStoryIdTemp, oldUserStory.getValue() + "", newUserStory.getValue() + "");
				historyFieldChanges.add(updatedField);
			}
			if (oldUserStory.getRisk() != newUserStory.getRisk()) {
				updatedField = new HistoryFieldChange(historyUpdate, "Risk of " + userStoryIdTemp, oldUserStory.getRisk() + "", newUserStory.getRisk() + "");
				historyFieldChanges.add(updatedField);
			}
			if (!oldUserStory.getStatus().equals(newUserStory.getStatus())) {
				updatedField = new HistoryFieldChange(historyUpdate, "Status of " + userStoryIdTemp, oldUserStory.getStatus() + "", newUserStory.getStatus() + "");
				historyFieldChanges.add(updatedField);
			}

			if (!oldUserStory.getPriority().equals(newUserStory.getPriority())) {
				updatedField = new HistoryFieldChange(historyUpdate, "Priority", oldUserStory.getPriority().toString(), newUserStory.getPriority().toString());
				historyFieldChanges.add(updatedField);
			}

			if (!oldUserStory.getNote().equals(newUserStory.getNote())) {
				String oldNoteValue = "", newNoteValue = "";
				if ((Jsoup.parse(oldUserStory.getNote().replaceAll("&nbsp;", "")).text()).trim().equals("") || oldUserStory.getNote() == null) {
					oldNoteValue = "None";
				} else {
					oldNoteValue = oldUserStory.getNote().trim();
				}
				if ((Jsoup.parse(newUserStory.getNote().replaceAll("&nbsp;", "")).text()).trim().equals("") || newUserStory.getNote() == null) {
					newNoteValue = "None";
				} else {
					newNoteValue = newUserStory.getNote().trim();
				}
				updatedField = new HistoryFieldChange(historyUpdate, "Note", oldNoteValue, newNoteValue);
				historyFieldChanges.add(updatedField);
			}
			historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			if (historyUpdate.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyUpdate);
			}
			return historyUpdate.getHistoryFieldChanges().size();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public int saveHistoryForSprint(Sprint sprint) {
		History historyUpdate = new History(sprint.getTeam().getTeamId(), History.TEAM_HISTORY, History.CREATE_SPRINT, this.getCurrent(), this.utils.getLoggedInMember());
		ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
		HistoryFieldChange updatedField = new HistoryFieldChange(historyUpdate, "Sprint", sprint.getSprintId().toString(), sprint.getSprintName());
		historyFieldChanges.add(updatedField);
		historyUpdate.setHistoryFieldChanges(historyFieldChanges);
		if (historyUpdate.getHistoryFieldChanges().size() > 0) {
			this.historyDao.save(historyUpdate);
		}
		return historyUpdate.getHistoryFieldChanges().size();

	}

	@Override
	public int saveHistoryForUpdateSprint(Sprint oldSprint, Sprint newSprint) {
		String sprintName = "sprint - '#" + oldSprint.getSprintId() + "'";
		try {
			History historyUpdate = new History(newSprint.getTeam().getTeamId(), History.TEAM_HISTORY, History.UPDATE_ACTION, this.getCurrent(), this.utils.getLoggedInMember());
			HistoryFieldChange updatedField = new HistoryFieldChange();
			// Add to history before save into database
			ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
			if (!oldSprint.getSprintName().trim().equals(newSprint.getSprintName().trim())) {
				updatedField = new HistoryFieldChange(historyUpdate, "Name of " + sprintName, oldSprint.getSprintName(), newSprint.getSprintName());
				historyFieldChanges.add(updatedField);
			}
			if (!oldSprint.getStatus().trim().equals(newSprint.getStatus().trim())) {
				updatedField = new HistoryFieldChange(historyUpdate, "Status of " + sprintName, oldSprint.getStatus(), newSprint.getStatus());
				historyFieldChanges.add(updatedField);
			}
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			String oldDate = formatter.format(oldSprint.getDateStart()).trim();
			String newDate = formatter.format(newSprint.getDateStart()).trim();
			if (!oldDate.equals(newDate)) {
				updatedField = new HistoryFieldChange(historyUpdate, "Date Start of " + sprintName, oldDate, newDate);
				historyFieldChanges.add(updatedField);
			}
			oldDate = formatter.format(oldSprint.getDateEnd()).trim();
			newDate = formatter.format(newSprint.getDateEnd()).trim();
			if (!oldDate.equals(newDate)) {
				updatedField = new HistoryFieldChange(historyUpdate, "Date End of " + sprintName, oldDate, newDate);
				historyFieldChanges.add(updatedField);
			}
			historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			if (historyUpdate.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyUpdate);
			}
			return historyUpdate.getHistoryFieldChanges().size();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public int saveHistoryForAttachment(Attachment attachment, String type) {
		History historyUpdate = new History();
		HistoryFieldChange updatedField = new HistoryFieldChange();
		try {
			if (type.equals("ISSUE")) {
				historyUpdate = new History(attachment.getContainerId(), Attachment.ISSUE_ATTACHMENT, History.ADD_ACTION, this.getCurrent(), attachment.getAuthor());
				ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
				updatedField = new HistoryFieldChange(historyUpdate, "Attachment", "", attachment.getFilename());
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			} else {
				UserStory storyTemp = userStoryDao.findUserStoryById(attachment.getContainerId());
				String usId = "userstory - '#" + storyTemp.getUserStoryId() + "'";
				historyUpdate = new History(storyTemp.getProject().getProjectId(), History.PROJECT_HISTORY, History.ADD_FILE_US_ACTION, this.getCurrent(), attachment.getAuthor());
				ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
				updatedField = new HistoryFieldChange(historyUpdate, "Attachment of " + usId, "", attachment.getFilename());
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			}
			if (historyUpdate.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyUpdate);
			}
			return historyUpdate.getHistoryFieldChanges().size();
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public int saveHistoryForDeleteAttachment(Attachment attachment, String type) {
		String userStoryIdTemp = "userstory - '#" + attachment.getContainerId() + "'";
		History historyUpdate = new History();
		HistoryFieldChange updatedField = new HistoryFieldChange();
		try {
			if (type.equals("ISSUE")) {
				historyUpdate = new History(attachment.getContainerId(), Attachment.ISSUE_ATTACHMENT, History.REMOVE_ACTION, this.getCurrent(), attachment.getAuthor());
				ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
				updatedField = new HistoryFieldChange(historyUpdate, "Attachment", attachment.getFilename(), "");
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			} else {
				UserStory storyTemp = userStoryDao.findUserStoryById(attachment.getContainerId());
				historyUpdate = new History(storyTemp.getProject().getProjectId(), History.PROJECT_HISTORY, History.DELETE_FILE_US_ACTION, this.getCurrent(), attachment.getAuthor());
				ArrayList<HistoryFieldChange> historyFieldChanges = new ArrayList<HistoryFieldChange>();
				updatedField = new HistoryFieldChange(historyUpdate, "Attachment of " + userStoryIdTemp, "", attachment.getFilename());
				historyFieldChanges.add(updatedField);
				historyUpdate.setHistoryFieldChanges(historyFieldChanges);
			}
			if (historyUpdate.getHistoryFieldChanges().size() > 0) {
				this.historyDao.save(historyUpdate);
			}
			return historyUpdate.getHistoryFieldChanges().size();
		} catch (Exception e) {
			return 0;
		}
	}

	public String convertPublicity(boolean publicity) {
		if (publicity) {
			return "Public";
		} else {
			return "Private";
		}
	}

	public HistoryDao getHistoryDao() {
		return this.historyDao;
	}

	public void setHistoryDao(HistoryDao historyDao) {
		this.historyDao = historyDao;
	}

	public PointRemainService getPointRemainService() {
		return this.pointRemainService;
	}

	public void setPointRemainService(PointRemainService pointRemainService) {
		this.pointRemainService = pointRemainService;
	}

	@Override
	public List<Object> findIssueHistoryBySprintWithStartAndEndRow(Long sprintId, int startRow, int endRow, Date dateStart, Date dateEnd) {
		return historyDao.findIssueHistoryBySprintWithStartAndEndRow(sprintId, startRow, endRow, dateStart, dateEnd);
	}

	@Override
	public List<Date> findDateRangeOfIssueHistoryBySprintId(Long sprintId) {
		return historyDao.findDateRangeOfIssueHistoryBySprintId(sprintId);
	}

	public Date getCurrent() {
		Calendar calendar = Calendar.getInstance();
//          calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
//          calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
//          calendar.set(Calendar.DATE, calendar.get(Calendar.DATE));
//          calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
//          calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
//          calendar.setTimeZone(TimeZone.getTimeZone("Asia/Saigon"));
		return calendar.getTime();
	}

	public void setCurrent(Date current) {
		this.current = current;
	}
}