package com.ant.myagile.managedbean;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.io.FilenameUtils;
import org.apache.xmlbeans.impl.regex.REUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanHistoryService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;

@Component("userStoryBean")
@Scope("request")
public class UserStoryBean {
	
	private static final String POINT_NUMBER_FORMAT = "2";
	private static final String ROOT_FOLDER_PATH = MyAgileFileUtils.getRootStorageLocation();
	private static final String ATTACHMENT_FOLDER = "/attachment";
	private static final int MAX_BUSSINESS_VALUE = 1000;
	
	@Autowired
	UserStoryService userStoryService;
	
	@Autowired
	IssueService issueService;
	
	@Autowired
	AttachmentService attachmentService;
	
	@Autowired
	Utils utils;
	
	@Autowired
	SprintService sprintService;
	
	@Autowired
	KanbanIssueService kanbanIssueService;
	
	public UserStory editedUserStory;
	
	@PostConstruct
	public void initial(){
		String editUserstoryId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editedUserStoryId");
		if(editUserstoryId != null){
			editedUserStory = userStoryService.findUserStoryById(Long.valueOf(editUserstoryId));
		}
	}
	
	public UserStory getUserStory(){
		String userstoryId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("userStory");	
		UserStory userStory = userStoryService.findUserStoryById(Long.valueOf(userstoryId).longValue());
		return userStory;
	}
	
	public List<Issue> getRelatedIssuesOfUserStory(Long userStoryId){
		List<Issue> relatedIssues = issueService.findNoChidrenIssuesByUserStory(userStoryId);
		return relatedIssues;
	}
	
	public List<Attachment> getAttachmentsOfUserStory(UserStory userStory){
		return attachmentService.findAttachmentByUserStory(userStory);
	}
	
	@SuppressWarnings("static-access")
	public String getAssignedMember(Member member) {
		try {
			return this.utils.checkName(member);
		} catch (Exception e) {
			return "";
		}
	}
	
	public int findProgressOfIssue(long issueID) {
		return this.issueService.findProgressOfIssue(issueID);
	}
	
	public String findPointOfIssue(Issue issue, boolean estimate) {
		String result = "";
		if (estimate) {
			result = issue.getEstimate();
		} else {
			result = this.issueService.findCurrentPointRemainOfIssue(issue);
		}
		if (issue.getPointFormat().equals(POINT_NUMBER_FORMAT)) {
			return String.valueOf(Math.round(Utils.convertPoint(result)));
		}
		return result;
	}
	
	public boolean isExistedFile(String fileName, String projectId){
		boolean isExist = false;
		String filename = ROOT_FOLDER_PATH + "/projects/P" + projectId
				+ ATTACHMENT_FOLDER + "/" + fileName;

		File f = new File(filename);
		if (f.exists()) {
			isExist = true;
		}
		return isExist;
	}
	
	public void fileNotFoundMsg(){
		JSFUtils.addWarningMessage("downloadMsgs",
				Utils.getMessage("myagile.FileNotFound", null));
		
	}
	
	
	public void goToEditUserStoryPage(){
		String editedUserStoryId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editedUserStoryId");
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/userstory/edit?editedUserStoryId=" + editedUserStoryId;
		JSFUtils.redirect(contextPath);
	}

	public List<Attachment> getAttachmentsOfEditedUserStory(){
		List<Attachment> attachments = new ArrayList<Attachment>();
		String editUserStoryId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editedUserStoryId");
		UserStory userStory = new UserStory();
		userStory = userStoryService.findUserStoryById(Long.valueOf(editUserStoryId));
		if(userStory != null){
			attachments.addAll(attachmentService.findAttachmentByUserStory(userStory));
		}
		return attachments;
	}
	
	public void uploadFile() {
		String editUserStoryId = JSFUtils.getRequestParameter("editedUserStoryId");
		String fileName = JSFUtils.getRequestParameter("filename");
		String diskFileName = attachmentService.fileNameProcess(FilenameUtils.removeExtension(fileName));
		diskFileName = this.attachmentService.replaceFile(fileName, diskFileName);
		UserStory userStory = new UserStory();
		userStory = userStoryService.findUserStoryById(Long.valueOf(editUserStoryId));
		if(userStory != null){
			Attachment attachmentAfterSave = addFileForUserStory(userStory, fileName, diskFileName);
			}
		KanbanIssue kanbanIssue =  kanbanIssueService.findKanbanIssueIsNotSubIssueOfUserStory(userStory);
		if(kanbanIssue != null){
			Long issueId = kanbanIssue.getIssueOfLastSprint();
			if(issueId != null){
				addFileForIssue(issueService.findIssueById(issueId), fileName,diskFileName);

			}
		}
	}
	
	private Attachment addFileForUserStory(UserStory userStory, String fileName, String diskFileName){
		Attachment attachment = new Attachment();
		attachment.setFilename(fileName);
		attachment.setDiskFilename(diskFileName);
		attachment.setContainerId(userStory.getUserStoryId());
		attachment.setContainerType(Attachment.USERSTORY_ATTACHMENT);
		attachment.setTemp(false);
		attachment.setCreatedOn(new Date());
		attachment.setAuthor(this.utils.getLoggedInMember());
		this.attachmentService.save(attachment);
		Attachment newAttachment = this.attachmentService.findAttachmentById(attachment.getAttachmentId());
		if (newAttachment.getContainerId() != null) {
			attachment.setTemp(false);
			attachmentService.moveAttachmentFile(attachment, userStory.getProject().getProjectId());
			
		}
		
		return attachment;
	}
	
	private void addFileForIssue(Issue issue, String fileName, String diskFileName){
		Attachment attachment = new Attachment();
		attachment.setFilename(fileName);
		attachment.setDiskFilename(diskFileName);
		attachment.setContainerId(issue.getIssueId());
		attachment.setContainerType(Attachment.ISSUE_ATTACHMENT);
		attachment.setTemp(false);
		attachment.setCreatedOn(new Date());
		attachment.setAuthor(this.utils.getLoggedInMember());
		this.attachmentService.save(attachment);
	}
	
	public void deleteAttachment(){
		String attachmentId = JSFUtils.getRequestParameter("attachmentId");
		String editedUserStoryId = JSFUtils.getRequestParameter("editedUserStoryId");
		Attachment  attachment = attachmentService.findAttachmentById(Long.valueOf(attachmentId));
		String diskFileName = attachment.getDiskFilename();
		UserStory userStory = new UserStory();
		userStory = userStoryService.findUserStoryById(Long.valueOf(editedUserStoryId));
		if(attachment != null){
			attachmentService.delete(attachment);
			attachmentService.deleteFile(attachment.getDiskFilename(),userStory.getProject().getProjectId());
		}
		KanbanIssue kanbanIssue =  kanbanIssueService.findKanbanIssueIsNotSubIssueOfUserStory(userStory);
		if(kanbanIssue != null){
			Long issueId = kanbanIssue.getIssueOfLastSprint();
			if(issueId != null){
				Issue issue = issueService.findIssueById(issueId);
				Attachment issueAtt = attachmentService.findAttachmentByIssueAnDiskFileName(issue,diskFileName);
				if(issueAtt != null){
					attachmentService.delete(issueAtt);
				
			}
			}
		}
	
	}
	
	public void validateValue(FacesContext context, UIComponent validate, Object value) throws ValidatorException {

		if (!utils.isValueOfRange(value.toString(), 0, MAX_BUSSINESS_VALUE)) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(utils.getMessage("myagile.backlog.RangeOfValue", null));
			throw new ValidatorException(msg);
		}
	}
	
	public void validateUserStoryForEdit(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		String editUserstoryId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editedUserStoryId");
		UserStory userStory = userStoryService.findUserStoryById(Long.valueOf(editUserstoryId));
		if(userStory != null){
			if (this.userStoryService.checkUserStoryForEdit(Utils.standardizeString(value.toString()), userStory.getProject().getProjectId(), userStory.getUserStoryId()) != null) {
				@SuppressWarnings("static-access")
				FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.Exists", null));
				throw new ValidatorException(msg);
			}
		}
	}
	
	public void goToViewDetail(){
		String editedUserStoryId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editedUserStoryId");
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/userstory/viewDetail" + "?userStory=" + editedUserStoryId; 
		JSFUtils.redirect(contextPath);
	}
	
	public void updateUserStory(){
		userStoryService.update(editedUserStory);
		KanbanIssue kanbanIssue = kanbanIssueService.findKanbanIssueIsNotSubIssueOfUserStory(editedUserStory);
		if(kanbanIssue != null){
			kanbanIssue.setSubject(editedUserStory.getName());
			kanbanIssue.setDescription(editedUserStory.getDescription());
			kanbanIssue.setNote(editedUserStory.getNote());
			kanbanIssue.setPriority(String.valueOf(editedUserStory.getPriority()));
			kanbanIssueService.updateKanbanIssue(kanbanIssue);
			
			Long issueId = kanbanIssue.getIssueOfLastSprint();
			if(issueId != null){
				Issue issue = issueService.findIssueById(Long.valueOf(issueId));
				if(issue != null){
					issue.setSubject(editedUserStory.getName());
					issue.setDescription(editedUserStory.getDescription());
					issue.setNote(editedUserStory.getNote());
					issue.setPriority(String.valueOf(editedUserStory.getPriority()));
					issueService.updateIssue(issue);
				}
			}
		}
		JSFUtils.addSuccessMessage("msgs", this.utils.getMessage("myagile.UpdateSuccess", null));
	}
	
	
	public UserStory getEditedUserStory() {
		return editedUserStory;
	}

	public void setEditedUserStory(UserStory editedUserStory) {
		this.editedUserStory = editedUserStory;
	}

	

}
