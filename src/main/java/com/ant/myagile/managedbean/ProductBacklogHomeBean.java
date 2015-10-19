package com.ant.myagile.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.json.JSONArray;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.ExportImportFileService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("productBacklogHomeBean")
@Scope("session")
public class ProductBacklogHomeBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(ProductBacklogHomeBean.class);

	@Autowired
	UserStoryService userStoryService;
	@Autowired
	TeamService teamService;
	@Autowired
	ProjectService projectService;
	@Autowired
	Utils utils;
	@Autowired
	MemberService memberService;
	@Autowired
	MailService mailService;
	@Autowired
	IssueService issueService;
	@Autowired
	AttachmentService attachmentService;
	@Autowired
	ExportImportFileService exportImportFileService;
	@Autowired
	KanbanIssueService kanbanIssueService;

	private long projectId;
	private List<Integer> progressList;
	private List<UserStory> userStoryList;
	private UserStory userStory;
	private String deleteId;
	private String deleteName;
	private Long deleteSortId;
	private int sizeMaxUs;
	private List<String> selectedFilter = Arrays.asList("TODO", "IN_PROGRESS");

	private String order = "userStoryId";
	private String orderBy = "0";
	private String searchedKeyWord = "";
	private boolean usingImportExcel = false;
	private LazyDataModel<UserStory> lazyUS;
	private static final int EMPTY_ERROR = 1;
	private static final int DUPPLICATE_ERROR = 2;
	private static final int EXCEPTION = 3;
	private static final int VALUE_ERROR = 4;
	private static final int RISK_ERROR = 5;
	private static final int NO_ERROR = 0;
	private static final int MAX_BUSSINESS_VALUE = 1000;
	private static final int MAX_RISK_VALUE = 1000;
	
	private Attachment attachment;
	private List<Attachment> notAddedAttachmentList;
	private List<Attachment> attachmentList;
	private Attachment deleteAttachment;
	private Attachment selectedAttachment;
	private List<Issue> relatedIssues;
	private List<KanbanIssue> relatedKanbanIssues;
	private boolean editMode;
	private boolean createMode;
	private List<Attachment> editAttachmentList;
	private Long selectedUserStoryId;
	
	
	@PostConstruct
	public void init() {
		progressList = new ArrayList<Integer>();
		userStoryList = new ArrayList<UserStory>();
		this.attachment = new Attachment();
		this.attachmentList = new ArrayList<Attachment>();
		
		if (this.notAddedAttachmentList == null) {
			setNotAddedAttachmentList(new ArrayList<Attachment>());
		}else{
			this.notAddedAttachmentList.clear();
		}
	}
	
	
	public List<Attachment> getEditAttachmentList() 
	{
		return editAttachmentList;
	}

	public void setEditAttachmentList(List<Attachment> editAttachmentList) 
	{
		this.editAttachmentList = editAttachmentList;
	}


	/**
	 * Create new user story with information entered in view, then save it into
	 * database
	 */
	public void addUserStory() 
	{
		RequestContext requestContext = RequestContext.getCurrentInstance();

		if (getProjectId() == 0) {
			requestContext.addCallbackParam("success", false);
			return;
		}
		try {
			UserStory us = new UserStory();
			
			//Remove Control Characters
			us.setName(Utils.standardizeString(userStory.getName()).replaceAll("\\p{Cntrl}", ""));
			us.setDescription(userStory.getDescription().replaceAll("\\p{Cntrl}", ""));
			
			us.setStatus(UserStory.StatusType.TODO);
			us.setValue(userStory.getValue());
			us.setProject(projectService.findProjectById(getProjectId()));
			us.setRisk(0);
			us.setPriority(UserStory.PriorityType.NONE);
			if (userStoryService.create(us)) {
				saveAttachmentsToDatabase(us);
				
				resetUserStoryValue();
				setValueForUsList();
				setCreateMode(false);
				requestContext.addCallbackParam("success", true);
				JSFUtils.resloveMethodExpression("#{homeBean.setDefaultHistoryList}", Void.class, new Class<?>[0], new Object[0]);
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			requestContext.addCallbackParam("success", false);
			LOGGER.error("HomePage addUserStory: " + e);
		}
	}

	/**
	 * TODO Knight team user or delete later
	 * Update user story with 'userStoryId' check 'name' of the user story if
	 * empty or duplicate send 'userStoryId' in response to client side
	 * 
	 * @param userStoryId
	 * @param name
	 */
//	public void updateUserStory(Long userStoryId, String name) {
//		RequestContext context = RequestContext.getCurrentInstance();
//		UserStory current = userStoryService.findUserStoryById(userStoryId);
//		if (current == null) {
//			context.addCallbackParam("userStoryId", userStoryId);
//			context.addCallbackParam("error", EXCEPTION);
//			return;
//		}
//
//		if (name.equals("")) {
//			context.addCallbackParam("userStoryId", userStoryId);
//			context.addCallbackParam("error", EMPTY_ERROR);
//			return;
//		}
//
//		if (userStoryService.checkExistUserStory(Utils.standardizeString(name), getProjectId()) != null && !(current.getName().equals(Utils.standardizeString(name)))) {
//			context.addCallbackParam("userStoryId", userStoryId);
//			context.addCallbackParam("error", DUPPLICATE_ERROR);
//			return;
//		}
//
//		for (UserStory us : userStoryList) {
//			if (us.getUserStoryId().equals(userStoryId)) {
//				if (us.getValue() > MAX_BUSSINESS_VALUE || us.getValue() < 0) {
//					context.addCallbackParam("userStoryId", userStoryId);
//					context.addCallbackParam("error", VALUE_ERROR);
//					return;
//				}
//				
//				//Remove Control Characters
//				us.setName(us.getName().replaceAll("\\p{Cntrl}", ""));
//				us.setDescription(us.getDescription().replaceAll("\\p{Cntrl}", ""));
//				
//				if (userStoryService.update(us)) {
//					userStoryService.updateAllIssueOfUserStoryHaveTheSameContent(us);
//					setValueForUsList();
//					context.addCallbackParam("error", NO_ERROR);
//					context.addCallbackParam("userStoryId", userStoryId);
//					JSFUtils.resloveMethodExpression("#{homeBean.setDefaultHistoryList}", Void.class, new Class<?>[0], new Object[0]);
//				} else {
//					context.addCallbackParam("userStoryId", userStoryId);
//					context.addCallbackParam("error", EXCEPTION);
//				}
//				break;
//			}
//		}
//
//	}
	
	public void updateUserStory() {
		RequestContext context = RequestContext.getCurrentInstance();
		if (userStory == null) {
			context.addCallbackParam("userStoryId", userStory.getUserStoryId());
			context.addCallbackParam("error", EXCEPTION);
			return;
		}

		if (userStory.getName().equals("")) {
			context.addCallbackParam("userStoryId", userStory.getUserStoryId());
			context.addCallbackParam("error", EMPTY_ERROR);
			return;
		}

		if (userStoryService.checkExistUserStory(Utils.standardizeString(userStory.getName()), getProjectId()) != null && !(userStory.getName().equals(Utils.standardizeString(userStory.getName())))) {
			context.addCallbackParam("userStoryId", userStory.getUserStoryId());
			context.addCallbackParam("error", DUPPLICATE_ERROR);
			return;
		}

		if(userStory.getValue() > MAX_BUSSINESS_VALUE || userStory.getValue() < 0 ){
			context.addCallbackParam("userStoryId", userStory.getUserStoryId());
			context.addCallbackParam("error", VALUE_ERROR);
			return;
		}
		
		userStory.setName(userStory.getName().replaceAll("\\p{Cntrl}", ""));
		userStory.setDescription(userStory.getDescription().replaceAll("\\p{Cntrl}", ""));
		
		if(userStoryService.update(userStory)){
			userStoryService.updateAllIssueOfUserStoryHaveTheSameContent(userStory);
			setValueForUsList();
			context.addCallbackParam("error", NO_ERROR);
			context.addCallbackParam("userStoryId", userStory.getUserStoryId());
			JSFUtils.resloveMethodExpression("#{homeBean.setDefaultHistoryList}", Void.class, new Class<?>[0], new Object[0]);
		} else {
			context.addCallbackParam("userStoryId", userStory.getUserStoryId());
			context.addCallbackParam("error", EXCEPTION);
		}

	}

	/**
	 * update user story on menu backlog (sprint backlog)
	 * 
	 * @param userStoryId
	 * @param name
	 * @param value
	 */
	public void updatePBLOnBacklogPage() {
		try {
			RequestContext requestContext = RequestContext.getCurrentInstance();
			String id = JSFUtils.getRequestParameter("Id");
			String name = JSFUtils.getRequestParameter("name");
			String description = JSFUtils.getRequestParameter("description");
			String value = JSFUtils.getRequestParameter("value");
			String risk = JSFUtils.getRequestParameter("risk");
			UserStory current = userStoryService.findUserStoryById(Long.parseLong(id.trim()));
			if (current == null) {
				requestContext.addCallbackParam("userStoryId", id);
				requestContext.addCallbackParam("error", EXCEPTION);
				return;
			}

			if (name.equals("")) {
				requestContext.addCallbackParam("userStoryId", id);
				requestContext.addCallbackParam("error", EMPTY_ERROR);
				return;
			}

			if ((userStoryService.checkExistUserStory(Utils.standardizeString(name), current.getProject().getProjectId()) != null) && !(current.getName().equals(Utils.standardizeString(name)))) {
				requestContext.addCallbackParam("userStoryId", id);
				requestContext.addCallbackParam("error", DUPPLICATE_ERROR);
				return;
			}
			if (Integer.parseInt(value) > MAX_BUSSINESS_VALUE || Integer.parseInt(value) < 0) {
				requestContext.addCallbackParam("userStoryId", id);
				requestContext.addCallbackParam("error", VALUE_ERROR);
				return;
			}
			if (Integer.parseInt(risk) > MAX_RISK_VALUE || Integer.parseInt(risk) < 0) {
				requestContext.addCallbackParam("userStoryId", id);
				requestContext.addCallbackParam("error", RISK_ERROR);
				return;
			}
			current.setName(name.replaceAll("\\p{Cntrl}", ""));
			current.setValue(Integer.parseInt(value));
			current.setRisk(Integer.parseInt(risk));
			current.setDescription(description.replaceAll("\\p{Cntrl}", ""));
			if(userStoryService.update(current)){
				userStoryService.updateAllIssueOfUserStoryHaveTheSameContent(current);
			}
			requestContext.execute("PrimeFaces.ab({source:'',update:'form2'});");
		} catch (Exception e) {

		}
	}

	/**
	 * delete user story
	 */
	public void deleteUserStory() {
		try {
			boolean flag = true;
			Member owner = memberService.findProjectOwner(getProjectId());
			UserStory userStoryTemp = userStoryService.findUserStoryById(Long.parseLong(deleteId));
			userStoryService.delete(Long.parseLong(deleteId));
			Member member = utils.getLoggedInMember();

			String mailContent = "User story #" + userStoryTemp.getUserStoryId() + " has been removed by <b>" + member.getLastName() + " " + member.getFirstName() + "</b><br/>";
			mailContent += "<hr>";
			mailContent += "<h3>User story #" + userStoryTemp.getUserStoryId() + ": " + userStoryTemp.getName() + "</h3>";
			mailContent += "<ul><li>Name: " + userStoryTemp.getName() + "</li>";
			mailContent += "<li>Description: " + userStoryTemp.getDescription() + "</li>";
			mailContent += "<li>Value: " + userStoryTemp.getValue() + "</li>";
			mailContent += "<li>Status: " + userStoryTemp.getStatus() + "</li></ul>";
			mailContent += "<hr>";
			mailContent += "You have received this notification because you have either subscribed to it, or are involved in it.";

			List<Team> teamList = teamService.findTeamsByProjectId(getProjectId());

			for (int i = 0; i < teamList.size(); i++) {
				List<TeamMember> teamMemberList = memberService.findTeamMemberListByTeamId(teamList.get(i).getTeamId());
				for (int j = 0; j < teamMemberList.size(); j++) {
					if (teamMemberList.get(j).getMember().getUsername().equals(owner.getUsername())) {
						flag = false;
					}
					mailService.sendMail("Inform about remove user story", mailContent, teamMemberList.get(j).getMember().getUsername());
				}
			}
			if (flag) {
				mailService.sendMail("Inform about remove user story", mailContent, owner.getUsername());
			}
			this.setValueForUsList();
			JSFUtils.resloveMethodExpression("#{homeBean.setDefaultHistoryList}", Void.class, new Class<?>[0], new Object[0]);
		} catch (Exception e) {

		}
	}

	public void setValueForUsList() {
		userStoryList.clear();
		if (getProjectId() == 0) {
			return;
		}
		if (selectedFilter.size() == 0) {
			return;
		}

		userStoryList.addAll(loadUserStoriesByCriteria());

		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.execute("PrimeFaces.ab({source:'',update:'userStoryDetail'});");
		lazyUS = new LazyDataModel<UserStory>() {
			private static final long serialVersionUID = 1L;
			List<UserStory> listUserStoryPerPage;

			@Override
			public List<UserStory> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {

				progressList.clear();

				int getTo = first + pageSize;
				if (first > userStoryList.size()) {
					first = 0;
				}
				if (getTo > userStoryList.size()) {
					getTo = userStoryList.size();
				}
				listUserStoryPerPage = userStoryList.subList(first, getTo);
				for (int i = 0; i < listUserStoryPerPage.size(); i++) {
					try {
						progressList.add(userStoryService.findProgressOfUserStory(listUserStoryPerPage.get(i)));
					} catch (Exception e) {
						LOGGER.warn(e);
					}
				}
				// Update list
				RequestContext requestContext = RequestContext.getCurrentInstance();
				requestContext.execute("handleProgressBar(" + new JSONArray(progressList, true).toString() + ");");
				return listUserStoryPerPage;
			}

			@Override
			public void setRowIndex(int rowIndex) {
				if (rowIndex == -1 || getPageSize() == 0) {
					super.setRowIndex(-1);
				} else {
					super.setRowIndex(rowIndex % getPageSize());
				}
			}

		};

		lazyUS.setRowCount(userStoryList.size());
	}

	private List<UserStory> loadUserStoriesByCriteria() {
		List<UserStory> resultList = new ArrayList<UserStory>();
		String keyword = searchedKeyWord.trim();
		List<UserStory.StatusType> statusFilter = new ArrayList<UserStory.StatusType>();
		for (String statusString : selectedFilter) {
			statusFilter.add(UserStory.StatusType.valueOf(statusString));
		}
		resultList.addAll(userStoryService.findUserStoriesByCriteria(statusFilter, getProjectId(), order, Integer.parseInt(orderBy), keyword));
		return resultList;
	}

	public Integer calculateProgressOfUserStory(UserStory userStory) {
		return userStoryService.findProgressOfUserStory(userStory);
	}

	public UserStory.StatusType loadStatusOfUserStory(UserStory userStory) {
		if (userStory.getStatus() == UserStory.StatusType.VOID) {
			return UserStory.StatusType.VOID;
		}
		return userStoryService.findStatusOfUserStory(userStory);
	}

	/**
	 * Set status VOID for current user story
	 */
	public void setVoid() {
		try{
		UserStory usTemp = userStoryService.findUserStoryById(selectedUserStoryId);
		usTemp.setPreviousStatus(usTemp.getStatus());
		usTemp.setStatus(UserStory.StatusType.VOID);
//		kanbanIssueService.deleteAllKanbanIssueByUserStoryID(usTemp.getUserStoryId());
		issueService.setVoidAllUnexpiredIssuesWhenSetUserStoryVoid(usTemp.getUserStoryId());
		kanbanIssueService.setVoidAllKanbanIssueWhenSetVoidUserStory(usTemp.getUserStoryId());
		userStoryService.update(usTemp);
		
	
		setValueForUsList();
		JSFUtils.resloveMethodExpression("#{homeBean.setDefaultHistoryList}", Void.class, new Class<?>[0], new Object[0]);
		} catch(Exception e ){
			LOGGER.error(e);
		}
	}
	
	public void setRelatedIssueForSelectedUserStory(Long userStoryId){
		selectedUserStoryId = userStoryId;
		relatedIssues = issueService.findAllUnexpiredChildrenIssuesByUserStory(userStoryId);
		relatedKanbanIssues = kanbanIssueService.findKanbanIssuesByUserStory(userStoryId);
	}

	/**
	 * Set status Destroy VOID for current user story
	 */
	public void destroyVoid() {
		UserStory usTemp = userStoryService.findUserStoryById(selectedUserStoryId);
		usTemp.setStatus(usTemp.getPreviousStatus());
		userStoryService.update(usTemp);
		setValueForUsList();
		issueService.destroyVoidAllUnexpiredIssuesWhenDestroyVoidUserStory(usTemp.getUserStoryId());
		kanbanIssueService.destroyVoidAllKanbanIssueWhenDestroyVoidUserStory(usTemp.getUserStoryId());
	}

	public void validateUserStoryName(FacesContext context, UIComponent validate, Object value) throws ValidatorException {

		if (userStoryService.checkExistUserStory(Utils.standardizeString(value.toString()), getProjectId()) != null) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(utils.getMessage("myagile.backlog.Exists", null));
			throw new ValidatorException(msg);
		}
		if (utils.isExistTooLongWord(value.toString())) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(utils.getMessage("myagile.backlog.LongestLength", null));
			throw new ValidatorException(msg);
		}
	}
	

	public void resetUserStoryValue() {
		setValueForUsList();
		this.userStory.setName("");
		this.userStory.setDescription("");
		this.userStory.setValue(0);
		
		if (this.notAddedAttachmentList == null) {
			setNotAddedAttachmentList(new ArrayList<Attachment>());
		}else{
			this.notAddedAttachmentList.clear();
		}
	}

	public void resetForm() {
		resetUserStoryValue();
		JSFUtils.resetForm("addNewForm");
	}

	public LazyDataModel<UserStory> getUsList() {
		return lazyUS;
	}

	public void setUsList(List<UserStory> usList) {
		try {
			for (UserStory us : usList) {
				userStoryService.update(us);
			}
			this.userStoryList = usList;
		} catch (Exception e) {
		}
	}

	/* Getter and Setter */

	public int getSizeMaxUs() {
		return sizeMaxUs;
	}

	public void setSizeMaxUs(int sizeMaxUs) {
		this.sizeMaxUs = sizeMaxUs;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getDeleteId() {
		return deleteId;
	}

	public void setDeleteId(String deleteId) {
		this.deleteId = deleteId;
	}

	public String getDeleteName() {
		return deleteName;
	}

	public void setDeleteName(String deleteName) {
		this.deleteName = deleteName;
	}

	public String getSearchedKeyWord() {
		return searchedKeyWord;
	}

	public void setSearchedKeyWord(String searchedKeyWord) {
		this.searchedKeyWord = searchedKeyWord;
		setValueForUsList();
	}

	public List<Integer> getProgressList() {
		return progressList;
	}

	public void setProgressList(List<Integer> progressList) {
		this.progressList = progressList;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public boolean getUsingImportExcel() {
		return usingImportExcel;
	}

	public void setUsingImportExcel(boolean usingImportExcel) {
		this.usingImportExcel = usingImportExcel;
	}

	public UserStory getUserStory() {
		if (this.userStory == null) {
			this.userStory = new UserStory();
		}
		return userStory;
	}

	
	public Long getDeleteSortId() {
		return deleteSortId;
	}

	public void setDeleteSortId(Long deleteSortId) {
		this.deleteSortId = deleteSortId;
	}

	public List<String> getSelectedFilter() {
		return selectedFilter;
	}

	public void setSelectedFilter(List<String> selectedFilter) {
		this.selectedFilter = selectedFilter;
	}
	
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

	public void uploadNewAttachment() {
		if (addAttachment()) {
				this.notAddedAttachmentList.add(this.attachment);
			}
	}
	
	private boolean addAttachment() {
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
		}catch(Exception exception){
			LOGGER.error("addAttachment at sprintBacklog " + exception);
			return false;
		}
	}
	
	public void deleteNotAddedAttachment() {
		try {
			this.attachmentService.deleteFileInTemp(deleteAttachment.getDiskFilename());
			this.notAddedAttachmentList.remove(deleteAttachment);
		} catch (Exception e) {
			LOGGER.error("deleteAttachment at sprintBackLog " + e);
		}
	}
	
	private void saveAttachmentsToDatabase(UserStory us) {
		try{
		for(Attachment attachment: this.notAddedAttachmentList){
			attachment.setContainerId(us.getUserStoryId());
			attachment.setTemp(false);
			this.attachmentService.moveAttachmentFile(attachment, us.getProject().getProjectId());
			this.attachmentService.save(attachment);
		}
		}catch(Exception e){
			LOGGER.error("saveAttachmentsToDatabase " + e );
		}
	}
	
	public String shortNameOfAttachment(String fileName){
		if(fileName.length() >= 15){
			String first = fileName.substring(0, 5);
			String last = fileName.substring(fileName.length() - 5);
			return first + "..." + last;
		}
		return fileName;
	}
	
//	==================== Attachment when edit US ======================
	
	private void setOtherValueForCurrentUserStory() {
		if (this.userStory.getStatus() != null && this.userStory.getStatus() == UserStory.StatusType.VOID) {
			this.userStory.setStatus(UserStory.StatusType.VOID);
		}
		if (this.attachmentList.size() > 0) {
			this.attachmentList.clear();
		}
		this.attachmentList = this.attachmentService.findAttachmentByUserStory(this.userStory);
	}
	
	public void setUserStory(UserStory userStory) {
		this.userStory = userStory;
		if (this.userStory != null) {
			setOtherValueForCurrentUserStory();
		} else {
			this.userStory = new UserStory();
			this.userStory.setProject(this.projectService.findProjectById(this.projectId));
			this.attachmentList = new ArrayList<Attachment>();
		}
	}
	
	public void uploadAttachment() {
		if (saveAttachment()) {
			Attachment newAttachment = this.attachmentService.findAttachmentById(this.attachment.getAttachmentId());
			if (this.notAddedAttachmentList == null) {
				setNotAddedAttachmentList(new ArrayList<Attachment>());
			}
			if (newAttachment.getContainerId() != null) {
				this.attachment.setTemp(false);
				this.attachmentService.moveAttachmentFile(this.attachment, this.userStory.getProject().getProjectId());
				this.attachmentService.update(newAttachment);
			} else {
				this.attachment.setTemp(true);
				this.notAddedAttachmentList.add(newAttachment);
			}
			setAttachmentList(this.attachmentService.findAttachmentByUserStory(this.userStory));
		}
	}
	
	private boolean saveAttachment() {
		String filename = JSFUtils.getRequestParameter("filename");
		String diskFileName = this.attachmentService.fileNameProcess(FilenameUtils.removeExtension(filename));
		diskFileName = this.attachmentService.replaceFile(filename, diskFileName);
		this.attachment.setFilename(filename);
		this.attachment.setDiskFilename(diskFileName);
		this.attachment.setContainerId(this.userStory.getUserStoryId());
		this.attachment.setContainerType(Attachment.USERSTORY_ATTACHMENT);
		if (this.attachment.getContainerId() == null) {
			this.attachment.setTemp(true);
		} else {
			this.attachment.setTemp(false);
		}
		this.attachment.setCreatedOn(new Date());
		this.attachment.setAuthor(this.utils.getLoggedInMember());
		return this.attachmentService.save(this.attachment);
	}
	
	public void handleCreateNewUserStory() {
		setCreateMode(true);
		setEditMode(false);
		resetForm();
		prepareUploadFile();
	}
	
	private void prepareUploadFile() {
		if (this.notAddedAttachmentList.size() > 0) {
			for (Attachment att : this.notAddedAttachmentList) {
				this.attachmentService.delete(att);
				this.attachmentService.deleteFileInTemp(att.getDiskFilename());
			}
			this.notAddedAttachmentList.clear();
		} else {
			this.notAddedAttachmentList = new ArrayList<Attachment>();
		}
		this.attachment = new Attachment();
		this.attachmentList = new ArrayList<Attachment>();
		this.attachmentList.clear();
	}
	
	public void deleteAttachmentInDB() {
		try {
			attachmentService.delete(selectedAttachment);
			attachmentList.remove(selectedAttachment);
		} catch (Exception e) {
			LOGGER.error("deleteAttachment in DB at Homepage " + e);
		}
	}
	
	public void handleEditUserStory(){
		setCreateMode(false);
		setEditMode(true);
		resetEditForm();
	}
	
	public void validateUserStoryWhenEdit(FacesContext context, UIInput validate, Object value) throws ValidatorException {
		final Long userStoryId = this.userStory.getUserStoryId();
		final UserStory currentUserStory = this.userStoryService.findUserStoryById(userStoryId);
		final UserStory newUserStory = this.userStoryService.checkExistUserStory(Utils.standardizeString(value.toString()), this.userStory.getProject().getProjectId());
		if ((newUserStory != null) && !(currentUserStory.getName().equals(Utils.standardizeString(value.toString())))) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(utils.getMessage("myagile.backlog.Exists", null));
			throw new ValidatorException(msg);
		}
		if (utils.isExistTooLongWord(value.toString())) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(utils.getMessage("myagile.backlog.LongestLength", null));
			throw new ValidatorException(msg);
		}
	}
	
	
	public void resetEditForm() {
		JSFUtils.resetForm("frmEditUs");
	}
	
	public List<Attachment> getAttachmentList() {
		return attachmentList;
	}

	public void setAttachmentList(List<Attachment> attachmentList) {
		this.attachmentList = attachmentList;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public boolean isCreateMode() {
		return createMode;
	}

	public void setCreateMode(boolean createMode) {
		this.createMode = createMode;
	}

	public Attachment getSelectedAttachment() {
		return selectedAttachment;
	}

	public void setSelectedAttachment(Attachment selectedAttachment) {
		this.selectedAttachment = selectedAttachment;
	}

	public List<Issue> getRelatedIssues() {
		return relatedIssues;
	}

	public void setRelatedIssues(List<Issue> relatedIssues) {
		this.relatedIssues = relatedIssues;
	}

	public List<KanbanIssue> getRelatedKanbanIssues() {
		return relatedKanbanIssues;
	}

	public void setRelatedKanbanIssues(List<KanbanIssue> relatedKanbanIssues) {
		this.relatedKanbanIssues = relatedKanbanIssues;
	}

	public Long getSelectedUserStoryId() {
		return selectedUserStoryId;
	}

	public void setSelectedUserStoryId(Long selectedUserStoryId) {
		this.selectedUserStoryId = selectedUserStoryId;
	}
	
	
}
