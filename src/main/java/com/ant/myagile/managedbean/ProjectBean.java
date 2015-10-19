package com.ant.myagile.managedbean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.model.TeamProject;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.TeamProjectService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.StringUtils;
import com.ant.myagile.utils.Utils;

@Component("projectBean")
@Scope("session")
public class ProjectBean implements Serializable 
{
	/**
	 * Static variables
	 */
	private static final long serialVersionUID = 4316465578316069131L;
	
	private static final int MIN_PROJECT_NAME = 6;
	private static final int MAX_PROJECT_NAME = 255;
	private static final int MAX_DESCRIPTION = 500;
	
	private static Logger logger = Logger.getLogger(ProjectBean.class.getName());

	@Autowired
	private Utils utils;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private TeamProjectService teamProjectService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private MailService mailService;
	@Autowired
	private ProductBacklogBean productBacklogBean;

	private Project project;
	private List<Project> projects;
	private Long projectId;
	private String projectName;
	private String description;
	private String imagePath;
	private Boolean isPublic;
	private Member owner;
	private List<Team> teams;
	private List<Long> selectedTeams;
	private List<Long> selectedTeamsUpdate;
	private String editMode = "false";
	private String nameLogoUpdated;
	private List<Project> selectedProject;
	private int numRow;
	private String projectNameOld = "";
	private final String fileUploadRootDir = MyAgileFileUtils.getStorageLocation("myagile.upload.root.location");
	private final String imageProjectFolder = "/projects";
	private final String fileUploadTempDir = MyAgileFileUtils.getStorageLocation("myagile.upload.temp.location");
	private Project selectedOneProject;
	private List<Long> selectedPOsForProject;
	private List<Member> allActiveUsers;
	private boolean projectSavedSuccessful = false;

	public ProjectBean() {
	}
	
	public List<Project> getProjectByMemberId(Long memberId)
	{
		return projectService.findAllByMemberId(memberId);
	}
	
	
	/**
	 * Replace old logo of project by a new one. Besides, delete the old.
	 */
	public void updateProjectLogo()
	{
		String filename = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("filename");
		String newFileName = this.projectService.projectImageNameProcess(this.project.getProjectName());
		this.projectService.deleteLogoRealPath(this.fileUploadRootDir + "/" + this.imageProjectFolder + "/P" + this.project.getProjectId() + "/" + this.project.getImagePath());
		String newLogoFilename = this.projectService.renameLogo(filename, newFileName);
		this.project.setImagePath(newLogoFilename);
		this.projectService.moveFileFromTempDirectoryToRealPath(this.fileUploadRootDir + this.imageProjectFolder + "/P" + this.project.getProjectId() + "/" + newLogoFilename, this.fileUploadTempDir
				+ this.imageProjectFolder + "/" + newLogoFilename, this.fileUploadRootDir + this.imageProjectFolder + "/P" + this.project.getProjectId());
		this.projectService.update(this.project);
	}

	/**
	 * Upload logo for a new Project in temp folder. If new project saved, image
	 * file will be moved to project folder. Otherwise, it will be deleted.
	 */
	public void newProjectLogo() 
	{
		String filename = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("filenameNew");
		String newFileName = this.projectService.projectImageNameProcess(FilenameUtils.removeExtension(filename));
		this.projectService.deleteLogo(this.fileUploadTempDir + this.imageProjectFolder + "/" + getNameLogoUpdated());
		String newLogoFilename = this.projectService.renameLogo(filename, newFileName);
		setNameLogoUpdated(newLogoFilename);
		setImagePath(newLogoFilename);
	}

	/**
	 * Create a new Project, assign Team to do Project
	 */
	public void addProjectWizard()
	{
		saveProjectIntoDB();
	}
	

	public void addProject() 
	{
	    	projectSavedSuccessful = false; //default value 
		saveProjectIntoDB();
		// Redirect to project page
		String projectContextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/project";
		HttpServletRequest origRequest = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
		StringBuffer requestURL = origRequest.getRequestURL();
		String contextPath = requestURL.substring(0, requestURL.indexOf(origRequest.getRequestURI())) + projectContextPath;
		projectSavedSuccessful = true;
		JSFUtils.redirect(contextPath);
	}
	
	public void saveProjectIntoDB() 
	{
		try 
		{
			UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
			UIComponent component = viewRoot.findComponent("list-update-project:projectUpdate");
			boolean flag = false;
			//FacesMessage msg = null;
			int oldSize = this.projectService.findAllProjects().size();
			String newLogoFileName = "";
			RequestContext requestContext = RequestContext.getCurrentInstance();
			
			if (getImagePath() != null) 
			{
				String newFileName = this.projectService.projectImageNameProcess(getProjectName());
				newLogoFileName = this.projectService.renameLogo(getImagePath(), newFileName);
			}
			// Add project
			Project projectTemp = new Project();
			
			//Remove Control Characters
			projectTemp.setDescription(getDescription().trim().replaceAll("\\p{Cntrl}", ""));
			projectTemp.setProjectName(getProjectName().replaceAll("\\p{Cntrl}", ""));
			this.projectNameOld = projectTemp.getProjectName();
			
			projectTemp.setIsPublic(getIsPublic());
			projectTemp.setImagePath(newLogoFileName);
			projectTemp.setOwner(this.utils.getLoggedInMember());
			this.projectService.save(projectTemp);
			this.projectService.moveFileFromTempDirectoryToRealPath(this.fileUploadRootDir + this.imageProjectFolder + "/P" + projectTemp.getProjectId() + "/" + newLogoFileName,
					this.fileUploadTempDir + this.imageProjectFolder + "/" + newLogoFileName, this.fileUploadRootDir + this.imageProjectFolder + "/P" + projectTemp.getProjectId());

			// Add TeamProject
			for (Long teamObj : getSelectedTeams())
			{
				TeamProject teamProject = new TeamProject();
				teamProject.setProject(projectTemp);
				teamProject.setStartDate(new Date());
				teamProject.setEndDate(null);
				teamProject.setTeam(this.teamService.findTeamByTeamId(teamObj));
				this.teamProjectService.create(teamProject);
				this.historyService.saveHistoryForTeamProject(teamProject, "assign", "Project");
			}

			// Show message
			if (oldSize < this.projectService.findAllProjects().size()) 
			{
				flag = true;
				//Object[] params = { projectTemp.getProjectName() };
				//msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Successful", this.utils.getMessage("myagile.project.AddSuccess", params));
			} else {
				flag = false;
				//Object[] params = { projectTemp.getProjectName() };
				//msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Fail", this.utils.getMessage("myagile.project.AddUnsuccess", params));
			}

			requestContext.addCallbackParam("saved", flag);
			if (component != null) {
				requestContext.scrollTo("list-update-project:projectUpdate");
			}
			//FacesContext.getCurrentInstance().addMessage(null, msg);
			reset();
			
		} catch (DataAccessException e) {
		    logger.error("saveProjectIntoDB: " + e.getMessage(), e.getCause());
		}
	}

	/**
	 * Check if Project name exist when creating, give back a message to view
	 * 
	 * @param context
	 *            - FacesContext
	 * @param validate
	 *            - UIComponent
	 * @param value
	 *            - Object
	 * @throws ValidatorException
	 */
	@SuppressWarnings("static-access")
	public void checkExistProject(FacesContext context, UIComponent validate, Object value) throws ValidatorException 
	{
		Project p = this.projectService.checkExistProject(value.toString().trim(), this.utils.getLoggedInMember().getMemberId());
		if (p != null) {
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.project.NameExist", null));
			throw new ValidatorException(msg);
		}
	}

	/**
	 * Check if Project name exist when updating, give back a message to view
	 * 
	 * @param context
	 *            - FacesContext
	 * @param validate
	 *            - UIComponent
	 * @param value
	 *            - Object
	 * @throws ValidatorException
	 */
	@SuppressWarnings("static-access")
	public void checkExistProjectUpdate(FacesContext context, UIComponent validate, Object value) throws ValidatorException 
	{
		Project p = this.projectService.checkExistProject(value.toString().trim(), this.utils.getLoggedInMember().getMemberId());
		if (p != null && !value.toString().trim().equals(this.project.getProjectName())) {
			FacesMessage msg = new FacesMessage(this.utils.getMessage("myagile.project.NameExist", null));
			throw new ValidatorException(msg);
		}
	}
	

  public void deleteProject() throws Exception
  {
      Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      String strId = params.get("projId");
      long id = Integer.parseInt(strId);
      Project proj = projectService.findProjectById(id);
      List<Team> listTeam = teamService.findTeamsByProjectId(id);
      boolean temp=false;
      
      try
      {
          temp = this.projectService.deleteProjectAndAllContents(id);
      }catch(Exception e){
          logger.error(e.getMessage(), e.getCause());
      }
      
      if(temp)
      {
          boolean flag = true;
          String mailContent = "  Project #" + proj.getProjectId() + ": " + proj.getProjectName()
                  + " has been deleted by " + proj.getOwner().getFirstName() + " " + proj.getOwner().getLastName() + ".<br/> "
                  + "  For more information, please contact " + proj.getOwner().getFirstName() + " " + proj.getOwner().getLastName() 
                  + " via email " + proj.getOwner().getUsername() + " .";
          
          if(!listTeam.isEmpty())
          {
              for (Team team : listTeam) 
              {
                  List<TeamMember> teamMemberList = this.memberService.findTeamMemberListByTeamId(team.getTeamId());
                  for (TeamMember teamMember : teamMemberList) 
                  {
                      if (teamMember.getMember().getUsername().equals(proj.getOwner().getUsername())) {
                          flag = false;
                      }
                      
                      this.mailService.sendMail("Inform about removing Project", mailContent, teamMember.getMember().getUsername());
                }
            }
          }
          
          if(flag){
              this.mailService.sendMail("Inform about removing Project", mailContent, proj.getOwner().getUsername());
          }
          logger.info("Delete Success");
          JSFUtils.addCallBackParam("deletes", true);
      }
      else{
          logger.info("Delete Failed");
          JSFUtils.addCallBackParam("deletes", false);
      }
  }

	public void switchToEditMode(long id) 
	{
		List<TeamProject> list = this.teamProjectService.findTeamProjectsByProjectId(id);
		List<Long> temp = new ArrayList<Long>();

		// Get list TeamProject by Project
		for (TeamProject tp : list) 
		{
			if (tp.getEndDate() == null) {
				temp.add(tp.getTeam().getTeamId());
			}
		}
		// Set selectedTeamsUpdate for show in update project form
		setSelectedTeamsUpdate(temp);

		// Set project for show in update project form
		Project p = this.projectService.findProjectById(id);
		setProject(p);

		setProjectNameOld(p.getProjectName());
		this.setEditMode("true");

		// execute function plupload
		RequestContext requestContext = RequestContext.getCurrentInstance();

		requestContext.execute("initUploader('edit')");
	}

	public void switchToViewMode() 
	{
		this.setEditMode("false");
		this.projectNameOld = "";
	}

	/**
	 * When the user click new project button, this method will be invoked to
	 * init the image uploader.
	 */
	public void newProjectInit() 
	{
		setEditMode("false");
		this.project = new Project();
		RequestContext requestContext = RequestContext.getCurrentInstance();
		requestContext.execute("initUploader('new')");
	}

	/**
	 * Update Project information and Teams which do it.
	 */
	public void updateProject()
	{
		Project p = this.projectService.checkExistProject(this.project.getProjectName().trim(), this.utils.getLoggedInMember().getMemberId());
		RequestContext context = RequestContext.getCurrentInstance();

		if (this.project.getProjectName().equals("")) {
			context.addCallbackParam("flagEmpty", true);
		} else if ((p != null) && (!getProjectNameOld().equals(this.project.getProjectName()))) {
			context.addCallbackParam("flagExist", true);
		} else if (this.project.getProjectName().length() > MAX_PROJECT_NAME) {
			context.addCallbackParam("flagLength", true);
		} else if (this.project.getProjectName().length() < MIN_PROJECT_NAME) {
			context.addCallbackParam("flagLengthLess", true);
		} else if (this.project.getDescription().length() > MAX_DESCRIPTION) {
			context.addCallbackParam("flagLengthDesc", true);
		} else {
			List<TeamProject> teamProjectsOld = this.teamProjectService.findTeamProjectsByProjectId(this.project.getProjectId());
			List<Long> teamProjectsNew = getSelectedTeamsUpdate();
			
			//Remove Control Characters 
	 		this.project.setDescription(this.project.getDescription().trim().replaceAll("\\p{Cntrl}", "")); 
	 		this.project.setProjectName(this.project.getProjectName().replaceAll("\\p{Cntrl}", "")); 
			
			this.projectService.update(this.project);

			// Remove team if teamProjectsNew not contain in teamProjectsOld
			for (TeamProject tp : teamProjectsOld) {
				if (!(teamProjectsNew.contains(tp.getTeam().getTeamId()))) {
					TeamProject teamRemove = this.teamProjectService.findTeamProjectByProjectAndTeam(this.project, tp.getTeam());
					if (teamRemove.getEndDate() == null) {
						this.historyService.saveHistoryForTeamProject(teamRemove, "remove", "Project");
						teamRemove.setEndDate(new Date());
					}
					this.teamProjectService.update(teamRemove);
				}
			}

			for (Long tp : teamProjectsNew) {
				Team t = this.teamService.findTeamByTeamId(tp);
				TeamProject editTeamProject = this.teamProjectService.findTeamProjectByProjectAndTeam(this.project, t);

				// Add team if editTeamProject not exist in database
				if (editTeamProject == null) {
					editTeamProject = new TeamProject();
					editTeamProject.setStartDate(new Date());
					editTeamProject.setEndDate(null);
					editTeamProject.setTeam(t);
					editTeamProject.setProject(this.project);
					this.teamProjectService.create(editTeamProject);
					this.historyService.saveHistoryForTeamProject(editTeamProject, "assign", "Project");
				}

				// Remove team if editTeamProject exist in database
				else {
					if (editTeamProject.getEndDate() != null) {
						this.historyService.saveHistoryForTeamProject(editTeamProject, "reassign", "Project");
					}
					editTeamProject.setEndDate(null);
					this.teamProjectService.update(editTeamProject);
				}
			}

			this.setEditMode("false");
			String str = "list-update-project:projectUpdate:" + getNumRow() + ":project-information";
			context.update(str);
			context.update("list-update-project:projectUpdate");
		}
	}

	/**
	 * Show project details when clicking expand button.
	 * 
	 * @param event
	 */
	public void onRowToggle(ToggleEvent event) 
	{
		if (event.getVisibility() == Visibility.VISIBLE) {
			Project p = (Project) event.getData();
			setNumRow(this.projects.indexOf(p));
		}
		switchToViewMode();
	}
	
	public void linkToBacklogPage() throws IOException 
	{
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/productbacklog";
		FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
	}

	public void reset() 
	{
		this.projectService.deleteLogo(this.fileUploadTempDir + this.imageProjectFolder + "/" + getNameLogoUpdated());
		UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
		UIComponent component = viewRoot.findComponent("add-project:projectGrid");
		this.projectService.deleteLogo(this.fileUploadTempDir + this.imageProjectFolder + "/" + getNameLogoUpdated());
		this.description = null;
		this.projectName = null;
		this.isPublic = false;
		this.imagePath = null;
		this.selectedTeams = null;
		this.projectSavedSuccessful = false;
		setNameLogoUpdated(null);
		if (component != null) {
			RequestContext.getCurrentInstance().reset("add-project:projectGrid");
		}
	}

	public void removeProjectMessage() 
	{
		projectNameOld = "";
		projectSavedSuccessful = false;
	}

	public List<Team> findTeamsByProject(long id) 
	{
		List<Team> list = this.teamService.findAllTeamsByProjectId(id); // all teams include hidden team
		return list;
	}

	/**
	 * Convert public status from boolean to human readable text.
	 * 
	 * @param status
	 * @return Yes/No string
	 */
	public String showPublicityStatus(boolean status) 
	{
		if (status) {
			return "Yes";
		} else {
			return "No";
		}
	}

	@SuppressWarnings("static-access")
	public String getProductOwner(Member member) 
	{
		return this.utils.checkName(member);
	}

	/**
	 * Getter and Setter
	 */
	public String getEditMode() {
		return this.editMode;
	}

	public int getNumRow() {
		return this.numRow;
	}

	public void setNumRow(int numRow) {
		this.numRow = numRow;
	}

	public void setEditMode(String editMode) {
		this.editMode = editMode;
	}

	public String getProjectNameOld() {
		return this.projectNameOld;
	}

	public void setProjectNameOld(String projectNameOld) {
		this.projectNameOld = projectNameOld;
	}

	public List<Project> getSelectedProject() {
		return this.selectedProject;
	}

	public void setSelectedProject(List<Project> selectedProject) {
		this.selectedProject = selectedProject;
	}

	public String getNameLogoUpdated() {
		return this.nameLogoUpdated;
	}

	public void setNameLogoUpdated(String nameLogoUpdated) {
		this.nameLogoUpdated = nameLogoUpdated;
	}

	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<Long> getSelectedTeamsUpdate() {
		return this.selectedTeamsUpdate;
	}

	public void setSelectedTeamsUpdate(List<Long> selectedTeamsUpdate) {
		this.selectedTeamsUpdate = selectedTeamsUpdate;
	}

	public List<Long> getSelectedTeams() {
		return this.selectedTeams;
	}

	public void setSelectedTeams(List<Long> selectedTeams) {

		this.selectedTeams = selectedTeams;
	}

	public List<Team> getTeams() 
	{
	    if (this.teams == null)
	    {
		excludeHiddenTeamsForSelection(this.teamService.findTeamsByMemberAndOwner(this.utils.getLoggedInMember()));
	    }
	
	    return this.teams;
	}

	public void setTeams(List<Team> teams) 
	{
	    excludeHiddenTeamsForSelection(teams);
	}

	public Long getProjectId() {
		return this.projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return this.projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImagePath() {
		return this.imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public Boolean getIsPublic() {
		return this.isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public Member getOwner() {
		return this.owner;
	}

	public void setOwner(Member owner)
	{
		this.owner = owner;
	}

	public List<Project> getProjects()
	{
		this.projects = projectService.findByOwnerId(this.utils.getLoggedInMember().getMemberId());
		
		if (this.projects == null){
		    this.projects = new ArrayList<Project>();
		}
		
		List<Project> projects = projectService.findByProductOwnerId(this.utils.getLoggedInMember().getMemberId());
		
		this.projects.addAll(projects);
		
		return this.projects;
	}
	
	@SuppressWarnings("static-access")
	public String getProductOwnersName(Project project)
	{
	    String result = "";
	    if (project != null && project.getProductOwnerIds() != null)
	    {
		List<Long> productOwnerIds = StringUtils.parseStringToList(project.getProductOwnerIds());
		for (Long poID : productOwnerIds) {
		    
		    Member member = memberService.findMemberById(poID);
		    
		    if (result.isEmpty()){
			result += this.utils.checkName(member);
		    } else {
			result += "; " + this.utils.checkName(member);
		    }
		}
	    }
	    
	    return result;
	}
	
	public void initDataForSelectedMembers(Project project)
	{
	    logger.debug("initDataForSelectedMembers start.........." );
	    selectedOneProject = project;
	    //initialize members
	    if (allActiveUsers == null || allActiveUsers.size() == 0)
	    {
		allActiveUsers = memberService.getAllActiveMembers();
	    }
	    logger.debug("members: " + allActiveUsers.size());
	    //current PO
	    selectedPOsForProject = StringUtils.parseStringToList(project.getProductOwnerIds());
	    selectedPOsForProject.add(project.getOwner().getMemberId());
	    logger.debug("selectedPOsForProject: " + selectedPOsForProject.toString());
	    
	}

    public Project getSelectedOneProject()
    {
        return selectedOneProject;
    }

    public void setSelectedOneProject(Project selectedOneProject)
    {
        this.selectedOneProject = selectedOneProject;
    }
    
    public List<Long> getSelectedPOsForProject() {
        return selectedPOsForProject;
    }

    public void setSelectedPOsForProject(List<Long> selectedPOsForProject) {
        this.selectedPOsForProject = selectedPOsForProject;
    }

    public void saveProductOwnersOfProject()
    {
	logger.debug("saveProductOwnersOfProject start.........");
	Project project = selectedOneProject;
	
	if (project != null)
	{
	    logger.debug("old ProductOwnersOfProject: " + project.getProductOwnerIds());
	    Long projectOwner = project.getOwner().getMemberId();
	    List<Long> projectHasPOs = selectedPOsForProject;
	    if (selectedPOsForProject.contains(projectOwner)){
		projectHasPOs.remove(projectOwner);
	    }
	    
	    String newProductOwnerIds = StringUtils.parseListToString(projectHasPOs);
	    logger.debug("new ProductOwnersOfProject: " + newProductOwnerIds);
	    project.setProductOwnerIds(newProductOwnerIds);
	    projectService.update(project);
	}
	logger.debug("saveProductOwnersOfProject start.........");
    }
    

    public List<Member> getAllActiveUsers() 
    {
	 if (allActiveUsers == null || allActiveUsers.size() == 0)
	 {
		allActiveUsers = memberService.getAllActiveMembers();
	 }
        return allActiveUsers;
    }

    public void setAllActiveUsers(List<Member> allActiveUsers) {
        this.allActiveUsers = allActiveUsers;
    }

    public boolean isArchived(long projectId)
    {
	return projectService.findProjectById(projectId).getIsArchived();
    }
    
    public boolean isProjectSavedSuccessful() {
        return projectSavedSuccessful;
    }

    public void setProjectSavedSuccessful(boolean projectSavedSuccessful) {
        this.projectSavedSuccessful = projectSavedSuccessful;
    }

    private void excludeHiddenTeamsForSelection(List<Team> allTeams)
    {
	    this.teams = new ArrayList<Team>();
	    
	    if (allTeams != null && allTeams.size() > 0)
	    {
		for (Team team : allTeams) 
		{
		    if (team != null && team.getValidTo() == null) //validTo = null => team is not hidden. 
		    {
			this.teams.add(team);
		    }
		}
	    }
	}
}
