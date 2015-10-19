package com.ant.myagile.managedbean;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.DisappearTaskConfiguration;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.KanbanPointRemain;
import com.ant.myagile.model.KanbanStatus;
import com.ant.myagile.model.KanbanStatus.StatusType;
import com.ant.myagile.model.KanbanSwimline;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.DisappearTaskConfigurationService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanHistoryService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.KanbanPointRemainService;
import com.ant.myagile.service.KanbanStatusService;
import com.ant.myagile.service.KanbanSwimlineService;
import com.ant.myagile.service.MemberIssueService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.PointRemainService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("kanbanBean")
@Scope("session")
public class KanbanBean {
	
	private int NUMBER_DATE_OF_WEEK = 7;
	
	private List<Team> listTeams;
	private List<Project> listProject;
	private Long currentProjectId;
	private List<Member> listTeamMembers;
	private UserStory addUserStory;
	private String estimateInAddBackLog;
	private List<UserStory> userStoriesOfProject;
	private Long idDeleteUserStory;
	private String nameDeleteUserStory;
	private Long idSortUserStory;
	private boolean showFormEditUserStory = false;
	private boolean showFormAddUserStory = false;
	private Long idEditUserStory;
	private UserStory editUserStory;
	private KanbanIssue addTask;
	private KanbanIssue editTask;
	private Long idEditTask = 0L;
	private Long idDeleteTask;
	private List<Issue> issueList;
	private boolean saveSuccess = false;
	private boolean updateSuccess = false;
	private Team currentTeam;
	private Long currentTeamId;
	private String pointRemain;
	private String pointR;
	private DisappearTaskConfiguration disappearTaskConfiguration;
	private KanbanIssue viewKanbanIssue;
	private Attachment attachment;
	private List<Attachment> notAddedAttachmentList;
	private Attachment deleteAttachment;
	private List<Attachment> editAttachmentList;
	private List<KanbanStatus> kanbanStatusTempList= new ArrayList<KanbanStatus>();
	private List<KanbanSwimline> kanbanSwimlines= new ArrayList<KanbanSwimline>();
	private List<KanbanSwimline> kanbanSwimlinesTemp= new ArrayList<KanbanSwimline>();
	private Long firstSwimline;
	private KanbanSwimline firstSwimlineObject;
	private KanbanStatus acceptedStatus;
	
	private StateLazyLoading lazyLoadingProductBacklogs;
	private boolean remainingProductBacklogs = true;
	private int totalRowProductBacklog = 0;
	
	private static final int MAX_BUSSINESS_VALUE = 1000;
	
	private final String idScriptView = "script";
	private final String idRenderUserstoryOfProject = "render-userstory-of-project";
	
	private int totalAllCellWith = 0;
	
	private Logger logger = Logger.getLogger(KanbanBean.class);
	
	@Autowired
	private TeamService teamService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private UserStoryService userStoryService;
	@Autowired
	private Utils utils;
	@Autowired
	private KanbanStatusService kanbanStatusService;
	@Autowired
	private KanbanSwimlineService kanbanSwimlineService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private MemberIssueService memberIssueService;
	@Autowired
	private KanbanIssueService kanbanIssueService;
	@Autowired
	private PointRemainService pointRemainService;
	@Autowired
	private KanbanPointRemainService kanbanPointRemainService;
	@Autowired
	private DisappearTaskConfigurationService disappearTaskConfigurationService;
	@Autowired
	private IssueService issueService;
	@Autowired
	private KanbanHistoryService kanbanHistoryService;
	@Autowired
	AttachmentService attachmentService;
	
	private List<KanbanStatus> statusList= new ArrayList<KanbanStatus>();
	
	@PostConstruct
	public void init()
	{
		defaultStateLoadingUserProjects();
		//new add userstory
		addUserStory = new UserStory();
		addUserStory.setValue(0);
		
		//new add task 
		addTask = new KanbanIssue();
		editTask = new KanbanIssue();
		
		this.setListTeams(this.teamService.findTeamsByMemberAndOwner(this.utils.getLoggedInMember()));
		
		if(!this.listTeams.isEmpty()){
				this.currentTeam = this.getListTeams().get(0);
				currentTeamId = currentTeam.getTeamId();
				//init kanbanstatsu
				initKanbanStatus();
				//init kanban swimline
				initKanbanSwimline();
				
				this.listTeamMembers = this.memberService.findDevelopeMembersByTeam(this.getListTeams().get(0));
				//get project from team
				this.listProject = this.projectService.findAllProjectsByTeam(this.currentTeam);
				if(!this.listProject.isEmpty()){
					this.currentProjectId = this.listProject.get(0).getProjectId();
					loadUserStoryOfCurrentProject();
				}else{
					this.listProject = this.projectService.findByOwnerId(this.utils.getLoggedInMember().getMemberId());
				}
		}else{
			//get project from own
			this.listProject = this.projectService.findByOwnerId(this.utils.getLoggedInMember().getMemberId());
			if(!this.listProject.isEmpty()){
				this.currentProjectId = this.listProject.get(0).getProjectId();
				loadUserStoryOfCurrentProject();
			}
		}
	}
	
	public int calculateTotalAllWithCell(){
		int total = 0;
		int defaultWithColumn = 20;
		for(KanbanStatus kanbanStatus : statusList){
			if(!kanbanStatus.getType().equals(KanbanStatus.StatusType.ACCEPTED_HIDE)){
				if(kanbanStatus.getWidth() != null){
					total = total + kanbanStatus.getWidth() * 16;
				} else {
					total = total + defaultWithColumn * 16;
				}
			}
		}
		return total;
	}
	
	public void loadUserStoryOfCurrentProject() {
		this.userStoriesOfProject = this.userStoryService.findLazyUserStoryToDoAndProgress(currentProjectId,lazyLoadingProductBacklogs);
		int totalUserStoryOfProject = this.userStoryService.countTotalUserStoryToDoAndProgress(currentProjectId);
		this.totalRowProductBacklog = totalUserStoryOfProject;
		if(userStoriesOfProject.size() == totalUserStoryOfProject){
			remainingProductBacklogs = false;
		}else{
			remainingProductBacklogs = true;
		}
	}
	
	public void loadMoreProductBacklog(){
		int newTotalRow = lazyLoadingProductBacklogs.getLimit() + lazyLoadingProductBacklogs.getStep();
		lazyLoadingProductBacklogs.setLimit(newTotalRow);
		loadUserStoryOfCurrentProject();
	}
	
	private void defaultStateLoadingUserProjects() {
		lazyLoadingProductBacklogs = new StateLazyLoading();
		lazyLoadingProductBacklogs.setStart(0);
		lazyLoadingProductBacklogs.setLimit(13);
		lazyLoadingProductBacklogs.setStep(20);
	}

	private void initKanbanStatus() {
		List<KanbanStatus> lKanbanStatusForTeam = kanbanStatusService.findKanbanStatusByTeamId(this.currentTeam.getTeamId());
		if(lKanbanStatusForTeam.isEmpty()){
			//create kanban status if team dont have
			kanbanStatusService.createStatusTodoAndDoneForTeam(this.currentTeam);
			//load again
			lKanbanStatusForTeam = kanbanStatusService.findKanbanStatusByTeamId(this.currentTeam.getTeamId());
		}
		statusList.addAll(new ArrayList<KanbanStatus>(lKanbanStatusForTeam));
		setKanbanStatusTempList(new ArrayList<KanbanStatus>(statusList));
		acceptedStatus = kanbanStatusService.findAcceptedStatusByTeamId(this.currentTeam.getTeamId());
		disappearTaskConfiguration = disappearTaskConfigurationService.getDisappearTaskConfigurationByKanbanStatusId(acceptedStatus.getStatusId());
		if(disappearTaskConfiguration == null){
			disappearTaskConfigurationService.createDisappearTaskConfigurationForAcceptedStatusOfTeam(acceptedStatus);
			disappearTaskConfiguration = disappearTaskConfigurationService.getDisappearTaskConfigurationByKanbanStatusId(acceptedStatus.getStatusId());
		}
	}

	private void initKanbanSwimline() {
		List<KanbanSwimline> lKanbanSwimlineForTeam = this.kanbanSwimlineService.findKanbanSwimlineByTeamId(this.currentTeam.getTeamId());
		if(lKanbanSwimlineForTeam.isEmpty()){
			//create if team dont have
			kanbanSwimlineService.createSwimlineForNewTeam(this.currentTeam);
			//load again
			lKanbanSwimlineForTeam = this.kanbanSwimlineService.findKanbanSwimlineByTeamId(this.currentTeam.getTeamId());
		}
		setKanbanSwimlines(lKanbanSwimlineForTeam); 
		setKanbanSwimlinesTemp(new ArrayList<KanbanSwimline>(getKanbanSwimlines()));
		//check isempty
		if(!this.kanbanSwimlines.isEmpty()){
			this.firstSwimline = this.kanbanSwimlines.get(0).getSwimlineId();
			this.firstSwimlineObject = this.kanbanSwimlines.get(0);
		}
	}
	
	public boolean checkUserStoryInProgressByKanbanIssues(List<KanbanIssue> kanbanIssues){
		for(KanbanIssue kanbanIssueItem : kanbanIssues){
			if(kanbanIssueItem.getKanbanStatus() != null && kanbanIssueItem.getKanbanSwimline() != null){
				return true;
			}
		}
		return false;
	}
	
	public void initSaveInfoAlert(){
		if (this.notAddedAttachmentList == null) {
			setNotAddedAttachmentList(new ArrayList<Attachment>());
		}else{
			this.notAddedAttachmentList.clear();
		}
		this.setSaveSuccess(false);
	}
	
	public int calculateUSProgress(UserStory userStory){
		return kanbanIssueService.calculateProgressUSForKanbanissue(userStory);
	}
	
	public int calculateProgressInit(KanbanIssue kanbanIssue) {
		return kanbanIssueService.calculateProgress(kanbanIssue);
	}
	
	public List<Attachment> getAttachmentsOfEditedUserStory(){
		this.editAttachmentList = attachmentService.findAttachmentByUserStory(this.editUserStory);
		return this.editAttachmentList;
	}
	
	/**
	 * Upload attachment file for user story
	 */
	public void uploadFile() {
		String filename = JSFUtils.getRequestParameter("filename");
		String diskFileName = this.attachmentService.fileNameProcess(FilenameUtils.removeExtension(filename));
		diskFileName = this.attachmentService.replaceFile(filename, diskFileName);
		this.attachment = new Attachment();
		this.attachment.setFilename(filename);
		this.attachment.setDiskFilename(diskFileName);
		this.attachment.setContainerId(this.editUserStory.getUserStoryId());
		this.attachment.setContainerType(Attachment.USERSTORY_ATTACHMENT);
		this.attachment.setTemp(false);
		this.attachment.setCreatedOn(new Date());
		this.attachment.setAuthor(this.utils.getLoggedInMember());
		this.attachmentService.save(this.attachment);
		this.editAttachmentList = attachmentService.findAttachmentByUserStory(this.editUserStory);
	}
	
	//F. Kanban/Add US 
	public void saveUserstory(){
		String descriptionUserStory = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("descriptionBackLog");
		this.addUserStory.setDescription(descriptionUserStory);
		this.addUserStory.setProject(this.projectService.findProjectById(this.currentProjectId));
		this.addUserStory.setPriority(UserStory.PriorityType.NONE);
		
		//Remove Control Characters
		this.addUserStory.setName(this.addUserStory.getName().replaceAll("\\p{Cntrl}", ""));
		this.addUserStory.setDescription(this.addUserStory.getDescription().replaceAll("\\p{Cntrl}", ""));
		
		if(this.userStoryService.create(this.addUserStory)){
			loadUserStoryOfCurrentProject();
			saveAttachmentsToDatabase();
			moveAttachmentFileToTemp();
			//reset userstory
			this.addUserStory.setName("");
			this.addUserStory.setDescription("");
			this.addUserStory.setValue(0);
			this.notAddedAttachmentList.clear();
			//hide form add backlog
			//this.showFormAddUserStory = false;
			//show msg
			this.setSaveSuccess(true);
		}
	}
	
	private void saveAttachmentsToDatabase() {
		try{
			for(Attachment attachment: this.notAddedAttachmentList){
				attachment.setContainerId(this.addUserStory.getUserStoryId());
				attachment.setTemp(false);
				this.attachmentService.save(attachment);
			}
		}catch(Exception e){
			logger.error("saveAttachmentsToDatabase: " + e );
		}
	}
	
	
	//TODO Knight check to use or delete 
	/*private void updateAttachmentsToDatabase() 
	{
		boolean isExistedOnDB = false;
		try{
			this.dBAttachmentList = this.attachmentService.findAttachmentByUserStory(this.editUserStory);
			//Add more US to DB
			for(Attachment attachment: this.notAddedAttachmentList){
				//update attachment files
				if(this.dBAttachmentList != null){
					for(Attachment attachmentEditUS: this.dBAttachmentList){
						if(attachmentEditUS.getDiskFilename().equalsIgnoreCase(attachment.getDiskFilename())){
							isExistedOnDB = true;
							break;
						}
						else {
							isExistedOnDB = false;
						}
					}
					if(!isExistedOnDB){
						attachment.setContainerId(this.editUserStory.getUserStoryId());
						attachment.setTemp(false);
						this.attachmentService.save(attachment);
					}
				}
				else{
					attachment.setContainerId(this.editUserStory.getUserStoryId());
					attachment.setTemp(false);
					this.attachmentService.save(attachment);
				}
			}
			for(Attachment attachmentEditUS: this.dBAttachmentList){
				boolean isExistOnNotAddedAttachment = false;
				for(Attachment attachment: this.notAddedAttachmentList){
					if(attachmentEditUS.getDiskFilename().equalsIgnoreCase(attachment.getDiskFilename())){
						isExistOnNotAddedAttachment = true;
						break;
					}
				}
				if(isExistOnNotAddedAttachment==false){
					this.attachmentService.delete(attachmentEditUS);
					this.attachmentService.deleteFile(attachmentEditUS.getDiskFilename(),this.editUserStory.getProject().getProjectId());
				}
				
			}
			//Delete US out of DB
			if(this.notAddedAttachmentList.size() == 0){
				for(Attachment attachmentEditUS: this.dBAttachmentList){
					this.attachmentService.delete(attachmentEditUS);
					this.attachmentService.deleteFile(attachmentEditUS.getDiskFilename(),this.editUserStory.getProject().getProjectId());
				}
			}
		}catch(Exception e){
			logger.error("saveAttachmentsToDatabase: " + e );
		}
	}*/
	
	private void moveAttachmentFileToTemp(){
		try{
			for(Attachment attachment: this.notAddedAttachmentList){
				this.attachmentService.moveAttachmentFile(attachment, this.addUserStory.getProject().getProjectId());
			}
		}catch(Exception e){
			logger.error("moveAttachmentFileToTemp: " + e );
		}
	}
	
	//F. Kanban/Add Task 
	public void addNewTaskInUserStory(){
		
		//get description and note from client
		String descriptionTask = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("descriptionTask");
		String noteTask = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("noteTask");
		String idKanbanBackLog = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idKanbanBackLog");
		
		this.editUserStory = this.userStoryService.findUserStoryById(this.idEditUserStory);
		
		//Remove Control Characters
		this.addTask.setSubject(this.addTask.getSubject().replaceAll("\\p{Cntrl}", ""));
		this.addTask.setDescription(descriptionTask.replaceAll("\\p{Cntrl}", ""));
		this.addTask.setNote(noteTask.replaceAll("\\p{Cntrl}", ""));
		
		this.addTask.setUserStory(editUserStory);
		this.addTask.setColumnDone(false);
		this.addTask.setRemain("D0T0");
		this.addTask.setEstimate("D0T0");
		this.addTask.setPointFormat("1");
		this.addTask.setType("Task");
		this.addTask.setTeam(currentTeam);
		this.addTask.setIsSubIssue(true);
	    if(kanbanIssueService.saveKanbanIssue(addTask)){
	    	kanbanHistoryService.historyForAddKanbanIssue(addTask);
	    }
	    Sprint lastSprintOfTeam = sprintService.findLastSprintByTeamId(currentTeamId);
	    Date nowDate = new Date();
	    if(lastSprintOfTeam != null && lastSprintOfTeam.getDateEnd().compareTo(nowDate) >= 0) {
	    	Issue issueParent = issueService.findIssueByUserStoryAndSprint (addTask.getUserStory().getUserStoryId(), lastSprintOfTeam.getSprintId());
	    	if(issueParent != null) {
	    		List<Issue> childIssues = issueService.findIssueByParent(issueParent);
	    		boolean existSubjectIssue = false;
	    		for(Issue childItem : childIssues) {
	    			if(childItem.getSubject().equals(addTask.getSubject())){
	    				existSubjectIssue = true;
	    				break;
	    			}
	    		}
	    		
	    		if(!existSubjectIssue) {
	    			//add new issue in last sprint
	    			Issue newIssueForLastSprint = new Issue();
	    			newIssueForLastSprint.setSubject(addTask.getSubject());
	    			newIssueForLastSprint.setDescription(addTask.getDescription());
	    			newIssueForLastSprint.setNote(addTask.getNote());
	    			newIssueForLastSprint.setParent(issueParent);
	    			newIssueForLastSprint.setEstimate("D0T0");
	    			newIssueForLastSprint.setPriority("NONE");
	    			newIssueForLastSprint.setRemain("D0T0");
	    			newIssueForLastSprint.setPointFormat("1");
	    			newIssueForLastSprint.setType("Task");
	    			newIssueForLastSprint.setSprint(lastSprintOfTeam);
	    			newIssueForLastSprint.setStatus(statusService.findStatusStartBySprintId(lastSprintOfTeam.getSprintId()));
	    			newIssueForLastSprint.setCreatedDate(new Date());
	    			issueService.saveIssue(newIssueForLastSprint);
	    			pointRemainService.save(newIssueForLastSprint, "D0T0");
	    			//update kanbanissue id reference
	    			addTask.setIssueOfLastSprint(newIssueForLastSprint.getIssueId());
	    			kanbanIssueService.updateKanbanIssue(addTask);
	    		}
	    	}
	    }
	    loadUserStoryOfCurrentProject();
	    //add callback param
	    RequestContext.getCurrentInstance().addCallbackParam("idUpdate",idKanbanBackLog);
	}
	
	//F. Kanban/Edit Task
	public void editTaskInUserStory(){
		//get description and note from client
		String subjectTask = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("subjectTask");
		String descriptionTask = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("descriptionTask");
		String noteTask = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("noteTask");
		String idKanbanBackLog = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idKanbanBackLog");

		//Remove Control Characters
		this.editTask.setSubject(subjectTask.replaceAll("\\p{Cntrl}", ""));
		this.editTask.setDescription(descriptionTask.replaceAll("\\p{Cntrl}", ""));
		this.editTask.setNote(noteTask.replaceAll("\\p{Cntrl}", ""));
		KanbanIssue kanbanIssueBeforeUpdate = kanbanIssueService.findKanbanIssueById(editTask.getIssueId());
		if(kanbanIssueService.updateKanbanIssue(editTask)){
			//add history for edit kanban issue
			kanbanHistoryService.historyForEditKanbanIssue(kanbanIssueBeforeUpdate,editTask,null);
			 //update issue reference kanban issue
			if(editTask.getIssueOfLastSprint() != null) {
				Issue issueFromId = issueService.findIssueById(editTask.getIssueOfLastSprint());
				if(issueFromId != null) {
					issueFromId.setSubject(editTask.getSubject());
					issueFromId.setDescription(editTask.getDescription());
					issueFromId.setNote(editTask.getNote());
					issueService.updateIssue(issueFromId);
				}
			}
			 loadUserStoryOfCurrentProject();
			 //add callback param
			 RequestContext.getCurrentInstance().addCallbackParam("idUpdate",idKanbanBackLog);
		}
	}
	
	//F. Kanban/Edit US 
	public void updateUserstory(){
		String descriptionEditUserStory = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("descriptionBackLog");
		this.editUserStory.setDescription(descriptionEditUserStory);
		this.editUserStory.setProject(projectService.findProjectById(this.currentProjectId));
		
		
		//Remove Control Characters
		this.editUserStory.setName(this.editUserStory.getName().replaceAll("\\p{Cntrl}", ""));
		this.editUserStory.setDescription(this.editUserStory.getDescription().replaceAll("\\p{Cntrl}", ""));
		
		if(this.userStoryService.update(this.editUserStory)){
			loadUserStoryOfCurrentProject();
			//reset userstory
			this.showFormEditUserStory = false;
			//show msg
			this.setUpdateSuccess(true);
		}
	}
	
	
	
	
	
	public void deleteUserstory(){
//		if(this.userStoryService.delete(this.idDeleteUserStory)){
//			//load userstory again
//			this.userStoriesOfProject = this.userStoryService.findAllUserStoryToDoAndProgress(this.currentProjectId);
//		}
		if(kanbanIssueService.findKanbanIssuesByUserStory(this.idDeleteUserStory).size() > 0){
			   	deleteTaskOfUserStory(this.idDeleteUserStory);
				userStoryService.delete(this.idDeleteUserStory);
				//update kanban view
				RequestContext.getCurrentInstance().update("kanban-view");
		} else {
				userStoryService.delete(this.idDeleteUserStory);
		}
		loadUserStoryOfCurrentProject();
	}
	
	public void loadEditUserStory(){
		this.editUserStory = this.userStoryService.findUserStoryById(this.idEditUserStory);
		this.editAttachmentList = this.attachmentService.findAttachmentByUserStory(this.editUserStory);
		this.setUpdateSuccess(false);
	}
	
	public void loadAddTask(){
		this.editUserStory = this.userStoryService.findUserStoryById(this.idEditUserStory);
		this.editTask = this.kanbanIssueService.findKanbanIssueById(this.idEditTask);
		UserStory tmp = this.editUserStory;
		this.editAttachmentList = this.attachmentService.findAttachmentByUserStory(tmp);
	}
	
	public void deleteTaskOfUserStory(long userSotryId){
		
		List<KanbanIssue> issues = kanbanIssueService.findKanbanIssuesByUserStory(userSotryId);
		for (KanbanIssue kanbanIssue : issues) {
		
				memberIssueService.deleteAllByIssue(kanbanIssue.getIssueId());
				kanbanIssueService.deleteKanbanIssue(kanbanIssue);
			
			
		}
		
	
	}
	
	public void handleChangeTeam(){
		try {
				this.listTeamMembers = this.memberService.findDevelopeMembersByTeam(this.teamService.findTeamByTeamId(currentTeamId));
				this.currentTeam = teamService.findTeamByTeamId(currentTeamId);
				this.setStatusList(new ArrayList<KanbanStatus>(kanbanStatusService.findKanbanStatusByTeamId(this.currentTeam.getTeamId())));
				this.setKanbanSwimlines(new ArrayList<KanbanSwimline>(this.kanbanSwimlineService.findKanbanSwimlineByTeamId(this.currentTeam.getTeamId())));
				
				if(getStatusList().isEmpty()){
					kanbanStatusService.createStatusTodoAndDoneForTeam(currentTeam);
				}
				
				if(getKanbanSwimlines().isEmpty()){
					kanbanSwimlineService.createSwimlineForNewTeam(currentTeam);
				}
				
				setStatusList(new ArrayList<KanbanStatus>(kanbanStatusService.findKanbanStatusByTeamId(this.currentTeam.getTeamId())));
				setKanbanStatusTempList(new ArrayList<KanbanStatus>(getStatusList()));
				
				setKanbanSwimlines(new ArrayList<KanbanSwimline>(this.kanbanSwimlineService.findKanbanSwimlineByTeamId(this.currentTeam.getTeamId())));
				setKanbanSwimlinesTemp(new ArrayList<KanbanSwimline>(getKanbanSwimlines()));
				
				this.firstSwimline = this.kanbanSwimlines.get(0).getSwimlineId();
				this.firstSwimlineObject = this.kanbanSwimlines.get(0);
				//end load view
			
				//load project from team
				this.listProject = this.projectService.findAllProjectsByTeam(this.teamService.findTeamByTeamId(this.currentTeam.getTeamId()));
				if(!this.listProject.isEmpty()){
					this.currentProjectId = this.listProject.get(0).getProjectId();
					loadUserStoryOfCurrentProject();
				}else{
					this.listProject = this.projectService.findByOwnerId(this.utils.getLoggedInMember().getMemberId());
					if(!listProject.isEmpty()){
						this.currentProjectId = this.listProject.get(0).getProjectId();
						loadUserStoryOfCurrentProject();
					}
				}
				
				acceptedStatus = kanbanStatusService.findAcceptedStatusByTeamId(currentTeam.getTeamId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void handleChangeProject(){
		defaultStateLoadingUserProjects();
		loadUserStoryOfCurrentProject();
		
	}
	
	public void validateUserStory(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		if (this.userStoryService.checkExistUserStory(Utils.standardizeString(value.toString()), this.currentProjectId) != null) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.Exists", null));
			throw new ValidatorException(msg);
		}
	}
	
	public void validateUserStoryForEdit(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		if (this.userStoryService.checkUserStoryForEdit(Utils.standardizeString(value.toString()), this.currentProjectId, this.editUserStory.getUserStoryId()) != null) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.Exists", null));
			throw new ValidatorException(msg);
		}
	}
	
	public void validateEditTask(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		if(this.kanbanIssueService.checkDuplicate(Utils.standardizeString(value.toString()), kanbanIssueService.findKanbanIssueById(this.idEditTask).getUserStory().getUserStoryId(), this.idEditTask)!= null){
			FacesMessage msg = new FacesMessage("Task name already exist", null);
			throw new ValidatorException(msg);
		}
	}
	
	public void validateAddTask(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		if(this.kanbanIssueService.checkDuplicate(Utils.standardizeString(value.toString()), this.editUserStory.getUserStoryId(), -1)!= null){
//			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage("Task name already exist", null);
			throw new ValidatorException(msg);
		}
	}
	
	public void validateValue(FacesContext context, UIComponent validate, Object value) throws ValidatorException {

		if (!utils.isValueOfRange(value.toString(), 0, MAX_BUSSINESS_VALUE)) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(utils.getMessage("myagile.backlog.RangeOfValue", null));
			throw new ValidatorException(msg);
		}
	}
	
	public void deleteTask(){
		
		//delete member kanbanissue
		memberIssueService.deleteAllByIssue(this.idDeleteTask);
		//delete kanban issue
		KanbanIssue kanbanIssueDelete = kanbanIssueService.findKanbanIssueById(this.idDeleteTask);
		kanbanIssueService.deleteKanbanIssue(kanbanIssueDelete);
		//add history for delete kanban issue
		kanbanHistoryService.historyForDeleteKanbanIssue(kanbanIssueDelete);
		
		if(kanbanIssueDelete.getIssueOfLastSprint() != null) {
			//delete issue reference kanban issue
			Issue issueFromId = issueService.findIssueById(kanbanIssueDelete.getIssueOfLastSprint());
			if(issueFromId != null){
				issueService.deleteIssue(issueFromId);
			}
		}
		loadUserStoryOfCurrentProject();
	}
	public void handleAddField(){
		KanbanStatus newField= new KanbanStatus();
		long idStatusTemp = -getKanbanStatusTempList().size();
		newField.setStatusId(idStatusTemp);//set id teamp for easy remove
		newField.setTeam(this.currentTeam);
		newField.setType(StatusType.IN_PROGRESS);
		newField.setWeightPoint(KanbanStatus.DEFAULT_WEIGHT_IN_PROCESS);
		newField.setWidth(10);
		newField.setColumnDone(false);
		newField.setName("");
		newField.setColor(KanbanStatus.DEFAULT_COLOR);
		kanbanStatusTempList.add(getKanbanStatusTempList().size()-2,newField);
	}
	public void handleAddSwimline(){
		KanbanSwimline swimline= new KanbanSwimline();
		long idSwimlineTemp = -kanbanSwimlinesTemp.size();
		swimline.setSwimlineId(idSwimlineTemp);
		swimline.setColor(KanbanSwimline.DEFAULT_COLOR);
		swimline.setTeam(this.currentTeam);
		swimline.setName("");
		kanbanSwimlinesTemp.add(swimline);
	}
	
	public List<KanbanIssue> getIssueFromKanbanStatusAndSwimline(long idTeam,long idKanbanStatus,long idKanbanSwimline){
		List<KanbanIssue> lissue = new ArrayList<KanbanIssue>();
		if(acceptedStatus.getStatusId().compareTo(kanbanStatusService.getById(idKanbanStatus).getStatusId()) == 0){
			lissue = kanbanStatusService.getAcceptedIssueNotDisappear(idTeam,idKanbanStatus, idKanbanSwimline);
		}else{
			lissue =  this.kanbanStatusService.getAllIssueByKanbanStatus(idTeam,idKanbanStatus, idKanbanSwimline);
		}
		return lissue;
	}
	
	public List<KanbanIssue> getIssueChildProgressFromKanbanStatusAndSwimline(long idTeam,long idKanbanStatus,long idKanbanSwimline){
		List<KanbanIssue> lissue =  this.kanbanStatusService.getAllIssueChildProgressByKanbanStatus(idTeam,idKanbanStatus, idKanbanSwimline);
		return lissue;
	}
	
	public List<KanbanIssue> getIssueChildDoneFromKanbanStatusAndSwimline(long idTeam,long idKanbanStatus,long idKanbanSwimline){
		List<KanbanIssue> lissue =  this.kanbanStatusService.getAllIssueChildDoneByKanbanStatus(idTeam,idKanbanStatus, idKanbanSwimline);
		return lissue;
	}
	
	public void handleCloseSettingForm() {
		//reset kanban status and swimline temp list
		if(this.kanbanStatusTempList != null && !this.kanbanStatusTempList.isEmpty()){
			kanbanStatusTempList.clear();
			//init again
			setKanbanStatusTempList(new ArrayList<KanbanStatus>(statusList));
		}
		if(this.kanbanSwimlinesTemp != null && !this.kanbanSwimlinesTemp.isEmpty()) {
			this.kanbanSwimlinesTemp.clear();
			setKanbanSwimlinesTemp(new ArrayList<KanbanSwimline>(getKanbanSwimlines()));
		}
	}
	
	public void handleSaveSettingForm() {
	try {
			for (KanbanStatus st : statusList) {
				if (!kanbanStatusTempList.contains(st)) {
					kanbanStatusService.updateIssueByTeamId(this.currentTeam.getTeamId(), st.getStatusId());
					kanbanStatusService.delete(st.getStatusId());
				}
			}
			for (KanbanStatus st : kanbanStatusTempList) {
				//create new kanban status when id lessthan 0
				if (st.getStatusId() == null || st.getStatusId().compareTo(0L) < 0) {
					kanbanStatusService.add(st);
				} else {
					kanbanStatusService.update(st);
				}
			}
			
			disappearTaskConfigurationService.update(disappearTaskConfiguration);
			
			for (KanbanSwimline swimline : kanbanSwimlines) {
				if (!kanbanSwimlinesTemp.contains(swimline)) {
					kanbanSwimlineService.updateIssueByTeamId(this.currentTeam.getTeamId(), swimline.getSwimlineId());
					kanbanSwimlineService.delete(swimline.getSwimlineId());
				}
			}
			for (KanbanSwimline st : kanbanSwimlinesTemp) {
				if (st.getSwimlineId() == null || st.getSwimlineId().compareTo(0L) < 0) {
					kanbanSwimlineService.add(st);
				} else {
					kanbanSwimlineService.update(st);
				}
			}
			
			setKanbanSwimlines(this.kanbanSwimlineService.findKanbanSwimlineByTeamId(this.currentTeam.getTeamId())); 
			setKanbanSwimlinesTemp(new ArrayList<KanbanSwimline>(getKanbanSwimlines()));
			firstSwimlineObject = this.getKanbanSwimlines().get(0);
			
			setStatusList(new ArrayList<KanbanStatus>(kanbanStatusService.findKanbanStatusByTeamId(this.currentTeam.getTeamId())));
			setKanbanStatusTempList(new ArrayList<KanbanStatus>(statusList));
			//setIssueList(issueService.findIssuesIsTaskBySprintId(currentSprint.getSprintId()));
			RequestContext.getCurrentInstance().addCallbackParam("save", true);
			//pushDataKanban(-1L, "true", "");
		} catch (Exception e) {
			RequestContext.getCurrentInstance().addCallbackParam("save", false);
			e.printStackTrace();
		}
	}
	public void refreshStatusTempList(){
		setKanbanStatusTempList(new ArrayList<KanbanStatus>(statusList));
	}
	
	public boolean getShowAcceptedStatus() {
		if (statusList.size() >= 3) {
			KanbanStatus acceptedStatus = statusList.get(statusList.size() - 1);
			if ((acceptedStatus != null) && (acceptedStatus.getType() == StatusType.ACCEPTED_SHOW)) {
				return true;
			}
		}
		return false;
	}
	public void setShowAcceptedStatus(boolean isShow) {
		if (statusList.size() > 0) {
			KanbanStatus acceptedStatus = statusList.get(statusList.size() - 1);
			if (isShow) {
				acceptedStatus.setType(KanbanStatus.StatusType.ACCEPTED_SHOW);
			} else {
				acceptedStatus.setType(KanbanStatus.StatusType.ACCEPTED_HIDE);
			}
		}
	}
	
	
	public void removeStatus() {
		String statusId = JSFUtils.getRequestParameter("statusId");
		try {
			if(!statusId.isEmpty()) {
				Long stId = Long.parseLong(statusId);
				Iterator<KanbanStatus> statusListVal = kanbanStatusTempList.iterator();
				while (statusListVal.hasNext()) {
					KanbanStatus st = statusListVal.next();
					if (stId.equals(st.getStatusId())) {
						statusListVal.remove();
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public void removeSwimline(){
		String swimlineId = JSFUtils.getRequestParameter("swimlineId");
		try {
			Long swimlineRemoteId = Long.parseLong(swimlineId);
			Iterator<KanbanSwimline> swimlineTemp = kanbanSwimlinesTemp.iterator();
			while (swimlineTemp.hasNext()) {
				KanbanSwimline st = swimlineTemp.next();
				if (swimlineRemoteId.equals(st.getSwimlineId())) {
					swimlineTemp.remove();
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//delete issue when drop in bl
	@SuppressWarnings("unused")
	public void deleteIssueDropInBL(){
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			Map<String, String> reqParams = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap();
			
			long idKanbanIssue = Long.parseLong(reqParams.get("idKanbanIssue"));
			
			KanbanIssue issueNeedDelete = this.kanbanIssueService.findKanbanIssueById(idKanbanIssue);
			UserStory userStoryOfKanbanIssue = issueNeedDelete.getUserStory();
			
			//delete member issue before delete kanban issue
			if(issueNeedDelete.getIsSubIssue() == false){
				memberIssueService.deleteAllByIssue(issueNeedDelete.getIssueId());
				this.kanbanIssueService.deleteKanbanIssue(issueNeedDelete);
				//add history for delete kanban issue
				kanbanHistoryService.historyForDeleteKanbanIssue(issueNeedDelete);
			}else{
				issueNeedDelete.setKanbanSwimline(null);
				issueNeedDelete.setColumnDone(false);
				issueNeedDelete.setKanbanStatus(null);
				issueNeedDelete.setIsSubIssue(true);
				KanbanIssue kanbanIssueBeforeUpdate = kanbanIssueService.findKanbanIssueById(issueNeedDelete.getIssueId());
				kanbanIssueService.updateKanbanIssue(issueNeedDelete);
				//add history for delete kanban issue
				kanbanHistoryService.historyForEditKanbanIssue(kanbanIssueBeforeUpdate, issueNeedDelete,null);
			}

			List<Issue> issues = issueService.findIssuesByUserStory(userStoryOfKanbanIssue.getUserStoryId());
			if(issues == null || issues.isEmpty()){
				List<KanbanIssue> listKanbanIssuesInprogress = kanbanIssueService.getKanbanIssueProgressByUserStory(userStoryOfKanbanIssue);
				if(listKanbanIssuesInprogress == null || listKanbanIssuesInprogress.isEmpty()) {
					userStoryOfKanbanIssue.setStatus(UserStory.StatusType.TODO);
				}
			}
			if(this.kanbanIssueService.checkAllKanbanIssueDoneOfUserstory(userStoryOfKanbanIssue.getUserStoryId())){
				userStoryOfKanbanIssue.setStatus(UserStory.StatusType.DONE);
			}
			
			
			if(userStoryService.update(userStoryOfKanbanIssue)){
				loadUserStoryOfCurrentProject();
			}
		} catch (Exception e) {
			logger.error("can not delete kanban issue",e);
		}
	}
	
	@SuppressWarnings("unused")
	public void saveDragAndDropTask()
	{
		try {
			boolean moveTaskFromDoneOrAccept = false;
			FacesContext context = FacesContext.getCurrentInstance();
			Map<String, String> reqParams = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap();
			
			long idKanbanIssue = Long.parseLong(reqParams.get("idKanbanIssue"));
			long idKanbanStatus = Long.parseLong(reqParams.get("idKanbanStatus"));
			long idKanbanSwimline = Long.parseLong(reqParams.get("idKanbanSwimline"));
			String idContainer = reqParams.get("idContainer");
			boolean isChildDone = Boolean.parseBoolean(reqParams.get("isChildDone"));
			String idColumnKanbanBackLog = reqParams.get("idColumnKanbanBackLog");
			String idColumnSender = reqParams.get("idColumnSender");
			int dataWIP = Integer.parseInt(reqParams.get("dataWIP"));
			//all drop task when all task in cell less than or equal wip
			boolean acceptDrop = true;
			
			//update issue
			KanbanIssue updateKanbanIssue = this.kanbanIssueService.findKanbanIssueById(idKanbanIssue);
			if(updateKanbanIssue.getKanbanStatus() != null && (updateKanbanIssue.getKanbanStatus().getType() == KanbanStatus.StatusType.DONE || updateKanbanIssue.getKanbanStatus().getType() == KanbanStatus.StatusType.ACCEPTED_SHOW)){
				moveTaskFromDoneOrAccept = true;
			}
			
			
			//set new status
			KanbanStatus kanbanStatus = this.kanbanStatusService.getById(idKanbanStatus);
			updateKanbanIssue.setKanbanStatus(kanbanStatus);
			//set new swimline
			updateKanbanIssue.setKanbanSwimline(this.kanbanSwimlineService.getById(idKanbanSwimline));
			updateKanbanIssue.setTeam(this.currentTeam);
			//set new child done
			updateKanbanIssue.setColumnDone(isChildDone);
			
			//check overload wip
			if(!kanbanStatus.getType().equals(KanbanStatus.StatusType.DONE) 
					&& !kanbanStatus.getType().equals(KanbanStatus.StatusType.ACCEPTED_SHOW) 
					&& !kanbanStatus.getType().equals(KanbanStatus.StatusType.ACCEPTED_HIDE)){
				List<KanbanIssue> kanbanIssuesFromCell = this.kanbanStatusService.getAllIssueByKanbanStatus( this.currentTeamId,idKanbanStatus, idKanbanSwimline);
				if(dataWIP <= kanbanIssuesFromCell.size() && !kanbanIssuesFromCell.contains(updateKanbanIssue)){
					acceptDrop = false;
				}
			}
			
			if(acceptDrop){
				if(kanbanStatus.getType() == KanbanStatus.StatusType.DONE || kanbanStatus.getType() == KanbanStatus.StatusType.ACCEPTED_SHOW)
				{
					updateKanbanIssue.setRemain("D0T0");
					if(kanbanStatus.getStatusId().compareTo(acceptedStatus.getStatusId())==0)
					{
					    updateKanbanIssue.setDisappearDate(Utils.converDateToString(Utils.addDateToParticularDate(new Date(), disappearTaskConfiguration.getWeek()*NUMBER_DATE_OF_WEEK)));
					}
					
					KanbanIssue kanbanIssueBeforeUpdate = kanbanIssueService.findKanbanIssueById(updateKanbanIssue.getIssueId());
					
					if(this.kanbanIssueService.updateKanbanIssue(updateKanbanIssue))
					{
						//history for update kanban issue
						kanbanHistoryService.historyForEditKanbanIssue(kanbanIssueBeforeUpdate, updateKanbanIssue,null);
						//check us is done
						if(kanbanIssueService.checkAllKanbanIssueDoneOfUserstory(updateKanbanIssue.getUserStory().getUserStoryId())){
							//update status us done
							UserStory userStoryDone = updateKanbanIssue.getUserStory();
							userStoryDone.setStatus(UserStory.StatusType.DONE);
							if(userStoryService.update(userStoryDone)){
								//update all issue last sprint in scrum board
								this.issueService.updateAllIssueByStatusUserStoryInLastSprint(userStoryDone,this.currentTeamId);
							}
						}
						
						loadUserStoryOfCurrentProject();
						//update product backlog column
						RequestContext.getCurrentInstance().update(idRenderUserstoryOfProject);
					}
					
				}else{
					updateKanbanIssue.setDisappearDate(null);
					KanbanIssue kanbanIssueBeforeUpdate = kanbanIssueService.findKanbanIssueById(updateKanbanIssue.getIssueId());
					
					if(this.kanbanIssueService.updateKanbanIssue(updateKanbanIssue))
					{
						//history for update kanban issue
						kanbanHistoryService.historyForEditKanbanIssue(kanbanIssueBeforeUpdate, updateKanbanIssue,null);
						//update userstory 
						UserStory userStoryProgress = updateKanbanIssue.getUserStory();
						userStoryProgress.setStatus(UserStory.StatusType.IN_PROGRESS);
						if(userStoryService.update(userStoryProgress)){
							//update all issue last sprint in scrum board
							this.issueService.updateAllIssueByStatusUserStoryInLastSprint(userStoryProgress,this.currentTeamId);
							loadUserStoryOfCurrentProject();
						}
					}
					
					if(moveTaskFromDoneOrAccept)
					{
						RequestContext.getCurrentInstance().update(idRenderUserstoryOfProject);
					}
				}
			} else {
				//show message overload
				RequestContext.getCurrentInstance().execute("informOverload.show()");
				//update column sender
				RequestContext.getCurrentInstance().update(idColumnSender);
			}
			
			//update id product backlog when move task from product backlog column to kanban
			RequestContext.getCurrentInstance().addCallbackParam("idColumnKanbanBackLog", idColumnKanbanBackLog);
			//update cell
			RequestContext.getCurrentInstance().update(idContainer);
			//update script js
			RequestContext.getCurrentInstance().update(idScriptView);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("error move task in kanban",e);
		}
		
	}
	
	@SuppressWarnings("unused")
	public void saveDragAndDropBacklog()
	{
		try 
		{
			FacesContext context = FacesContext.getCurrentInstance();
			Map<String, String> reqParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
			
			long idBacklog = Long.parseLong(reqParams.get("idBacklog"));
			long idKanbanStatus = Long.parseLong(reqParams.get("idKanbanStatus"));
			long idKanbanSwimline = Long.parseLong(reqParams.get("idKanbanSwimline"));
			String idContainer = reqParams.get("idContainer");
			String idColumnKanbanBackLog = reqParams.get("idColumnKanbanBackLog");
			boolean isChildDone = Boolean.parseBoolean(reqParams.get("isChildDone"));
			int dataWIP = Integer.parseInt(reqParams.get("dataWIP"));
			boolean acceptDrop = true;
			KanbanStatus kanbanStatus = this.kanbanStatusService.getById(idKanbanStatus);
			
			if(!kanbanStatus.getType().equals(KanbanStatus.StatusType.DONE) 
					&& !kanbanStatus.getType().equals(KanbanStatus.StatusType.ACCEPTED_SHOW) 
					&& !kanbanStatus.getType().equals(KanbanStatus.StatusType.ACCEPTED_HIDE))
			{
				List<KanbanIssue> kanbanIssuesFromCell = this.kanbanStatusService.getAllIssueByKanbanStatus( this.currentTeamId,idKanbanStatus, idKanbanSwimline);
				if(dataWIP <= kanbanIssuesFromCell.size()){
					acceptDrop = false;
				}
			}
			
			if(acceptDrop)
			{
				//get userstory
				UserStory currentUserStory = this.userStoryService.findUserStoryById(idBacklog);
				//get all kanbanissue in us
				List<KanbanIssue> lKanbanIssueOfUs = this.kanbanIssueService.getListKanbanIssueNotNullByUserstory(idBacklog);
				
				if(lKanbanIssueOfUs.isEmpty())
				{
					//create new kanbanissue
					//create issue
					KanbanIssue createNewKanbanIssue = new KanbanIssue();
					createNewKanbanIssue.setSubject(currentUserStory.getName());
					createNewKanbanIssue.setType("Task");
					createNewKanbanIssue.setDescription(currentUserStory.getDescription());
					createNewKanbanIssue.setNote(currentUserStory.getNote());
					createNewKanbanIssue.setPriority("NONE");
					createNewKanbanIssue.setRemain("D0T0");
					createNewKanbanIssue.setPointFormat("1");
					createNewKanbanIssue.setEstimate("D0T0");
					createNewKanbanIssue.setUserStory(currentUserStory);
					createNewKanbanIssue.setTeam(this.currentTeam);
					createNewKanbanIssue.setKanbanStatus(kanbanStatus);
					createNewKanbanIssue.setKanbanSwimline(this.kanbanSwimlineService.getById(idKanbanSwimline));
					createNewKanbanIssue.setColumnDone(isChildDone);
					createNewKanbanIssue.setIsSubIssue(false);
					
					if(this.kanbanIssueService.saveKanbanIssue(createNewKanbanIssue))
					{
						currentUserStory.setStatus(UserStory.StatusType.IN_PROGRESS);
						if(kanbanStatus.getType() == KanbanStatus.StatusType.DONE || kanbanStatus.getType() == KanbanStatus.StatusType.ACCEPTED_SHOW)
						{
							currentUserStory.setStatus(UserStory.StatusType.DONE);
							//update us
							if(this.userStoryService.update(currentUserStory))
							{
								//update date point
								createNewKanbanIssue.setRemain("D0T0");
								if(this.kanbanIssueService.updateKanbanIssue(createNewKanbanIssue)){
									
								}
								//update product backlog column
								RequestContext.getCurrentInstance().update(idRenderUserstoryOfProject);
							}
						}
						//update userstory progress
						this.userStoryService.update(currentUserStory);
						//update all issue last sprint in scrum board
						this.issueService.updateAllIssueByStatusUserStoryInLastSprint(currentUserStory,this.currentTeamId);
					}
				}else{
					KanbanSwimline kanbanSwimline = this.kanbanSwimlineService.getById(idKanbanSwimline);
					Team teamIssue = this.currentTeam;
					List<KanbanIssue> lKanbanIssueNullInBackLog = this.kanbanIssueService.getListKanbanIssueByUserstory(idBacklog);
					
					for(int i = 0;i< lKanbanIssueNullInBackLog.size();i++)
					{
						lKanbanIssueNullInBackLog.get(i).setKanbanStatus(kanbanStatus);
						if(acceptedStatus.getStatusId().compareTo(kanbanStatus.getStatusId())==0)
						{
							lKanbanIssueNullInBackLog.get(i).setDisappearDate(Utils.converDateToString(Utils.addDateToParticularDate(new Date(), disappearTaskConfiguration.getWeek()*NUMBER_DATE_OF_WEEK)));
						}
						lKanbanIssueNullInBackLog.get(i).setKanbanSwimline(kanbanSwimline);
						lKanbanIssueNullInBackLog.get(i).setTeam(teamIssue);
						lKanbanIssueNullInBackLog.get(i).setColumnDone(isChildDone);
						this.kanbanIssueService.updateKanbanIssue(lKanbanIssueNullInBackLog.get(i));
					}
					
					if(kanbanStatus.getType() == KanbanStatus.StatusType.DONE || kanbanStatus.getType() == KanbanStatus.StatusType.ACCEPTED_SHOW)
					{
						//check us is done
						if(kanbanIssueService.checkAllKanbanIssueDoneOfUserstory(currentUserStory.getUserStoryId()))
						{
							//update status us done
							UserStory userStoryDone = currentUserStory;
							userStoryDone.setStatus(UserStory.StatusType.DONE);
							
							if(userStoryService.update(userStoryDone))
							{
								//update all issue last sprint in scrum board
								this.issueService.updateAllIssueByStatusUserStoryInLastSprint(currentUserStory,this.currentTeamId);
								//update product backlog column
								RequestContext.getCurrentInstance().update(idRenderUserstoryOfProject);
							}
						}
					}
					
				}
				
			} else {
				RequestContext.getCurrentInstance().execute("informOverload.show()");
			}
			
			loadUserStoryOfCurrentProject();
			
			//update cell
			RequestContext.getCurrentInstance().update(idContainer);
			//send param and update in javscript
			RequestContext.getCurrentInstance().addCallbackParam("idColumnKanbanBackLog", idColumnKanbanBackLog);
			
		} catch (Exception e) {
			e.printStackTrace();
			//update product backlog column
			RequestContext.getCurrentInstance().update(idRenderUserstoryOfProject);
		}
	}
	
	public String getUsertoryName(){
		return "ABC";
	}
	
	public void resetFormUserStory(){
		//reset userstory
		this.addUserStory.setName("");
		this.addUserStory.setDescription("");
		this.addUserStory.setValue(0);
	}
	/*------------------------------*/
	public void resetProductBacklogForm() {
		JSFUtils.resetForm("panel-edit-backlog-form");
		
	}
	
	
	
	public void loadTaskNeedEdit(){
		this.editTask = kanbanIssueService.findKanbanIssueById(this.idEditTask);

	}
	
	public List<KanbanIssue> getTaskInUserStory(long idUserStory){
		return this.kanbanIssueService.getListKanbanIssueByUserstory(idUserStory);
	}
	
	public void checkEstimatePoint(FacesContext context, UIComponent validate, Object value) {
		pointRemainService.checkEstimatePoint(context, validate, value);
	}
	
	public void updatePointRemain(long idKanbanIssue)
	{
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String remain= request.getParameter("hiddenPointRemain"+idKanbanIssue);
		remain = remain.trim();
		String[] tempRemain = remain.split("\\s");
		
		if(tempRemain.length == 1) 
		{
			remain = remain + " " + "0";
		}
		
		KanbanIssue issueNeedUpdate = this.kanbanIssueService.findKanbanIssueById(idKanbanIssue);
		String oldRemainPoint = issueNeedUpdate.getRemain();
		String pattern = "\\d{1,2}(\\.5)?(\\.0+)?\\s\\d{1,2}(\\.5)?(\\.0+)?";
		String pointRemainActual = remain.trim();
		
		if(pointRemainActual.matches(pattern))
		{
			String[] str = pointRemainActual.split("\\s");
			pointRemainActual = String.format("D%sT%s", str[0], str[1]);
		}
		
		issueNeedUpdate.setRemain(pointRemainActual);
		KanbanIssue kanbanIssueBeforeUpdate = kanbanIssueService.findKanbanIssueById(issueNeedUpdate.getIssueId());
		
		if(this.kanbanIssueService.updateKanbanIssue(issueNeedUpdate))
		{
			//update history when update remain point
			kanbanHistoryService.historyForRemainPointKanbanIssue(kanbanIssueBeforeUpdate, issueNeedUpdate);
			//add kanban kanban point remain
			if(!remain.equals(oldRemainPoint)){
				KanbanPointRemain pointRemain = new KanbanPointRemain();
				pointRemain.setDateUpdate(new Date());
				pointRemain.setPointRemain(oldRemainPoint);
				pointRemain.setKanbanissue(issueNeedUpdate);
				if(this.kanbanPointRemainService.save(pointRemain)){
					
				}
			}
			
		}
	}
	
	public void updateEstimatePoint(long idKanbanIssue)
	{
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String estimate= request.getParameter("hiddenPointEstimate"+idKanbanIssue);
		estimate = estimate.trim();
		String[] tempEstimate = estimate.split("\\s");
	
		if(tempEstimate.length == 1) 
		{
			estimate = estimate + " " + "0";
		}
		
		KanbanIssue issueNeedUpdate = this.kanbanIssueService.findKanbanIssueById(idKanbanIssue);
		String pattern = "\\d{1,2}(\\.5)?(\\.0+)?\\s\\d{1,2}(\\.5)?(\\.0+)?";
		String pointEstimateActual = estimate.trim();
		
		if(pointEstimateActual.matches(pattern))
		{
			String[] str = pointEstimateActual.split("\\s");
			pointEstimateActual = String.format("D%sT%s", str[0], str[1]);
		}
		
		issueNeedUpdate.setEstimate(pointEstimateActual);
		issueNeedUpdate.setRemain(pointEstimateActual);
		KanbanIssue kanbanIssueBeforeUpdate = kanbanIssueService.findKanbanIssueById(issueNeedUpdate.getIssueId());
		
		if(this.kanbanIssueService.updateKanbanIssue(issueNeedUpdate))
		{
			//add history when update kanban issue
			kanbanHistoryService.historyForEstimateKanbanIssue(kanbanIssueBeforeUpdate, issueNeedUpdate);
		}
	}
	
	public void goToViewKanbanIssue() 
	{
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/kanbanIssue/viewDetail" + "?kanbanIssueId=" + viewKanbanIssue.getIssueId() + "&userStory="+ viewKanbanIssue.getUserStory().getUserStoryId();
		JSFUtils.redirect(contextPath);
	}
	

	public List<Team> getListTeams() 
	{
	    excludeHiddenTeamsForSelection(teamService.findTeamsByMemberAndOwner(this.utils.getLoggedInMember()));

	    return listTeams;
	}
	
	//upload file
//	==========================================================
	
	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public List<Attachment> getNotAddedAttachmentList() {
		return notAddedAttachmentList;
	}

	public void setNotAddedAttachmentList(List<Attachment> notAddedAttachmentList) {
		this.notAddedAttachmentList = notAddedAttachmentList;
	}

	public Attachment getDeleteAttachment() {
		return deleteAttachment;
	}

	public void setDeleteAttachment(Attachment deleteAttachment) {
		this.deleteAttachment = deleteAttachment;
	}

	public void uploadAttachment() {
		if (addAttachment()) {
				this.notAddedAttachmentList.add(this.attachment);
			}
	}
	
	private boolean addAttachment() 
	{
		try{
        		this.attachment = new Attachment();
        			
        		String filename = JSFUtils.getRequestParameter("filename");
        		String diskFileName = this.attachmentService.fileNameProcess(FilenameUtils.removeExtension(filename));
        		diskFileName = this.attachmentService.replaceFile(filename, diskFileName);
        		this.attachment.setFilename(filename);
        		this.attachment.setDiskFilename(diskFileName);
        		this.attachment.setContainerType(Attachment.USERSTORY_ATTACHMENT);
        		this.attachment.setTemp(true);
        		this.attachment.setCreatedOn(new Date());
        		this.attachment.setAuthor(this.utils.getLoggedInMember());
        		return true;
		}
		catch(Exception exception)
		{
			logger.error("addAttachment at KanbanBacklog " + exception.getMessage());
			return false;
		}
	}
	
	public void deleteNotAddedAttachment() {
		try {
			this.attachmentService.deleteFileInTemp(deleteAttachment.getDiskFilename());
			this.notAddedAttachmentList.remove(deleteAttachment);
		} catch (Exception e) {
			logger.error("deleteAttachment at KanbanBacklog " + e);
		}
	}
	
	public void deleteAttachmentInDB() {
		try {
			this.attachmentService.delete(deleteAttachment);
			this.editAttachmentList.remove(deleteAttachment);
		} catch (Exception e) {
			logger.error("deleteAttachment in DB at KanbanBacklog " + e);
		}
	}
	//end public file


	public void setListTeams(List<Team> listTeams) 
	{
	    excludeHiddenTeamsForSelection(listTeams);
	}

	public List<Project> getListProject() {
		return listProject;
	}

	public void setListProject(List<Project> listProject) {
		this.listProject = listProject;
	}

	public Long getCurrentProjectId() {
		return currentProjectId;
	}

	public void setCurrentProjectId(Long currentProjectId) {
		this.currentProjectId = currentProjectId;
	}

	public List<Member> getListTeamMembers() {
		return listTeamMembers;
	}

	public void setListTeamMembers(List<Member> listTeamMembers) {
		this.listTeamMembers = listTeamMembers;
	}

	public List<KanbanStatus> getKanbanStatusTempList() {
		return kanbanStatusTempList;
	}

	public void setKanbanStatusTempList(List<KanbanStatus> kanbanStatusTempList) {
		this.kanbanStatusTempList = kanbanStatusTempList;
	}

	public UserStory getAddUserStory() {
		return addUserStory;
	}

	public void setAddUserStory(UserStory addUserStory) {
		this.addUserStory = addUserStory;
	}

	public String getEstimateInAddBackLog() {
		return estimateInAddBackLog;
	}

	public void setEstimateInAddBackLog(String estimateInAddBackLog) {
		this.estimateInAddBackLog = estimateInAddBackLog;
	}

	public List<UserStory> getUserStoriesOfProject() {
		return userStoriesOfProject;
	}

	public void setUserStoriesOfProject(List<UserStory> userStoriesOfProject) {
		this.userStoriesOfProject = userStoriesOfProject;
	}

	public Long getIdDeleteUserStory() {
		return idDeleteUserStory;
	}

	public void setIdDeleteUserStory(Long idDeleteUserStory) {
		this.idDeleteUserStory = idDeleteUserStory;
	}

	public String getNameDeleteUserStory() {
		return nameDeleteUserStory;
	}

	public void setNameDeleteUserStory(String nameDeleteUserStory) {
		this.nameDeleteUserStory = nameDeleteUserStory;
	}

	public boolean isShowFormEditUserStory() {
		return showFormEditUserStory;
	}

	public KanbanIssue getAddTask() {
		return addTask;
	}

	public void setAddTask(KanbanIssue addTask) {
		this.addTask = addTask;
	}

	public void setShowFormEditUserStory(boolean showFormEditUserStory) {
		this.showFormEditUserStory = showFormEditUserStory;
	}

	public Long getIdEditUserStory() {
		return idEditUserStory;
	}

	public void setIdEditUserStory(Long idEditUserStory) {
		this.idEditUserStory = idEditUserStory;
	}

	public UserStory getEditUserStory() {
		return editUserStory;
	}

	public void setEditUserStory(UserStory editUserStory) {
		this.editUserStory = editUserStory;
	}
	
	public List<Attachment> getEditAttachmentList() {
		return editAttachmentList;
	}

	public void setEditAttachmentList(List<Attachment> editAttachmentList) {
		this.editAttachmentList = editAttachmentList;
	}

	public boolean isShowFormAddUserStory() {
		return showFormAddUserStory;
	}

	public void setShowFormAddUserStory(boolean showFormAddUserStory) {
		this.showFormAddUserStory = showFormAddUserStory;
	}

	public List<KanbanSwimline> getKanbanSwimlines() {
		return kanbanSwimlines;
	}

	public void setKanbanSwimlines(List<KanbanSwimline> kanbanSwimlines) {
		this.kanbanSwimlines = kanbanSwimlines;
	}

	public Long getFirstSwimline() {
		return firstSwimline;
	}

	public void setFirstSwimline(Long firstSwimline) {
		this.firstSwimline = firstSwimline;
	}
	
	public List<KanbanStatus> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<KanbanStatus> statusList) {
		this.statusList = statusList;
	}

	public List<Issue> getIssueList() {
		return issueList;
	}

	public void setIssueList(List<Issue> issueList) {
		this.issueList = issueList;
	}

	public List<KanbanSwimline> getKanbanSwimlinesTemp() {
		return kanbanSwimlinesTemp;
	}

	public void setKanbanSwimlinesTemp(List<KanbanSwimline> kanbanSwimlinesTemp) {
		this.kanbanSwimlinesTemp = kanbanSwimlinesTemp;
	}

	public KanbanSwimline getFirstSwimlineObject() {
		return firstSwimlineObject;
	}

	public void setFirstSwimlineObject(KanbanSwimline firstSwimlineObject) {
		this.firstSwimlineObject = firstSwimlineObject;
	}

	public Long getIdSortUserStory() {
		return idSortUserStory;
	}

	public void setIdSortUserStory(Long idSortUserStory) {
		this.idSortUserStory = idSortUserStory;
	}

	public boolean isSaveSuccess() {
		return saveSuccess;
	}

	public void setSaveSuccess(boolean saveSuccess) {
		this.saveSuccess = saveSuccess;
	}

	public boolean isUpdateSuccess() {
		return updateSuccess;
	}

	public void setUpdateSuccess(boolean updateSuccess) {
		this.updateSuccess = updateSuccess;
	}

	public Team getCurrentTeam() {
		return currentTeam;
	}

	public void setCurrentTeam(Team currentTeam) {
		this.currentTeam = currentTeam;
	}

	public KanbanIssue getEditTask() {
		return editTask;
	}

	public void setEditTask(KanbanIssue editTask) {
		this.editTask = editTask;
	}

	public Long getIdEditTask() {
		return idEditTask;
	}

	public void setIdEditTask(Long idEditTask) {
		this.idEditTask = idEditTask;
	}

	public Long getIdDeleteTask() {
		return idDeleteTask;
	}

	public void setIdDeleteTask(Long idDeleteTask) {
		this.idDeleteTask = idDeleteTask;
	}

	public String getPointRemain() {
		return pointRemain;
	}

	public void setPointRemain(String pointRemain) {
		this.pointRemain = pointRemain;
	}

	public String getPointR() {
		return pointR;
	}

	public void setPointR(String pointR) {
		this.pointR = pointR;
	}

	public Long getCurrentTeamId() {
		return currentTeamId;
	}

	public void setCurrentTeamId(Long currentTeamId) {
		this.currentTeamId = currentTeamId;
	}

	public StateLazyLoading getLazyLoadingProductBacklogs() {
		return lazyLoadingProductBacklogs;
	}

	public void setLazyLoadingProductBacklogs(
			StateLazyLoading lazyLoadingProductBacklogs) {
		this.lazyLoadingProductBacklogs = lazyLoadingProductBacklogs;
	}

	public boolean isRemainingProductBacklogs() {
		return remainingProductBacklogs;
	}

	public void setRemainingProductBacklogs(boolean remainingProductBacklogs) {
		this.remainingProductBacklogs = remainingProductBacklogs;
	}

	public int getTotalRowProductBacklog() {
		return totalRowProductBacklog;
	}

	public void setTotalRowProductBacklog(int totalRowProductBacklog) {
		this.totalRowProductBacklog = totalRowProductBacklog;
	}

	public DisappearTaskConfiguration getDisappearTaskConfiguration() {
		return disappearTaskConfiguration;
	}

	public void setDisappearTaskConfiguration(
			DisappearTaskConfiguration disappearTaskConfiguration) {
		this.disappearTaskConfiguration = disappearTaskConfiguration;
	}

	public KanbanStatus getAcceptedStatus() {
		return acceptedStatus;
	}

	public void setAcceptedStatus(KanbanStatus acceptedStatus) {
		this.acceptedStatus = acceptedStatus;
	}

	public int getTotalAllCellWith() {
		return totalAllCellWith;
	}

	public void setTotalAllCellWith(int totalAllCellWith) {
		this.totalAllCellWith = totalAllCellWith;
	}

	public KanbanIssue getViewKanbanIssue() {
		return viewKanbanIssue;
	}

	public void setViewKanbanIssue(KanbanIssue viewKanbanIssue) {
		this.viewKanbanIssue = viewKanbanIssue;
	}
	
	private void excludeHiddenTeamsForSelection(List<Team> allTeams)
	{
	    this.listTeams = new ArrayList<Team>();
	    
	    if (allTeams != null && allTeams.size() > 0)
	    {
		for (Team team : allTeams) 
		{
		    if (team != null && team.getValidTo() == null) //validTo = null => team is not hidden. 
		    {
			this.listTeams.add(team);
		    }
		}
	    }
	}
}