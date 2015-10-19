package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;

import jxl.biff.NameRangeException;
import jxl.write.WriteException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.ExportImportFileService;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.service.LastUserStoryIndexService;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.UserStoryService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;
import com.itextpdf.text.DocumentException;
import org.apache.myfaces.custom.fileupload.UploadedFile;

@Component("productBacklogBean")
@Scope("session")
public class ProductBacklogBean implements Serializable {

	private static final Logger LOGGER = Logger.getLogger(ProductBacklogBean.class);
	
	private static final long serialVersionUID = 1L;
	private static final String POINT_NUMBER_FORMAT = "2";

	private long projectId = 0;
	private UserStory userStory;
	private UserStory selectedUserStory;
	private Integer progressOfCurrentUserStory;
	private Attachment attachment;
	private List<Attachment> notAddedAttachmentList;
	private Long notAddAttachmentId;
	private List<Attachment> attachmentList;
	private Long selectedAttachmentId;
	private List<UserStory> userStoryList;
	private List<UserStory> filteredUserStories;
	private List<Project> projectList;
	private List<Issue> relatedIssues;
	private UserStory.StatusType tempStatus;
	private List<UserStory.StatusType> selectedFilter = Arrays.asList(UserStory.StatusType.TODO, UserStory.StatusType.IN_PROGRESS);
	private List<UserStory> selectedUserStories = new ArrayList<UserStory>();
	private boolean selectedAllUserStory = false;
	
	private static final String ATTACHMENT_PATH = MyAgileFileUtils.getStorageLocation("myagile.upload.temp.location") + "/attachment/";
	private static final int XLSFORMAT = 1;
	private static final int XLSXFORMAT = 2;
	private static final int NAME = 0;
	private static final int DESCRIPTION = 1;
	private static final int VALUE = 2;
	private static final int RISK = 3;
	private static final int PRIORITY = 4;
	private static final int NUMBER_OF_COLUNM = 5;
	private static final int MAX_LENGTH = 255;
	private static final int MAX_BUSSINESS_VALUE = 1000;
	private static final int MAX_RISK_VALUE = 1000;
	private static final int SUCCESS_MESSAGE_TYPE = 1;
	private static final int ERROR_MESSAGE_TYPE = 2;
	private static final int WARN_MESSAGE_TYPE = 3;
	private static final int FIRST_ROW_INDEX = 0;
	private static final int VALUE_ERROR = 1;
	private static final int RISK_ERROR = 2;
	private static final int PRIORITY_ERROR = 3;
	private int errorType = 0;
    private UploadedFile uploadedFile;

	private boolean edit = false;
	private boolean createMode = false;

	@Autowired
	private UserStoryService userStoryService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private IssueService issueService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private MailService mailService;
	@Autowired
	private Utils utils;
	@Autowired
	private AttachmentService attachmentService;
	@Autowired
	private LastUserStoryIndexService lastUSIndexService;
	@Autowired
	private ExportImportFileService exportImportFileService;

	@PostConstruct
	public void init() {
		this.userStory = new UserStory();
		this.attachment = new Attachment();
		this.attachmentList = new ArrayList<Attachment>();
		this.notAddedAttachmentList = new ArrayList<Attachment>();
	}

	public void exportExcelProductBacklogFromProject() throws WriteException, IOException{
		LOGGER.debug("---export excel---");
		List<UserStory> userStories = this.userStoryService.findAllUserStoryByProjectId(this.projectId);
		if(!userStories.isEmpty()){
			exportImportFileService.exportProductBacklogToExcel(userStories);
		}
		LOGGER.debug("---end export excel product backlog---");
	}
	
	/**
	 * initialize default value for preview
	 */
	public void initPreview() {
		if (JSFUtils.isPostbackRequired()) {
			Member memberLogin = this.utils.getLoggedInMember();
			this.projectList = this.projectService.findByMemberAndOwner(memberLogin.getMemberId());
			if (this.projectList != null && !this.projectList.isEmpty()) {
				if (this.projectId == 0) {
					this.projectId = this.projectList.get(0).getProjectId();
				}
				updateUserStoryList();
				setOtherValueForCurrentUserStory();
			}
			setEdit(false);
			setFilteredUserStories(null);
		}
	}

	/**
	 * Reset edited value not saved when refresh page
	 */
	public void onRefreshPage() {
		if (FacesContext.getCurrentInstance().isPostback()) {
			UserStory temp = this.userStoryService.findUserStoryById(this.userStory.getUserStoryId());
			this.setUserStory(temp);
		}

	}
	
	public void selectOrUnSelectAllUserStory(){
		if(selectedAllUserStory){
			//unselect all
			selectedUserStories.clear();
			selectedAllUserStory = false;
		} else {
			//select all
			selectedUserStories.clear();
			selectedUserStories.addAll(userStoryList);
			selectedAllUserStory = true;
		}
	}
	
	//selected userstory
	public void selectUserStory() {
		String idUserStoryStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idUserStory");
		Long idUserstory = Long.valueOf(idUserStoryStr);
		UserStory userStoryFromId = userStoryService.findUserStoryById(idUserstory);
		if(!selectedUserStories.contains(userStoryFromId)){
			selectedUserStories.add(userStoryFromId);
			if(selectedUserStories.size() == userStoryList.size()){
				selectedAllUserStory = true;
				RequestContext.getCurrentInstance().update("form-userStoryTable");
			}
		}
	}
	
	//unselected userstory
	public void unSelectUserStory() {
		String idUserStoryStr = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idUserStory");
		Long idUserstory = Long.valueOf(idUserStoryStr);
		UserStory userStoryFromId = userStoryService.findUserStoryById(idUserstory);
		if(selectedUserStories.contains(userStoryFromId)){
			selectedUserStories.remove(userStoryFromId);
			selectedAllUserStory = false;
		}
	}
	
	public void exportUserStoryPDF() throws IOException, DocumentException {
		if(!selectedUserStories.isEmpty()){
			this.exportImportFileService.exportUserStoryPDF(this.selectedUserStories);
		}
	}
	
	public void checkExportUserStoryPDF(){
		if(selectedUserStories.isEmpty()) {
			RequestContext.getCurrentInstance().execute("exportError.show()");
		}else {
			RequestContext.getCurrentInstance().execute("triggerClickExportPdf()");
		}
	}

	public void handleChangeProject() {
		updateUserStoryList();
		this.userStory = new UserStory();
		this.userStory.setProject(this.projectService.findProjectById(this.projectId));
		setEdit(false);
		setFilteredUserStories(null);
		//clear selected userstory
		selectedUserStories.clear();
		selectedAllUserStory = false;
	}

	public void editUserStory() {
		setEdit(true);
	}

	/**
	 * prepare for creating a new user story session
	 */
	public void handleCreateNewUserStory() {
		setEdit(true);
		setCreateMode(true);
		this.relatedIssues = null;
		resetForm();
		prepareCreateNewUserStory();
		prepareUploadFile();
	}

	/**
	 * Set status VOID for user story in temporary case
	 */
	public void onSetVoidButton() {
		setTempStatus(this.userStory.getStatus());
		this.userStory.setStatus(UserStory.StatusType.VOID);
	}

	/**
	 * Reset status for user story when cancel edit
	 */
	public void onCancelButton() {
		this.userStory.setStatus(this.getTempStatus());
	}

	public void onDestroyVoidButton() {
		setTempStatus(this.userStory.getStatus());
		this.userStory.setStatus(this.userStoryService.findStatusOfUserStory(this.userStory));
	}

	/**
	 * Save current user story that can be created or updated
	 */
	@SuppressWarnings("static-access")
	public void saveUserStory() {
		try {
			if (this.createMode) {
				
				//Remove Control Characters
				this.userStory.setName(this.userStory.getName().replaceAll("\\p{Cntrl}", ""));
				this.userStory.setDescription(this.userStory.getDescription().replaceAll("\\p{Cntrl}", ""));
				this.userStory.setNote(this.userStory.getNote().replaceAll("\\p{Cntrl}", ""));
				
				if (this.userStoryService.create(this.userStory)) {
					addAttachmentForCurrentUserStory();
					setCreateMode(false);
					updateUserStoryList();
					setOtherValueForCurrentUserStory();
					JSFUtils.addCallBackParam("success", true);
					JSFUtils.addSuccessMessage("msgs", this.utils.getMessage("myagile.backlog.SaveSuccess", null));
				} else {
					JSFUtils.addErrorMessage("msgs", this.utils.getMessage("myagile.backlog.SaveUnsuccess", null));
				}

			} else {
				
				//Remove Control Characters
				this.userStory.setName(this.userStory.getName().replaceAll("\\p{Cntrl}", ""));
				this.userStory.setDescription(this.userStory.getDescription().replaceAll("\\p{Cntrl}", ""));
				this.userStory.setNote(this.userStory.getNote().replaceAll("\\p{Cntrl}", ""));
				
				if (this.userStoryService.update(this.userStory)) {
					this.userStoryService.updateSubjectDescriptionNotePriorityOfAllIssueOfUserStory(this.userStory);
					setEdit(false);
					updateUserStoryList();
					if (this.userStory.getStatus() == UserStory.StatusType.VOID) {
						setOtherValueForCurrentUserStory();
						this.userStory.setStatus(UserStory.StatusType.VOID);
					} else {
						setOtherValueForCurrentUserStory();
					}
					JSFUtils.addCallBackParam("success", true);
					JSFUtils.addSuccessMessage("msgs", this.utils.getMessage("myagile.backlog.UpdateSuccess", null));
				} else {
					JSFUtils.addErrorMessage("msgs", this.utils.getMessage("myagile.backlog.UpdateUnsuccess", null));
				}
			}
		} catch (Exception e) {
			JSFUtils.addErrorMessage("msgs", this.utils.getMessage("myagile.SaveUnsuccess", null));
		}
	}

	public void deleteUserStory() {
		try {
			boolean flag = true;
			Member owner = this.memberService.findProjectOwner(getProjectId());
			UserStory userStoryTemp = this.userStory;
			Member member = this.utils.getLoggedInMember();

			String mailContent = "User story #" + userStoryTemp.getUserStoryId() + " has been removed by <b>" + member.getFirstName() + " " + member.getLastName() + "</b><br/>";
			mailContent += "<hr>";
			mailContent += "<h3>User story #" + userStoryTemp.getUserStoryId() + ": " + userStoryTemp.getName() + "</h3>";
			mailContent += "<ul><li>Name: " + userStoryTemp.getName() + "</li>";
			mailContent += "<li>Description: " + userStoryTemp.getDescription() + "</li>";
			mailContent += "<li>Value: " + userStoryTemp.getValue() + "</li>";
			mailContent += "<li>Status: " + userStoryTemp.getStatus() + "</li></ul>";
			mailContent += "<hr>";
			mailContent += "You have received this notification because you have either subscribed to it, or are involved in it.";

			List<Team> teamList = this.teamService.findTeamsByProjectId(getProjectId());

			for (int i = 0; i < teamList.size(); i++) {
				List<TeamMember> teamMemberList = this.memberService.findTeamMemberListByTeamId(teamList.get(i).getTeamId());
				for (int j = 0; j < teamMemberList.size(); j++) {
					if (teamMemberList.get(j).getMember().getUsername().equals(owner.getUsername())) {
						flag = false;
					}
					this.mailService.sendMail("Inform about remove user story", mailContent, teamMemberList.get(j).getMember().getUsername());
				}
			}
			if (flag) {
				this.mailService.sendMail("Inform about remove user story", mailContent, owner.getUsername());
			}

			setAttachmentList(this.attachmentService.findAttachmentByUserStory(userStoryTemp));
			if (this.attachmentList != null) {
				for (Attachment att : this.attachmentList) {
					this.attachmentService.deleteFile(att.getDiskFilename(), userStoryTemp.getProject().getProjectId());
					this.attachmentService.delete(att);
				}
			}
			this.userStoryService.delete(userStoryTemp.getUserStoryId());
			setUserStory(null);
			updateUserStoryList();
		} catch (Exception e) {
		}
	}

	/**
	 * upload attachment file includes saving it to database and creating real
	 * file on computer saveAttachment(): always save file without error
	 */
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
				//add attachment to add issue
				if(newAttachment.getContainerType().equals(Attachment.USERSTORY_ATTACHMENT)){
					this.attachmentService.addAttachmentToIssue(newAttachment);
				}
			} else {
				this.attachment.setTemp(true);
				this.notAddedAttachmentList.add(newAttachment);
			}
			setAttachmentList(this.attachmentService.findAttachmentByUserStory(this.userStory));
		}
	}
	
	/**
	 * upload attachment file includes saving it to database and creating real
	 * file on computer saveAttachment(): always save file without error
	 */
	public void submit() throws IOException {
        String fileName = FilenameUtils.getName(uploadedFile.getName());
        String contentType = uploadedFile.getContentType();
        byte[] bytes = uploadedFile.getBytes();

        // Now you can save bytes in DB (and also content type?)

        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(String.format("File '%s' of type '%s' successfully uploaded!", fileName, contentType)));
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
	    
	    
	/**
	 * delete an attachment with "edit" mode
	 */
	public void deleteAttachment() {
		try {
			Attachment deleteAttachment = this.attachmentService.findAttachmentById(getSelectedAttachmentId());
			this.attachmentService.delete(deleteAttachment);
			this.attachmentService.deleteFile(deleteAttachment.getDiskFilename(), this.userStory.getProject().getProjectId());
			setAttachmentList(this.attachmentService.findAttachmentByUserStory(this.userStory));
			//delete attach issue
			if(deleteAttachment.getContainerType().equals(Attachment.USERSTORY_ATTACHMENT)){
				attachmentService.deleteAttachmentIssueByAttachment(deleteAttachment);
			}
		} catch (Exception e) {
			LOGGER.error("delete file unsuccessful",e);
		}
	}

	/**
	 * delete an attachment with "create" mode
	 */
	public void deleteNotAddedAttachment() {
		try {
			Attachment deleteAttachment = this.attachmentService.findAttachmentById(getNotAddAttachmentId());
			this.attachmentService.delete(deleteAttachment);
			this.attachmentService.deleteFileInTemp(deleteAttachment.getDiskFilename());
			this.notAddedAttachmentList.remove(deleteAttachment);
		} catch (Exception e) {
		}
	}

	/**
	 * saving an attachment includes adding a record into database and creating
	 * a real-file on computer with "create-mode" that attachment without
	 * "container" will be "temporary" file
	 * 
	 * @return true if save successfully
	 */
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

	/**
	 * other values of a user story includes it's issues, progress, status,
	 * attachment
	 */
	private void setOtherValueForCurrentUserStory() {
		this.relatedIssues = this.issueService.findNoChidrenIssuesByUserStory(this.userStory.getUserStoryId());
		this.progressOfCurrentUserStory = this.userStoryService.findProgressOfUserStory(this.userStory);
		if (this.userStory.getStatus() != null && this.userStory.getStatus() == UserStory.StatusType.VOID) {
			this.userStory.setStatus(UserStory.StatusType.VOID);
		}
		if (this.attachmentList.size() > 0) {
			this.attachmentList.clear();
		}
		this.attachmentList = this.attachmentService.findAttachmentByUserStory(this.userStory);
	}

	public Integer calculateProgressOfUserStory(UserStory userStory) {
		return this.userStoryService.findProgressOfUserStory(userStory);
	}

	public UserStory.StatusType loadStatusOfUserStory(UserStory userStory) {
		if (userStory.getStatus() == UserStory.StatusType.VOID) {
			return UserStory.StatusType.VOID;
		}
		return this.userStoryService.findStatusOfUserStory(userStory);
	}

	private void prepareCreateNewUserStory() {
		this.userStory = new UserStory();
		this.userStory.setSortId(this.lastUSIndexService.findLastUserStoryIndex(this.projectId) + 1);
		this.userStory.setProject(this.projectService.findProjectById(this.projectId));
		this.userStory.setStatus(UserStory.StatusType.TODO);
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

	public void resetForm() {
		JSFUtils.resetForm("userStoryForm");
	}

	/**
	 * re-update user story list from database
	 */
	private void updateUserStoryList() {
		List<UserStory> listTemp = this.userStoryService.findAllUserStoryByProjectId(this.projectId);
		this.userStoryList = new ArrayList<UserStory>();
		if (listTemp != null && listTemp.size() > 0) {
			for (UserStory us : listTemp) {
				if (this.selectedFilter.contains(this.loadStatusOfUserStory(us)) || this.selectedFilter.contains(this.loadStatusOfUserStory(us).name())) {
					this.userStoryList.add(us);
				}
			}

		}
	}

	/**
	 * add attachments for user story with "create" mode
	 */
	private void addAttachmentForCurrentUserStory() {
		if (this.notAddedAttachmentList.size() > 0) {
			for (Attachment att : this.notAddedAttachmentList) {
				att.setContainerId(this.userStory.getUserStoryId());
				att.setTemp(false);
				this.attachmentService.update(att);
				this.attachmentService.moveAttachmentFile(att, this.userStory.getProject().getProjectId());
			}
			this.notAddedAttachmentList.clear();
		}
	}

	public void validateUserStory(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		if (this.userStoryService.checkExistUserStory(Utils.standardizeString(value.toString()), getProjectId()) != null) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.Exists", null));
			throw new ValidatorException(msg);
		}
		if (utils.isExistTooLongWord(value.toString())) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.LongestLength", null));
			throw new ValidatorException(msg);
		}
	}

	public void validateUserStoryWhenEdit(FacesContext context, UIComponent validate, Object value) throws ValidatorException {
		final Long userStoryId = this.userStory.getUserStoryId();
		final UserStory currentUserStory = this.userStoryService.findUserStoryById(userStoryId);
		final UserStory newUserStory = this.userStoryService.checkExistUserStory(Utils.standardizeString(value.toString()), this.userStory.getProject().getProjectId());
		if ((newUserStory != null) && !(currentUserStory.getName().equals(Utils.standardizeString(value.toString())))) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.Exists", null));
			throw new ValidatorException(msg);
		}
		if (utils.isExistTooLongWord(value.toString())) {
			@SuppressWarnings("static-access")
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.backlog.LongestLength", null));
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

	public void goToViewUserStoryPage() {
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/userstory/viewDetail" + "?userStory=" + userStory.getUserStoryId()+"#";
		JSFUtils.redirect(contextPath);
	}

	public int findProgressOfIssue(long issueID) {
		return this.issueService.findProgressOfIssue(issueID);
	}

	/**
	 * Find point (estimate or remain) of issue
	 * 
	 * @param issue
	 * @param estimate
	 * @return string value of point
	 */
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

	/**
	 * Show fullname or email of owner
	 * 
	 * @param member
	 * @return
	 */
	@SuppressWarnings("static-access")
	public String getAssignedMember(Member member) {
		try {
			return this.utils.checkName(member);
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Handle uploading excel file step 1. get the uploaded excel file step 2.
	 * get data from the file (step 1) step 3. create user stories from the
	 * data(step 2) step 4. delete the uploaded file step 5. update the current
	 * list of user stories
	 * 
	 * @throws IOException
	 */
	public void handleUploadExcelFile(String uploadExelFile) throws IOException {
		// get the uploaded excel file
		String uploadFileName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(uploadExelFile);
		String realFileName = ATTACHMENT_PATH + uploadFileName;

		// get data from file
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		data.addAll(getDataFromUploadedExcelFile(realFileName));

		// create user story from excel data
		createUserStoryFromExcelData(data);

		// delete temp file
		MyAgileFileUtils.deleteFile(realFileName);

		// update user story list
		updateUserStoryList();
	}

	/**
	 * read data from excel file was uploaded
	 * 
	 * @param uploadFileName
	 * @return ArrayList<ArrayList<String>>
	 * @throws IOException
	 */
	private ArrayList<ArrayList<String>> getDataFromUploadedExcelFile(String uploadFileName) throws IOException {
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		if (uploadFileName.toLowerCase().endsWith("xlsx")) {
			data.addAll(exportImportFileService.readExcel(uploadFileName, XLSXFORMAT));
		} else {
			data.addAll(exportImportFileService.readExcel(uploadFileName, XLSFORMAT));
		}
		return data;
	}

	/**
	 * create user story from excel file
	 * 
	 * @param data
	 *            , ArrayList<ArrayList<String>>
	 */
	@SuppressWarnings("static-access")
	private void createUserStoryFromExcelData(ArrayList<ArrayList<String>> data) {
		RequestContext requestContext = RequestContext.getCurrentInstance();

		if (getProjectId() == 0) {
			requestContext.addCallbackParam("numRows", this.utils.getMessage("myagile.backlog.NoProject", null));
			requestContext.addCallbackParam("result", ERROR_MESSAGE_TYPE);
			return;
		}

		int numberOfRow = data.size();

		if (numberOfRow == 0) {
			requestContext.addCallbackParam("numRows", this.utils.getMessage("myagile.backlog.DataEmpty", null));
			requestContext.addCallbackParam("result", ERROR_MESSAGE_TYPE);
			return;
		}

		int numberOfColum = data.get(FIRST_ROW_INDEX).size();

		if (numberOfColum > NUMBER_OF_COLUNM) {
			Object[] params = { NUMBER_OF_COLUNM };
			requestContext.addCallbackParam("numRows", this.utils.getMessage("myagile.backlog.IncorrectMaxColunms", params));
			requestContext.addCallbackParam("result", ERROR_MESSAGE_TYPE);
			return;
		}

		for (int i = 0; i < numberOfColum; i++) {
			ArrayList<String> headerOfTable = data.get(FIRST_ROW_INDEX);
			if (headerOfTable.get(i).equals("") || headerOfTable.get(i) == null) {
				requestContext.addCallbackParam("numRows", "Not correct format! Name of column " + (i + 1) + " is empty!");
				requestContext.addCallbackParam("result", ERROR_MESSAGE_TYPE);
				return;
			}
		}

		ArrayList<String> messages = new ArrayList<String>();
		int totalRowSuccess = 0;
		for (int i = FIRST_ROW_INDEX + 1; i < numberOfRow; i++) {
			try {
				UserStory usrStory = new UserStory();
				String name = "";
				String description = "";
				int value = 0;
				int risk = 0;
				String priority = UserStory.PriorityType.NONE.toString();

				ArrayList<String> row = new ArrayList<String>(numberOfColum);
				for (int j = 0; j < numberOfColum; j++) {
					row.add("");
					if (j >= data.get(i).size()) {
						continue;
					}
					row.set(j, data.get(i).get(j));
				}
				switch (numberOfColum) {
				case 1:
					name = loadUserStoryNameFromExcelData(row);
					break;
				case 2:
					name = loadUserStoryNameFromExcelData(row);
					description = loadUserStoryDescFromExcelData(row);
					break;
				case 3:
					name = loadUserStoryNameFromExcelData(row);
					description = loadUserStoryDescFromExcelData(row);
					value = loadUserStoryValueFromExcelData(row);
					break;
				case 4:
					name = loadUserStoryNameFromExcelData(row);
					description = loadUserStoryDescFromExcelData(row);
					value = loadUserStoryValueFromExcelData(row);
					risk = loadUserStoryRiskFromExcelData(row);
					break;
				default:
					name = loadUserStoryNameFromExcelData(row);
					description = loadUserStoryDescFromExcelData(row);
					value = loadUserStoryValueFromExcelData(row);
					risk = loadUserStoryRiskFromExcelData(row);
					priority = loadUserStoryPriorityFromExcelData(row);
					break;
				}

				usrStory.setStatus(UserStory.StatusType.TODO);
				usrStory.setProject(this.projectService.findProjectById(getProjectId()));
				usrStory.setName(name);
				usrStory.setDescription(description);
				usrStory.setValue(value);
				usrStory.setRisk(risk);
				usrStory.setPriority(UserStory.PriorityType.valueOf(priority));

				if (this.userStoryService.create(usrStory)) {
					totalRowSuccess++;
				} else {
					throw new Exception();
				}
			} catch (IllegalArgumentException e) {
				switch (errorType) {
				case RISK_ERROR:
					messages.add("User story risk of row " + (i + 1) + " must be in range 0 - 1000");
					break;
				case VALUE_ERROR:
					messages.add("User story value of row " + (i + 1) + " must be in range 0 - 1000");
					break;
				case PRIORITY_ERROR:
					messages.add("User story prioprity of row " + (i + 1) + " is not correct format (MoSCoW)!");
					break;
				}
			} catch (NameAlreadyBoundException e) {
				messages.add("User story name of row " + (i + 1) + " is duplicate!");
			} catch (NameRangeException e) {
				messages.add("User story name of row " + (i + 1) + " is too long!");
			} catch (NameNotFoundException e) {
				messages.add("User story name of row " + (i + 1) + " is blank!");
			} catch (Exception e) {
				requestContext.addCallbackParam("numRows", "Cannot create user story from the file!");
				requestContext.addCallbackParam("result", ERROR_MESSAGE_TYPE);
			}
		}

		if (totalRowSuccess > 1) {
			requestContext.addCallbackParam("numRows", totalRowSuccess + " user stories have been added");
		} else {
			requestContext.addCallbackParam("numRows", totalRowSuccess + " user story has been added");
		}

		if (messages.size() == 0) {
			requestContext.addCallbackParam("result", SUCCESS_MESSAGE_TYPE);
		} else {
			requestContext.addCallbackParam("messageExcelUpload", new JSONArray(messages, true).toString());
			requestContext.addCallbackParam("result", WARN_MESSAGE_TYPE);
		}

	}

	private String loadUserStoryPriorityFromExcelData(ArrayList<String> row) throws IllegalArgumentException {
		String prioprity = row.get(PRIORITY);
		if (!this.utils.isBelongToUserStoryPriorityValues(prioprity)) {
			errorType = PRIORITY_ERROR;
			throw new IllegalArgumentException();
		}
		return prioprity.toUpperCase();
	}

	private int loadUserStoryRiskFromExcelData(ArrayList<String> row) throws IllegalArgumentException {
		String riskValue = row.get(RISK);
		if (!utils.isValueOfRange(riskValue, 0, MAX_RISK_VALUE)) {
			errorType = RISK_ERROR;
			throw new IllegalArgumentException();
		}
		return Integer.parseInt(riskValue);
	}

	private int loadUserStoryValueFromExcelData(ArrayList<String> row) throws IllegalArgumentException {
		String value = row.get(VALUE);
		if (!utils.isValueOfRange(value, 0, MAX_BUSSINESS_VALUE)) {
			errorType = VALUE_ERROR;
			throw new IllegalArgumentException();
		}
		return Integer.parseInt(value);
	}

	private String loadUserStoryDescFromExcelData(ArrayList<String> row) {
		return row.get(DESCRIPTION);
	}

	private String loadUserStoryNameFromExcelData(ArrayList<String> row) throws NameAlreadyBoundException, NameNotFoundException, NameRangeException {
		String nameTemp = row.get(NAME);
		if (nameTemp.length() > MAX_LENGTH) {
			throw new NameRangeException();
		} else {
			if (nameTemp.trim().length() == 0) {
				throw new NameNotFoundException();
			} else {
				if (this.userStoryService.checkExistUserStory(nameTemp, getProjectId()) != null) {
					throw new NameAlreadyBoundException();
				}
			}
		}
		return nameTemp;
	}

	public UserStory getUserStory() {
		if (this.userStory == null) {
			this.userStory = new UserStory();
			this.userStory.setProject(this.projectService.findProjectById(this.projectId));
		}
		return this.userStory;
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

	public long getProjectId() {
		return this.projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public UserStory getSelectedUserStory() {
		return this.selectedUserStory;
	}

	public void setSelectedUserStory(UserStory selectedUserStory) {
		this.selectedUserStory = selectedUserStory;
	}

	public List<UserStory> getUserStoryList() {
		if (this.userStoryList == null) {
			this.userStoryList = new ArrayList<UserStory>();
		}
		return this.userStoryList;
	}

	public void setUserStoryList(List<UserStory> userStoryList) {
		this.userStoryList = userStoryList;
		setOtherValueForCurrentUserStory();
	}

	public List<UserStory> getFilteredUserStories() {
		return this.filteredUserStories;
	}

	public void setFilteredUserStories(List<UserStory> filteredUserStories) {
		this.filteredUserStories = filteredUserStories;
	}

	public List<Project> getProjectList() {
		if (this.projectList == null) {
			this.projectList = new ArrayList<Project>();
		}
		return this.projectList;
	}

	public void setProjectList(List<Project> projectList) {
		this.projectList = projectList;
	}

	public boolean isEdit() {
		return this.edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public Integer getProgressOfCurrentUserStory() {
		return this.progressOfCurrentUserStory;
	}

	public void setProgressOfCurrentUserStory(Integer progressOfCurrentUserStory) {
		this.progressOfCurrentUserStory = progressOfCurrentUserStory;
	}

	public List<Attachment> getNotAddedAttachmentList() {
		if (this.notAddedAttachmentList == null) {
			this.notAddedAttachmentList = new ArrayList<Attachment>();
		}
		return this.notAddedAttachmentList;
	}

	public void setNotAddedAttachmentList(List<Attachment> notAddedAttachmentList) {
		this.notAddedAttachmentList = notAddedAttachmentList;
	}

	public Long getNotAddAttachmentId() {
		return this.notAddAttachmentId;
	}

	public void setNotAddAttachmentId(Long notAddAttachmentId) {
		this.notAddAttachmentId = notAddAttachmentId;
	}

	public List<Attachment> getAttachmentList() {
		return this.attachmentList;
	}

	public void setAttachmentList(List<Attachment> attachmentList) {
		this.attachmentList = attachmentList;
	}

	public Long getSelectedAttachmentId() {
		return this.selectedAttachmentId;
	}

	public void setSelectedAttachmentId(Long selectedAttachmentId) {
		this.selectedAttachmentId = selectedAttachmentId;
	}

	public Attachment getAttachment() {
		return this.attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public boolean isCreateMode() {
		return this.createMode;
	}

	public void setCreateMode(boolean createMode) {
		this.createMode = createMode;
	}

	public List<UserStory.StatusType> getSelectedFilter() {
		return this.selectedFilter;
	}

	public void setSelectedFilter(List<UserStory.StatusType> selectedFilter) {
		this.selectedFilter = selectedFilter;
		updateUserStoryList();
	}

	public UserStory.StatusType getTempStatus() {
		return this.tempStatus;
	}

	public void setTempStatus(UserStory.StatusType tempStatus) {
		this.tempStatus = tempStatus;
	}

	public List<Issue> getRelatedIssues() {
		return this.relatedIssues;
	}

	public void setRelatedIssues(List<Issue> relatedIssues) {
		this.relatedIssues = relatedIssues;
	}

	public List<UserStory> getSelectedUserStories() {
		return selectedUserStories;
	}

	public void setSelectedUserStories(List<UserStory> selectedUserStories) {
		this.selectedUserStories = selectedUserStories;
	}

	public boolean isSelectedAllUserStory() {
		return selectedAllUserStory;
	}

	public void setSelectedAllUserStory(boolean selectedAllUserStory) {
		this.selectedAllUserStory = selectedAllUserStory;
	}
	
}
