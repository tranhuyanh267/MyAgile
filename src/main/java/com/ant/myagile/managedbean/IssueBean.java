package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import jxl.write.WriteException;
import net.sf.jasperreports.engine.JRException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.History;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Status;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.ExportImportFileService;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;
import com.itextpdf.text.DocumentException;

@Component("issueBean")
@Scope("session")
public class IssueBean implements Serializable {
	/**
     * 
     */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(IssueBean.class);
	private static final String DONE_COLUMN_STATUS = "Done";

	private Issue issue;
	private Issue viewIssue;
	private Project viewProject;
	private Sprint sprint;
	private Attachment attachment;
	private Attachment selectedAttachment;
	private Attachment attachmentNotAdd;
	private PointRemain pointRemain;

	private List<Project> projectList;
	private List<Team> teamList;
	private List<Sprint> sprintList;
	private List<Status> statusList;
	private List<Member> memberList;
	private List<Issue> issueList;
	private List<Issue> issueParentList;
	private List<Issue> selectedIssues;
	private List<Issue> filteredIssues;
	private List<Attachment> attachmentNotAddList;
	private List<Attachment> attachmentListByIssue;

	private Long projectId;
	private Long teamId;
	private String filterIssueType = "Any";
	private String filterIssueStatus = "Any";
	private String filterIssuePriority = "Any";
	private Integer chooseFormatExcel = 1;
	private String subjectIssueBeforeEdit = "";

	@Autowired
	private Utils utils;
	@Autowired
	private MemberService memberService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private IssueService issueService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private TeamService teamService;
	@Autowired
	AttachmentService attachmentService;
	@Autowired
	PointRemainService pointRemainService;
	@Autowired
	ExportImportFileService exportFileService;
	@Autowired
	private UserStoryService userStoryService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private KanbanIssueService kanbanIssueService;
	
	public void initPreview() {
		if (JSFUtils.isPostbackRequired()) {
			refreshDropDownList();
		}
	}

	/**
	 * Change value of <code>sprintList</code>, <code>sprintId</code>,
	 * <code>issueList</code> and <code>issue</code> when change team in team
	 * dropdown list
	 */
	public void handleTeamChange() {
		this.sprintList = null;
		this.sprint = null;
		getSprintList();
		getSprint();
		this.projectList = null;
		this.projectId = null;
		this.issueParentList = null;
		getProjectList();
		getProjectId();
		handleSprintChange();
	}

	/**
	 * Change value of <code>issueList</code> and <code>issue</code> when change
	 * sprint in sprint dropdown list
	 */
	public void handleSprintChange() {
		refreshIssueListTable();
		resetIssueForm();
		resetIssueValue();
		handleProjectChange();
		this.statusList = null;
	}

	/**
	 * Change value of <code>teamList</code>, <code>teamId</code>,
	 * <code>sprintList</code>, <code>sprintId</code>, <code>issueList</code>
	 * and <code>issue</code> when change project in project dropdown list
	 */
	public void handleProjectChange() {
		this.issueParentList = null;
		getIssueParentList();
	}

	public void refreshDropDownList() {
		this.teamList = null;
		getTeamList();
		if ((this.teamId == null) || (this.teamId == 0)) {
			this.teamId = !this.teamList.isEmpty() ? this.teamList.get(0).getTeamId() : 0;
		}
		if ((this.teamId != null) && (this.teamId != 0)) {
			Team teamTemp = this.teamService.findTeamByTeamId(this.teamId);
			this.projectList = this.projectService.findAllProjectsByTeam(teamTemp);
			this.sprintList = this.sprintService.findSprintsByTeamId(this.teamId);
			this.memberList = this.memberService.findMembersByTeam(teamTemp);
			getProjectId();
			if ((this.sprint == null) || (this.sprint.getSprintId() == null)) {
				this.sprint = !this.sprintList.isEmpty() ? this.sprintList.get(0) : new Sprint();
			}
		}
		if ((this.sprint != null) && (this.sprint.getSprintId() != null)) {
			this.statusList = this.statusService.findStatusBySprintId(this.sprint.getSprintId());
			if ((this.projectId != null) && (this.projectId != 0)) {
				this.issueParentList = this.issueService.findIssuesParent(this.projectId, this.sprint.getSprintId());
			}
		}
		refreshIssueListTable();
	}

	public void refreshIssueListTable() {
		this.issueList = new ArrayList<Issue>();
		if ((this.sprint != null) && (this.sprint.getSprintId() != null)) {
			this.issueList = this.issueService.findIssuesIsTaskBySprintId(this.sprint.getSprintId());
		}
		// Display list of issues based on filter options
		this.filteredIssues = this.issueService.filterIssues(this.issueList, this.filterIssueType, this.filterIssueStatus, this.filterIssuePriority);
		this.selectedIssues = new ArrayList<Issue>();
	}

	public void resetIssueValue() {
		this.issue = new Issue();
		if ((this.sprint != null) && (this.sprint.getSprintId() != null)) {
			this.issue.setSprint(this.sprintService.findSprintById(this.sprint.getSprintId()));
		}
	}

	public void resetAttachmentList() {
		deleteAttachmentNotAdded();
		this.attachmentListByIssue = new ArrayList<Attachment>();
		this.attachmentNotAddList = new ArrayList<Attachment>();
	}

	public void resetProperties() {
		this.filterIssuePriority = "Any";
		this.filterIssueStatus = "Any";
		this.filterIssueType = "Any";
		this.filteredIssues = null;
		this.selectedIssues = null;
	}

	public void resetIssueForm() {
		JSFUtils.resetForm("issueForm");
	}

	/**
	 * Redirect to view issue detail page
	 */
	public void toViewIssuePage() {
		this.viewProject = this.projectService.findProjectOfIssue(this.viewIssue);
		this.attachmentListByIssue = this.attachmentService.findAttachmentByIssue(getViewIssue());
		JSFUtils.setManagedBeanValue("historyBean.issueId", this.viewIssue.getIssueId());
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/issue/view";
		JSFUtils.redirect(contextPath);
	}

	public void backToAddIssuePage() {
		this.issue = this.viewIssue;
		List<Issue> issues = issueService.findIssueByParent(issue);
		if(!issues.isEmpty()) {
			this.issue.setIsParent(true);
		}
		this.viewProject = this.projectService.findProjectOfIssue(this.viewIssue);
		this.projectId = this.viewProject.getProjectId();
		this.teamId = this.issue.getSprint().getTeam().getTeamId();
		this.sprint = this.issue.getSprint();
		refreshDropDownList();
		this.attachmentListByIssue = this.attachmentService.findAttachmentByIssue(this.issue);
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/issue";
		JSFUtils.redirect(contextPath);
	}

	public void editIssue() {
		this.issue = this.issueService.findIssueById(this.issue.getIssueId());
		this.issue.setEstimate(this.issueService.getPointByFormat(this.issue.getEstimate(), this.issue.getPointFormat()));
		this.issue.setRemain(this.issueService.getPointByFormat(this.issue.getRemain(), this.issue.getPointFormat()));
		//save subject issue before save
		this.subjectIssueBeforeEdit = this.issue.getSubject();
		resetAttachmentList();
		this.attachmentListByIssue = this.attachmentService.findAttachmentByIssue(this.issue);
		resetIssueForm();
		if (this.issue.getParent() == null) {
			this.setProjectId(this.issue.getUserStory().getProject().getProjectId());
		} else {
			this.setProjectId(this.issue.getParent().getUserStory().getProject().getProjectId());
		}
		this.issueParentList = null;
		RequestContext context = org.primefaces.context.RequestContext.getCurrentInstance();
		context.scrollTo("newFrmBtn");
		context.addCallbackParam("edit", true);
	}

	@SuppressWarnings("static-access")
	public void saveIssue() {
		// update userstory
		try {
			String estimatePoint = this.issue.getEstimate().trim();
			this.issue.setPointFormat(this.issueService.checkingPointFormat(estimatePoint));
			estimatePoint = this.issueService.checkingEstimatePoint(estimatePoint);
			this.issue.setEstimate(estimatePoint);
			this.issue.setRemain(estimatePoint);

			// Remove Control Characters
			this.issue.setSubject(this.issue.getSubject().replaceAll("\\p{Cntrl}", ""));
			this.issue.setNote(this.issue.getNote().replaceAll("\\p{Cntrl}", ""));
			this.issue.setDescription(this.issue.getDescription().trim().replaceAll("\\p{Cntrl}", ""));

			if(this.issueService.saveIssue(this.issue)){
				//check exist kanban issue by content and userstory
				if(!kanbanIssueService.existKanbanIssueByUserStoryAndSubject(this.issue.getParent().getUserStory(),this.issue.getSubject())){
					//add new kanban issue
					KanbanIssue addKanbanIssue = new KanbanIssue();
					addKanbanIssue.setUserStory(this.issue.getParent().getUserStory());
					addKanbanIssue.setSubject(this.issue.getSubject());
					addKanbanIssue.setDescription(this.issue.getDescription());
					addKanbanIssue.setNote(this.issue.getNote());
					addKanbanIssue.setColumnDone(false);
					addKanbanIssue.setRemain(this.issue.getRemain());
					addKanbanIssue.setEstimate(this.issue.getEstimate());
					addKanbanIssue.setPointFormat("1");
					addKanbanIssue.setType("Task");
					addKanbanIssue.setIsSubIssue(false);
					kanbanIssueService.saveKanbanIssue(addKanbanIssue);
				}
			}
			issueService.updateStatusOfIssueParent(this.issue.getParent());

			// Update status userstory
			UserStory userStory = this.userStoryService.findUserStoryByIssue(this.issue);
			UserStory.StatusType status = this.userStoryService.findStatusOfUserStory(userStory);
			userStory.setStatus(status);
			this.userStoryService.update(userStory);

			addAttachment();
			// insert into point remain
			updateRemainPointPerDay(this.issue);
			JSFUtils.addCallBackParam("save", true);
			JSFUtils.addSuccessMessage("msgs", this.utils.getMessage("myagile.SaveSuccess", null));
		} catch (Exception e) {
			JSFUtils.addWarningMessage("msgs", this.utils.getMessage("myagile.SaveUnsucces", null));
		}
	}

	@SuppressWarnings("static-access")
	public void updateIssue() {
		try {
			String typeFormatPoint = "";
			String remainPoint = this.issue.getRemain();
			String estimatePoint = this.issue.getEstimate();
			typeFormatPoint = this.issueService.checkingPointFormat(estimatePoint);
			estimatePoint = this.issueService.checkingEstimatePoint(estimatePoint);
			remainPoint = this.issueService.checkingRemainPoint(remainPoint);
			if ((this.issue.getStatus() != null) && (this.issue.getStatus().getType() == Status.StatusType.START)) {
				remainPoint = estimatePoint;
			}
			issueService.updatePointRemain(remainPoint, this.issue.getIssueId());
			this.issue.setRemain(remainPoint);
			this.issue.setEstimate(estimatePoint);
			this.issue.setPointFormat(typeFormatPoint);

			// Remove Control Characters
			this.issue.setSubject(this.issue.getSubject().replaceAll("\\p{Cntrl}", ""));
			this.issue.setNote(this.issue.getNote().replaceAll("\\p{Cntrl}", ""));
			this.issue.setDescription(this.issue.getDescription().trim().replaceAll("\\p{Cntrl}", ""));
			this.issueService.updateIssue(this.issue);
			//update kanban issue when exist kanban issue has the same subject
			if(kanbanIssueService.existKanbanIssueByUserStoryAndSubject(this.issue.getParent().getUserStory(),this.subjectIssueBeforeEdit)){
				//add new kanban issue
				kanbanIssueService.updateKanbanIssueByUserStoryAndSubjectAndIssue(this.issue.getParent().getUserStory(),this.subjectIssueBeforeEdit,this.issue);
			}
			if (this.issue.getParent() != null) {
				issueService.updateStatusOfIssueParent(this.issue.getParent());
				// Update status userstory
				UserStory userStory = userStoryService.findUserStoryByIssue(this.issue);
				UserStory.StatusType status = userStoryService.findStatusOfUserStoryBySprintAndUserStory(userStory,this.sprint);
				userStory.setStatus(status);
				userStoryService.update(userStory);
				//update kanbanissue status belong to userstory
				kanbanIssueService.updateAllKanbanIssueByUserStoryStatusOfTeam(userStory,sprint.getTeam());
			}
			updateRemainPointPerDay(this.issue);
			
			JSFUtils.addCallBackParam("edited", true);
			JSFUtils.addSuccessMessage("msgs", this.utils.getMessage("myagile.UpdateSuccess", null));
		} catch (Exception e) {
			JSFUtils.addWarningMessage("msgs", this.utils.getMessage("myagile.UpdateUnsuccess", null));
		}
	}

	/**
	 * Delete an issue and delete its all attachment
	 */
	public void deleteIssue() {
		Issue issueTemp = this.issue;
		try {
			this.attachmentListByIssue = this.attachmentService.findAttachmentByIssue(this.issue);
			if (this.attachmentListByIssue != null) {
				for (Attachment att : this.attachmentListByIssue) {
					this.attachmentService.deleteFile(att.getDiskFilename(), this.projectService.findProjectOfIssue(this.issue).getProjectId());
					this.attachmentService.delete(att);
				}
			}
			List<History> historyOfIssue = new ArrayList<History>();
			historyOfIssue = historyService.findHistoryByContainer("Issue", this.issue.getIssueId());
			for (History history : historyOfIssue) {
				historyService.delete(history);
			}
			Issue issueParent = issueTemp.getParent();
			this.issueService.deleteIssue(this.issue);
			//delete kanban issue if has the same subject with issue
			//check exist kanban issue by content and userstory
			if(kanbanIssueService.existKanbanIssueByUserStoryAndSubject(this.issue.getParent().getUserStory(),this.issue.getSubject())){
				kanbanIssueService.deleteAllKanbanIssueByUserStoryAndSubject(this.issue.getParent().getUserStory(),this.issue.getSubject());
			}
			if (issueParent != null) {
				issueService.updateStatusOfIssueParent(issueParent);
			}
			resetIssueForm();
			resetIssueValue();
			this.issueList.remove(issueTemp);
			this.filteredIssues = new ArrayList<Issue>(this.issueList);
			this.selectedIssues = new ArrayList<Issue>();
			resetAttachmentList();
		} catch (Exception a) {
		}
	}

	public void updateRemainPointPerDay(Issue issue) {
		PointRemain pointRM = this.pointRemainService.findPointRemainByIssueIdAndNowDate(issue.getIssueId());
		if (pointRM != null) {
			pointRM.setPointRemain(issue.getRemain());
			pointRM.setDateUpdate(new Date());
			this.pointRemainService.update(pointRM);
		} else {
			this.pointRemain = new PointRemain();
			this.pointRemain.setDateUpdate(new Date());
			this.pointRemain.setIssue(issue);
			this.pointRemain.setPointRemain(issue.getRemain());
			this.pointRemainService.save(this.pointRemain);
		}
	}

	public void handleCreateIssue() {
		resetIssueValue();
		resetIssueForm();
		resetAttachmentList();
		JSFUtils.addCallBackParam("create", true);
	}

	public void updateUserStoryWhenUpdateIssue(){
		if (this.issue.getIssueId() != null) {
			updateIssue();
			UserStory userStoryOfIssue = this.issue.getUserStory();
			if(userStoryOfIssue != null){
				// Update status userstory
				UserStory.StatusType status = userStoryService.findStatusOfUserStoryBySprintAndUserStory(userStoryOfIssue,this.sprint);
				userStoryOfIssue.setStatus(status);
				userStoryOfIssue.setName(this.issue.getSubject());
				userStoryOfIssue.setDescription(this.issue.getDescription());
				userStoryOfIssue.setNote(this.issue.getNote());
				if(userStoryService.update(userStoryOfIssue)){
					userStoryService.updateAllIssueOfUserStoryHaveTheSameContent(userStoryOfIssue);
					//update kanbanissue status belong to userstory
					kanbanIssueService.updateAllKanbanIssueByUserStoryStatusOfTeam(userStoryOfIssue,sprint.getTeam());
				}
			}
		}
		// Reset issueList and issue value
		resetIssueForm();
		resetIssueValue();
		resetAttachmentList();
		refreshIssueListTable();
	}
	
	public void handleSaveAndUpadteIssue() {
		if (this.issue.getIssueId() == null) {
			saveIssue();
			// Reset issueList and issue value
			resetIssueForm();
			resetIssueValue();
			resetAttachmentList();
			refreshIssueListTable();
		} else {
			
				//update userstory here and other issue have the same level
				UserStory userStoryOfIssue = this.issue.getUserStory();
				if(userStoryOfIssue != null){
					RequestContext.getCurrentInstance().execute("confirmUpdateUserStoryWhenUpdateIssue.show()");
				}else{
					//update child issue
					updateIssue();
					// Reset issueList and issue value
					resetIssueForm();
					resetIssueValue();
					resetAttachmentList();
					refreshIssueListTable();
				}
		}
		
	}
	
	public List<Status> getDoneAndAcceptedStatusOfSprint(){
		Status acceptedStatus = statusService.findStatusAcceptedBySprintId(this.sprint.getSprintId());
		Status doneStatus = statusService.findStatusDoneBySprintId(this.sprint.getSprintId());
		List<Status> status = new ArrayList<Status>();
		status.add(doneStatus);
		status.add(acceptedStatus);
		return status;
	}
	
	public void filterIssues() {
		this.filteredIssues = this.issueService.filterIssues(this.issueList, this.filterIssueType, this.filterIssueStatus, this.filterIssuePriority);
		this.selectedIssues = new ArrayList<Issue>();
	}

	public void exportIssuesPDF() throws DocumentException, IOException {
		this.exportFileService.exportIssuesPDF(this.selectedIssues);
	}

	public void exportIssuesExcel() throws DocumentException, IOException, WriteException, JRException {
		if (this.chooseFormatExcel == 1) {
			this.exportFileService.generatePlainExcel(this.selectedIssues);
		} else {
			this.exportFileService.generateRichExcel(this.selectedIssues);
		}
	}

	public void isSelectedIssueEmpty() {
		if ((getSelectedIssues() == null) || (getSelectedIssues().size() == 0)) {
			JSFUtils.addCallBackParam("isEmpty", true);
		} else {
			JSFUtils.addCallBackParam("exportType", JSFUtils.getRequestParameter("exportType"));
		}
	}

	/**
	 * Get total point of all issue in issueList
	 * 
	 * @return
	 */
	public float getIssuesTotalPoints() {
		try {
			return this.issueService.calculateIssuesTotalPoints(this.issueList);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * @param context
	 * @param validate
	 * @param value
	 */
	public void checkPointEstimate(FacesContext context, UIComponent validate, Object value) {
		this.pointRemainService.checkEstimatePoint(context, validate, value);
	}

	/**
	 * @param context
	 * @param validate
	 * @param value
	 */
	public void checkPointRemain(FacesContext context, UIComponent validate, Object value) {
		this.pointRemainService.checkPointRemain(context, validate, value, this.issue.getPointFormat());
	}

	/**
	 * Upload attachment file for issue
	 */
	public void uploadFile() {
		String filename = JSFUtils.getRequestParameter("filename");
		String diskFileName = this.attachmentService.fileNameProcess(FilenameUtils.removeExtension(filename));
		diskFileName = this.attachmentService.replaceFile(filename, diskFileName);
		this.attachment = new Attachment();
		this.attachment.setFilename(filename);
		this.attachment.setDiskFilename(diskFileName);
		this.attachment.setContainerId(this.issue.getIssueId());
		this.attachment.setContainerType(Attachment.ISSUE_ATTACHMENT);
		this.attachment.setTemp(false);
		this.attachment.setCreatedOn(new Date());
		this.attachment.setAuthor(this.utils.getLoggedInMember());
		this.attachmentService.save(this.attachment);
		Attachment newAttachment = this.attachmentService.findAttachmentById(this.attachment.getAttachmentId());
		if (this.attachmentNotAddList == null) {
			this.attachmentNotAddList = new ArrayList<Attachment>();
		}
		if (newAttachment.getContainerId() != null) {
			this.attachment.setTemp(false);
			this.attachmentService.moveAttachmentFile(this.attachment, this.projectId);
			//add file to userstory and other issue have the same level
			if(issue.getUserStory() != null){
				logger.warn("add attachment for userstory and other issue have the same level");
				this.attachmentService.addAttachmentForUserStoryFromIssueAttachment(attachment);
				this.attachmentService.addAttachmentForOtherIssueFromCurrentIssueAttachment(attachment);
			}
			
		} else {
			this.attachment.setTemp(true);
			this.attachmentNotAddList.add(newAttachment);
		}
		this.attachmentListByIssue = this.attachmentService.findAttachmentByIssue(this.issue);
	}

	/**
	 * Add attachment(s) for an issue
	 */
	public void addAttachment() {
		if (this.attachmentNotAddList != null) {
			for (Attachment att : this.attachmentNotAddList) {
				att.setContainerId(this.issue.getIssueId());
				att.setTemp(false);
				if (this.projectId != null) {
					this.attachmentService.moveAttachmentFile(att, this.projectId);
				}
			}
			this.attachmentNotAddList = new ArrayList<Attachment>();
		}
	}

	/**
	 * Delete an attachment in temp when editing issue
	 */
	public void deleteAttachmentNotAddById() {
		try {
			Attachment deleteAttachment = this.attachmentNotAdd;
			this.attachmentService.deleteFileInTemp(deleteAttachment.getDiskFilename());
			this.attachmentService.delete(deleteAttachment);
			this.attachmentNotAddList.remove(deleteAttachment);
			getAttachmentListByIssue();
		} catch (Exception e) {

		}
	}

	/**
	 * Delete all attachments in temp folder when creating new Issue
	 */
	public void deleteAttachmentNotAdded() {
		if (this.attachmentNotAddList != null) {
			for (int i = 0; i < this.attachmentNotAddList.size(); i++) {
				this.attachmentService.delete(this.attachmentNotAddList.get(i));
				this.attachmentService.deleteFileInTemp(this.attachmentNotAddList.get(i).getDiskFilename());
			}
		}
	}

	/**
	 * Delete an attachment
	 */
	public void deleteAttachmentOfIssue() {
		Attachment deleteAttachment = this.selectedAttachment;
		this.attachmentService.delete(deleteAttachment);
		this.attachmentService.deleteFile(deleteAttachment.getDiskFilename(), this.projectId);
		this.attachmentListByIssue = this.attachmentService.findAttachmentByIssue(this.issue);
		//delete attachment userstory and other issue have the same level
		if(deleteAttachment.getContainerType().equals(Attachment.ISSUE_ATTACHMENT)){
			//delete userstory attachment 
			this.attachmentService.deleteAttachmentUserStoryFromAttachmentIssue(deleteAttachment);
			//delete other issue have the same attachment
			this.attachmentService.deleteAttachmentOtherIssueFromAttachmentCurrentIssue(deleteAttachment);
		}
	}
	
	public boolean canChangeToAccepted(Issue issue){
		return DONE_COLUMN_STATUS.equalsIgnoreCase(issue.getStatus().getName());
	}

	public String getEstimatePointByFormat() {
		Issue issueTemp = (Issue) JSFUtils.resolveExpression("#{issue}");
		return this.issueService.getPointByFormat(issueTemp.getEstimate(), issueTemp.getPointFormat());
	}

	public String getRemainPointByFormat() {
		Issue issueTemp = (Issue) JSFUtils.resolveExpression("#{issue}");
		return this.issueService.getPointByFormat(issueTemp.getRemain(), issueTemp.getPointFormat());
	}

	public Issue getIssue() {
		if (this.issue == null) {
			this.issue = new Issue();
			if ((this.sprint != null) && (this.sprint.getSprintId() != null)) {
				this.issue.setSprint(this.sprintService.findSprintById(this.sprint.getSprintId()));
			}
		}
		return this.issue;
	}

	public void setIssue(Issue issue) {
		this.issue = issue;
	}

	public Issue getViewIssue() {
		if (this.viewIssue == null) {
			this.viewIssue = new Issue();
		}
		return this.viewIssue;
	}

	public void setViewIssue(Issue viewIssue) {
		viewIssue = this.issueService.findIssueById(viewIssue.getIssueId());
		viewIssue.setEstimate(this.issueService.getPointByFormat(viewIssue.getEstimate(), viewIssue.getPointFormat()));
		viewIssue.setRemain(this.issueService.getPointByFormat(viewIssue.getRemain(), viewIssue.getPointFormat()));
		this.viewIssue = viewIssue;
	}
	
	public boolean isPastSprint(Sprint sprint){
		return sprintService.isPastSprint(sprint);
	}

	public Project getViewProject() {
		return this.viewProject;
	}

	public void setViewProject(Project viewProject) {
		this.viewProject = viewProject;
	}

	public Sprint getSprint() {
		if (this.sprint == null) {
			this.sprint = !this.sprintList.isEmpty() ? this.sprintList.get(0) : new Sprint();
		}
		return this.sprint;
	}

	public void setSprint(Sprint sprint) {
		this.sprint = sprint;
	}

	public Attachment getAttachment() {
		if (this.attachment == null) {
			this.attachment = new Attachment();
		}
		return this.attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment getSelectedAttachment() {
		if (this.selectedAttachment == null) {
			this.selectedAttachment = new Attachment();
		}
		return this.selectedAttachment;
	}

	public void setSelectedAttachment(Attachment selectedAttachment) {
		this.selectedAttachment = selectedAttachment;
	}

	public Attachment getAttachmentNotAdd() {
		return this.attachmentNotAdd;
	}

	public void setAttachmentNotAdd(Attachment attachmentNotAdd) {
		this.attachmentNotAdd = attachmentNotAdd;
	}

	public List<Project> getProjectList() {
		if (this.projectList == null) {
			this.projectList = new ArrayList<Project>();
			if ((this.teamId != null) || (this.teamId != 0)) {
				this.projectList = this.projectService.findAllProjectsByTeam(this.teamService.findTeamByTeamId(this.teamId));
			}
		}
		return this.projectList;
	}

	public List<Team> getTeamList() {
		if (this.teamList == null) {
			this.teamList = new ArrayList<Team>();
			this.teamList = this.teamService.findTeamsByMemberAndOwner(this.utils.getLoggedInMember());
		}
		return this.teamList;
	}

	public List<Sprint> getSprintList() {
		if (this.sprintList == null) {
			this.sprintList = new ArrayList<Sprint>();
			if (this.teamId != null) {
				this.sprintList = this.sprintService.findSprintsByTeamId(this.teamId);
			}
		}
		return this.sprintList;
	}
	
	public List<Status> getStatusList() {
		if (this.statusList == null) {
			if ((this.sprint != null) && (this.sprint.getSprintId() != null)) {
				this.statusList = this.statusService.findStatusBySprintId(this.sprint.getSprintId());
			}
		}
		return this.statusList;
	}

	public List<Member> getMemberList() {
		if (this.memberList == null) {
			this.memberList = new ArrayList<Member>();
			if (this.teamId != null) {
				this.memberList = this.memberService.findMembersByTeam(this.teamService.findTeamByTeamId(this.teamId));
			}
		}
		return this.memberList;
	}

	public List<Issue> getIssueList() {
		if (this.issueList == null) {
			this.issueList = new ArrayList<Issue>();
			if ((this.sprint != null) && (this.sprint.getSprintId() != null)) {
				this.issueList = this.issueService.findIssuesIsTaskBySprintId(this.sprint.getSprintId());
			}
		}
		return this.issueList;
	}

	public List<Issue> getIssueParentList() {
		if (this.issueParentList == null) {
			this.issueParentList = new ArrayList<Issue>();
			if ((this.sprint != null) && (this.sprint.getSprintId() != null)) {
				this.issueParentList = this.issueService.findIssuesParent(this.projectId, this.sprint.getSprintId());
			}
		}
		return this.issueParentList;
	}

	public List<Issue> getSelectedIssues() {
		if (this.selectedIssues == null) {
			this.selectedIssues = new ArrayList<Issue>();
		}
		return this.selectedIssues;
	}

	public void setSelectedIssues(List<Issue> selectedIssues) {
		this.selectedIssues = selectedIssues;
	}

	public List<Issue> getFilteredIssues() {
		if (this.filteredIssues == null) {
			this.filteredIssues = this.issueList;
		}
		return this.filteredIssues;
	}

	public List<Attachment> getAttachmentNotAddList() {
		if (this.attachmentNotAddList == null) {
			this.attachmentNotAddList = new ArrayList<Attachment>();
		}
		return this.attachmentNotAddList;
	}

	public List<Attachment> getAttachmentListByIssue() {
		if (this.attachmentListByIssue == null) {
			this.attachmentListByIssue = new ArrayList<Attachment>();
		}
		return this.attachmentListByIssue;
	}

	public Long getProjectId() {
		if (this.projectId == null) {
			this.projectId = !this.projectList.isEmpty() ? this.projectList.get(0).getProjectId() : 0;
		}
		return this.projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getTeamId() {
		if (this.teamId == null) {
			this.teamId = !this.teamList.isEmpty() ? this.teamList.get(0).getTeamId() : 0;
		}
		return this.teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public String getFilterIssueType() {
		return this.filterIssueType;
	}

	public void setFilterIssueType(String filterIssueType) {
		this.filterIssueType = filterIssueType;
	}

	public String getFilterIssueStatus() {
		return this.filterIssueStatus;
	}

	public void setFilterIssueStatus(String filterIssueStatus) {
		this.filterIssueStatus = filterIssueStatus;
	}

	public String getFilterIssuePriority() {
		return this.filterIssuePriority;
	}

	public void setFilterIssuePriority(String filterIssuePriority) {
		this.filterIssuePriority = filterIssuePriority;
	}

	public Integer getChooseFormatExcel() {
		return this.chooseFormatExcel;
	}

	public void setChooseFormatExcel(Integer chooseFormatExcel) {
		this.chooseFormatExcel = chooseFormatExcel;
	}

}
