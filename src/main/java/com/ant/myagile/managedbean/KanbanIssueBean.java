package com.ant.myagile.managedbean;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanHistory;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberIssue;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.model.UserStory.PriorityType;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanHistoryService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.MemberIssueService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;

@Component("kanbanIssueBean")
@Scope("request")
public class KanbanIssueBean 
{
	private static final String ROOT_FOLDER_PATH = MyAgileFileUtils.getRootStorageLocation();
	private static final String ATTACHMENT_FOLDER = "/attachment";
	
	@Autowired
	private KanbanIssueService kanbanIssueService;
	@Autowired
	private AttachmentService attachmentService;
	@Autowired
	private MemberIssueService memberIssueService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private UserStoryService userStoryService;
	@Autowired
	private Utils utils;
	@Autowired
	private PointRemainService pointRemainService;
	@Autowired
	private IssueService issueService;
	@Autowired
	private KanbanHistoryService kanbanHistoryService;
	
	public KanbanIssue editedKanbanIssue;
	public UserStory editedUserStory;
	public List<Long> assignedMembers;
	public List<Member> teamMembers;
	public List<Attachment> userStoryAttachments;
	
	@PostConstruct
	public void initial()
	{
		String kanbanIssueId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editedKanbanIssueId");
		
		if(kanbanIssueId != null)
		{
			assignedMembers = new ArrayList<Long>();
			editedKanbanIssue = kanbanIssueService.findKanbanIssueById(Long.valueOf(kanbanIssueId));
			editedUserStory = editedKanbanIssue.getUserStory();
			List<Member> members = 	memberIssueService.getMembersByIssue(Long.valueOf(kanbanIssueId));
			if(members != null && members.size() > 0){
				for (Member mem : members ){
					assignedMembers.add(mem.getMemberId());
				}
			}
			
			teamMembers = 	memberService.findDevelopeMembersByTeam(editedKanbanIssue.getTeam());
		}
	}
	
	public List<KanbanHistory> getKanbanHistoryOfKanbanIssue(KanbanIssue kanbanIssue) 
	{
		return kanbanHistoryService.getByKanbanIssue(kanbanIssue);
	}
	
	public KanbanIssue getViewKanbanIssue()
	{
		String kanbanIssueId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("kanbanIssueId");
		
		KanbanIssue kanbanIssue = new KanbanIssue();
		kanbanIssue = kanbanIssueService.findKanbanIssueById(Long.valueOf(kanbanIssueId));
		
		return kanbanIssue;
	}
	
	public void viewUserStory(String userStoryId)
	{
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/userstory/viewDetail" + "?userStory=" + userStoryId;
		JSFUtils.redirect(contextPath);
	}
	
	public void goToEditKanbanIssue(String kanbanIssueId)
	{
		editedKanbanIssue = kanbanIssueService.findKanbanIssueById(Long.valueOf(kanbanIssueId));
		editedUserStory = editedKanbanIssue.getUserStory();
		userStoryAttachments = attachmentService.findAttachmentByUserStory(editedUserStory);
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/kanbanIssue/edit" + "?editedKanbanIssueId=" + kanbanIssueId;
		JSFUtils.redirect(contextPath);
	}
	
	public void goToViewDetail()
	{
		String editkanbanIssueId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editedKanbanIssueId");
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/kanbanIssue/viewDetail" + "?kanbanIssueId=" + editkanbanIssueId +"&userStory="+editedKanbanIssue.getUserStory().getUserStoryId();
		JSFUtils.redirect(contextPath);
	}
	
	public List<Long> getAssignedMembers()
	{
		return assignedMembers;
	}

	public void setAssignedMembers(List<Long> assignedMembers)
	{
		this.assignedMembers = assignedMembers;
	}

	public List<Attachment> getAttachmentsOfUserStory(UserStory userStory)
	{	
		return attachmentService.findAttachmentByUserStory(userStory);
	}
	
	public List<Attachment> getAttachmentsOfKanbanIssue(KanbanIssue kanbanIssue)
	{
		List<Attachment> attachments = new ArrayList<Attachment>();
		
		if(kanbanIssue.getIsSubIssue()!= true){
			attachments.clear();
			attachments.addAll(attachmentService.findAttachmentByUserStory(kanbanIssue.getUserStory()));
		}else{
			attachments.clear();
			attachments.addAll(attachmentService.findAttachmentByKanbanIssue(kanbanIssue));
		}
		return attachments;
	}
	
	public List<Attachment> getAttachmentsOfEditedKanbanIssue()
	{
		List<Attachment> attachments = new ArrayList<Attachment>();
		String editkanbanIssueId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editedKanbanIssueId");
		KanbanIssue kanbanIssue = new KanbanIssue();
		kanbanIssue = kanbanIssueService.findKanbanIssueById(Long.valueOf(editkanbanIssueId));
		
		if(kanbanIssue.getIsSubIssue()!= true){
			attachments.clear();
			attachments.addAll(attachmentService.findAttachmentByUserStory(kanbanIssue.getUserStory()));
		}else{
			attachments.clear();
			attachments.addAll(attachmentService.findAttachmentByKanbanIssue(kanbanIssue));
		}
		
		return attachments;
	}
	
	
	public boolean isExistedFile(String fileName, String projectId)
	{
		boolean isExist = false;
		String filename = ROOT_FOLDER_PATH + "/projects/P" + projectId
				+ ATTACHMENT_FOLDER + "/" + fileName;

		File f = new File(filename);
		if (f.exists()) {
			isExist = true;
		}
		return isExist;
	}
	
	
	public void uploadFile() 
	{
		String editkanbanIssueId = JSFUtils.getRequestParameter("editedKanbanIssueId");
		String fileName = JSFUtils.getRequestParameter("filename");
		String diskFileName = attachmentService.fileNameProcess(FilenameUtils.removeExtension(fileName));
		diskFileName = this.attachmentService.replaceFile(fileName, diskFileName);
		KanbanIssue kanbanIssue = new KanbanIssue();
		kanbanIssue = kanbanIssueService.findKanbanIssueById(Long.valueOf(editkanbanIssueId));
		if(kanbanIssue != null){
			if(kanbanIssue.getIsSubIssue() == true){
				addFileForKanbanIssue(kanbanIssue, fileName, diskFileName);
			}else{
				Attachment attachmentAfterSave = addFileForUserStory(kanbanIssue.getUserStory(), fileName, diskFileName);
				//history for add file kanban issue
				kanbanHistoryService.historyForAddAttachment(kanbanIssue,attachmentAfterSave);
			}
			Long issueId = kanbanIssue.getIssueOfLastSprint();
			if(issueId != null){
				addFileForIssue(issueService.findIssueById(issueId), fileName,diskFileName);
			}
		}
	}
	
	
	private Attachment addFileForUserStory(UserStory userStory, String fileName, String diskFileName)
	{
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
		
		if (newAttachment.getContainerId() != null) 
		{
			attachment.setTemp(false);
			attachmentService.moveAttachmentFile(attachment, userStory.getProject().getProjectId());
			
		}
		
		return attachment;
	}
	
	
	private void addFileForKanbanIssue(KanbanIssue kanbanIssue, String fileName, String diskFileName)
	{
		Attachment attachment = new Attachment();
		attachment.setFilename(fileName);
		attachment.setDiskFilename(diskFileName);
		attachment.setContainerId(kanbanIssue.getIssueId());
		attachment.setContainerType(Attachment.KANBAN_ISSUE_ATTACHMENT);
		attachment.setTemp(false);
		attachment.setCreatedOn(new Date());
		attachment.setAuthor(this.utils.getLoggedInMember());
		this.attachmentService.save(attachment);
		Attachment newAttachment = this.attachmentService.findAttachmentById(attachment.getAttachmentId());
		
		if (newAttachment.getContainerId() != null)
		{
			attachment.setTemp(false);
			attachmentService.moveAttachmentFile(attachment,kanbanIssue.getUserStory().getProject().getProjectId());
			//history for add file kanban issue
			kanbanHistoryService.historyForAddAttachment(kanbanIssue,newAttachment);
		}
	}
	
	
	private void addFileForIssue(Issue issue, String fileName, String diskFileName)
	{
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
	
	
	public void deleteAttachment()
	{
		String attachmentId = JSFUtils.getRequestParameter("attachmentId");
		String editkanbanIssueId = JSFUtils.getRequestParameter("editedKanbanIssueId");
		Attachment  attachment = attachmentService.findAttachmentById(Long.valueOf(attachmentId));
		String diskFileName = attachment.getDiskFilename();
		KanbanIssue KanbanIssue = kanbanIssueService.findKanbanIssueById(Long.valueOf(editkanbanIssueId));
		if(attachment != null){
			attachmentService.delete(attachment);
			attachmentService.deleteFile(attachment.getDiskFilename(),KanbanIssue.getUserStory().getProject().getProjectId());
			//add history for delete attachment
			kanbanHistoryService.historyForDeleteAttachment(KanbanIssue, attachment);
		}
		Long issueId = KanbanIssue.getIssueOfLastSprint();
		if(issueId != null){
			Issue issue = issueService.findIssueById(issueId);
			Attachment issueAtt = attachmentService.findAttachmentByIssueAnDiskFileName(issue,diskFileName);
			if(issueAtt != null){
				attachmentService.delete(issueAtt);
				
			}
		}
	
	}
	
	
	public void fileNotFoundMsg()
	{
		JSFUtils.addWarningMessage("downloadMsgs", Utils.getMessage("myagile.FileNotFound", null));
	}
	
	public void checkPointEstimate(FacesContext context, UIComponent validate, Object value)
	{
		pointRemainService.checkEstimatePoint(context, validate, value);
	}
	
	public void checkPointRemain(FacesContext context, UIComponent validate, Object value) 
	{
		pointRemainService.checkPointRemain(context, validate, value, editedKanbanIssue.getPointFormat());
	}
	
	public void updateKanbanIssue()
	{
		String remainPoint = editedKanbanIssue.getRemain().trim();
		String estimatePoint = editedKanbanIssue.getEstimate().trim();
		estimatePoint = issueService.checkingEstimatePoint(estimatePoint);
		remainPoint = this.issueService.checkingRemainPoint(remainPoint);
		
		editedKanbanIssue.setRemain(remainPoint);
		editedKanbanIssue.setEstimate(estimatePoint);
		editedKanbanIssue.setPointFormat("1");
		
		editedKanbanIssue.setSubject(editedKanbanIssue.getSubject().replaceAll("\\p{Cntrl}", ""));
		editedKanbanIssue.setNote(editedKanbanIssue.getNote().replaceAll("\\p{Cntrl}", ""));
		editedKanbanIssue.setDescription(editedKanbanIssue.getDescription().trim().replaceAll("\\p{Cntrl}", ""));
		
		List<Member> oldAssignees = memberIssueService.getMembersByIssue(editedKanbanIssue.getIssueId());
		
		if(oldAssignees != null && oldAssignees.size() > 0)
		{
			memberIssueService.deleteAllByIssue(editedKanbanIssue.getIssueId());
		}
		
		for (Long memberId : assignedMembers) 
		{
			MemberIssue memberIssue = new MemberIssue(memberService.findMemberById(memberId), editedKanbanIssue);
			memberIssueService.save(memberIssue);
			
		}
		
		KanbanIssue kanbanIssueBeforeUpdate = kanbanIssueService.findKanbanIssueById(editedKanbanIssue.getIssueId());
		kanbanIssueService.updateKanbanIssue(editedKanbanIssue);
		//add history for update kanban issue
		kanbanHistoryService.historyForEditKanbanIssue(kanbanIssueBeforeUpdate, editedKanbanIssue, oldAssignees);
		
		if(editedKanbanIssue.getIsSubIssue() != true){
			updateUserStory(editedKanbanIssue);
		}else{
			if(editedKanbanIssue.getIssueOfLastSprint() != null){
				updateIssue(editedKanbanIssue);
			}
		}
		
		JSFUtils.addSuccessMessage("msgs", this.utils.getMessage("myagile.UpdateSuccess", null));
	}
	
	
	private void updateUserStory(KanbanIssue kanbanIssue)
	{
		UserStory userStory = kanbanIssue.getUserStory();
		userStory.setName(kanbanIssue.getSubject());
		userStory.setDescription(kanbanIssue.getDescription());
		userStory.setNote(kanbanIssue.getNote());
		userStory.setPriority(PriorityType.valueOf(kanbanIssue.getPriority()));
		userStoryService.update(userStory);
	}
	
	private void updateIssue(KanbanIssue kanbanIssue)
	{
		Long issueId = kanbanIssue.getIssueOfLastSprint();
		Issue issue = issueService.findIssueById(issueId);
		if(issue != null){
			issue.setSubject(kanbanIssue.getSubject());
			issue.setDescription(kanbanIssue.getDescription());
			issue.setNote(kanbanIssue.getNote());
			issue.setType(kanbanIssue.getType());
			issueService.updateIssue(issue);
		}
	}
	

	public UserStory getEditedUserStory() 
	{
		return editedUserStory;
	}

	public void setEditedUserStory(UserStory editedUserStory) 
	{
		this.editedUserStory = editedUserStory;
	}

	public void setEditedKanbanIssue(KanbanIssue editedKanbanIssue)
	{
		this.editedKanbanIssue = editedKanbanIssue;
	}
	
	public KanbanIssue getEditedKanbanIssue(){
		return editedKanbanIssue;
	}
	
	public List<Attachment> getUserStoryAttachments() 
	{
		return userStoryAttachments;
	}

	public void setUserStoryAttachments(List<Attachment> userStoryAttachments) 
	{
		this.userStoryAttachments = userStoryAttachments;
	}

	public List<Member> getTeamMembers() 
	{
		return teamMembers;
	}

	public void setTeamMembers(List<Member> teamMembers) 
	{
		this.teamMembers = teamMembers;
	}

	
	
}
