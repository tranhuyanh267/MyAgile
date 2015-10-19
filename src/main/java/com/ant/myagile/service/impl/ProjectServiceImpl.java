package com.ant.myagile.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.FeedbackDao;
import com.ant.myagile.dao.IssueDao;
import com.ant.myagile.dao.LastUserStoryIndexDao;
import com.ant.myagile.dao.PointRemainDao;
import com.ant.myagile.dao.ProjectDao;
import com.ant.myagile.dao.RetrospectiveResultDao;
import com.ant.myagile.dao.TeamProjectDao;
import com.ant.myagile.dao.UserStoryDao;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Feedback;
import com.ant.myagile.model.Issue;
import com.ant.myagile.model.LastUserStoryIndex;
import com.ant.myagile.model.PointRemain;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.RetrospectiveResult;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService 
{
    	private static final Logger logger = Logger.getLogger(ProjectServiceImpl.class);
    	
	@Autowired
	SessionFactory sessionFactory;
	@Autowired
	private ProjectDao projectDao;
	@Autowired
	private IssueDao issueDao;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private PointRemainDao pointRemainDAO;
	@Autowired
	private UserStoryDao userStoryDAO;
	@Autowired
	private FeedbackDao feedbackDAO;
	@Autowired
	private LastUserStoryIndexDao lastUserStoryIndexDAO;
	@Autowired
	private RetrospectiveResultDao retroResultDAO;
	@Autowired
	private TeamProjectDao teamProjectDAO;
	@Autowired
	private Utils utils;
	@Autowired
	private MailService mailService;
	private final String fileUploadTempDir = MyAgileFileUtils.getStorageLocation("myagile.upload.temp.location");
	private String imageProjectFolder = MyAgileFileUtils.getStorageLocation("myagile.upload.image.project.folder");

	@Override
	public boolean save(Project project) 
	{
		if (projectDao.save(project)) 
		{
			historyService.saveHistoryForNewProject(project);
			Date currentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, MMM d, yyyy HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getDefault());
			Object[] detail = { project.getProjectName(),
					this.utils.getLoggedInMember().getUsername(),
					dateFormat.format(currentTime) };
			SendMailThread sendMailThread = new SendMailThread(mailService, "sendToAdmin",
					Utils.getMessage(
							"myagile.email.createProjectNotification.Subject",
							null),								Utils.getMessage(
									"myagile.email.createProjectNotification.Content",
									detail));
			sendMailThread.start();
			return true;
		}
		return false;
	}

	@Override
	public List<Project> findByOwnerId(Long ownerId) 
	{
		return this.projectDao.findByOwnerId(ownerId);
	}

	@Override
	public boolean delete(Project project) 
	{
		if (this.projectDao.delete(project)) 
		{
			Date currentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, MMM d, yyyy HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getDefault());
			Object[] detail = { project.getProjectName(),
					this.utils.getLoggedInMember().getUsername(),
					dateFormat.format(currentTime) };
			SendMailThread sendMailThread = new SendMailThread(mailService, "sendToAdmin",
					Utils.getMessage(
							"myagile.email.removeProjectNotification.Subject",
							null),  Utils.getMessage(
									"myagile.email.removeProjectNotification.Content",
									detail));
			sendMailThread.start();
			return true;
		}
		return false;
	}

	@Override
	public List<Project> findAllProjects() 
	{
		return this.projectDao.listAllproject();
	}

	@Override
	public boolean update(Project project)
	{
		this.utils.evict(project);
		Project oldProject = this.projectDao.findById(project.getProjectId());
		this.historyService.saveHistoryForUpdateProject(oldProject, project);
		this.utils.evict(oldProject);
		return this.projectDao.update(project);
	}
	
	@Override
	public void updateWithoutHistory(Project project)
	{
		this.utils.evict(project);
		this.projectDao.update(project);
	}

	@Override
	@Deprecated
	public boolean createProjects(Project project) 
	{
		return this.projectDao.save(project);
	}

	@Override
	public Project findProjectById(long projectId) 
	{
		return this.projectDao.findById(projectId);
	}

	@Override
	public Project checkExistProject(String projectName, long ownerId)
	{
		return this.projectDao.checkExistProject(projectName, ownerId);
	}

	@Override
	public String projectImageNameProcess(String projectName)
	{
		return MyAgileFileUtils.fileNameProcess(projectName);
	}

	@Override
	public boolean deleteLogo(String filename) 
	{
		return MyAgileFileUtils.deleteFile(imageProjectFolder + "/" + filename);
	}

	@Override
	public String renameLogo(String oldFileName, String newFileName)
	{
		return MyAgileFileUtils.renameFile(
				this.fileUploadTempDir + "/projects", oldFileName, newFileName);
	}

	@Override
	public boolean deleteLogoRealPath(String fileName) 
	{
		return MyAgileFileUtils.deleteFile(fileName);
	}

	@Override
	public boolean moveFileFromTempDirectoryToRealPath(String newFileName, String oldFileName, String path) 
	{
		File oldFile = new File(oldFileName);
		File newFile = new File(newFileName);
		File newPath = new File(path);
		if (oldFile.exists()) {
			if (!newPath.exists()) {
				newPath.mkdirs();
			}
		}
		return oldFile.renameTo(newFile);
	}

	@Override
	public List<Project> findByMemberId(Long memberId)
	{
		return this.projectDao.findByMemberId(memberId);
	}

	@Override
	public List<Project> findByMemberAndOwner(Long memberId) 
	{
		List<Project> projectsByMemberId = findByMemberId(memberId);
		List<Project> projectsByOwnerId = findByOwnerId(memberId);

		// Merge two project lists
		for (int i = 0; i < projectsByOwnerId.size(); i++) 
		{
			boolean flag = true;
			for (int j = 0; j < projectsByMemberId.size(); j++) {
				if (projectsByMemberId.get(j).getProjectId()
						.equals(projectsByOwnerId.get(i).getProjectId())) {
					flag = false;
				}
			}
			if (flag) {
				projectsByMemberId.add(projectsByOwnerId.get(i));
			}
		}
		
		 Collections.sort(projectsByMemberId,Project.ProjectNameComparator);
		 return projectsByMemberId;
	}

	@Override
	public List<Project> findAllProjectsByTeam(Team t) 
	{
		return this.projectDao.findAllProjectsByTeam(t);

	}

	@Override
	public List<Project> findProjectsAssignedToTeam(Team t) 
	{
		return this.projectDao.findProjectsAssignedToTeam(t);
	}

	@Override
	public Project findProjectOfIssue(Issue issue) 
	{
		Project p = new Project();
		if (issue.getParent() == null) {
			p = issue.getUserStory().getProject();
		} else {
			Issue parentIssue = this.issueDao.findIssueById(issue.getParent()
					.getIssueId());
			p = parentIssue.getUserStory().getProject();
		}
		return p;
	}

	@Override
	public List<Project> filterTeamProjectsWithMemberId(Long teamId, Long memberId) 
	{
		ArrayList<Project> filteredProject = new ArrayList<Project>();

		Team team = this.teamService.findTeamByTeamId(teamId);
		List<Project> projectListByTeam = this.findAllProjectsByTeam(team);
		for (int i = 0; i < projectListByTeam.size(); i++) 
		{
			Project projectEach = projectListByTeam.get(i);

			if (projectEach.getOwner().getMemberId() == memberId) {
				filteredProject.add(projectEach);
			}
		}

		if (filteredProject.size() > 0) {
			return filteredProject;
		} else {
			return projectListByTeam;
		}
	}

	@Override
	@Transactional
	public boolean deleteProjectAndAllContents(long projectId)
	{
		try {
			/* find and delete all User Story belong to Project */
			List<UserStory> listUserStory = userStoryDAO
					.findAllUserStoryByProjectId(projectId);
			for (UserStory userStory : listUserStory) {
				/* find and delete all Issue belong to User Story */
				List<Issue> listIssue = issueDao
						.findIssuesByUserStory(userStory.getUserStoryId());
				if (listIssue != null) {
					for (Issue issue : listIssue) {
						/*
						 * find and delete all Issue children belong to another
						 * Issue
						 */
						List<Issue> listIssueChildren = issueDao
								.findIssueByParent(issue);
						if (!listIssueChildren.isEmpty()) {
							for (Issue issueChildren : listIssueChildren) {
								/*
								 * find and delete all Point Remain belong to
								 * Issue children
								 */
								List<PointRemain> listPointRemainChildren = pointRemainDAO
										.findPointRemainByIssueId(issueChildren
												.getIssueId());
								for (PointRemain pointRemain : listPointRemainChildren) {
									pointRemainDAO.delete(pointRemain);
								}
								issueDao.deleteIssue(issueChildren);
							}
						}
						/* find and delete all Point Remain belong to Issue */
						List<PointRemain> listPointRemain = pointRemainDAO
								.findPointRemainByIssueId(issue.getIssueId());
						for (PointRemain pointRemain : listPointRemain) {
							pointRemainDAO.delete(pointRemain);
						}
						issueDao.deleteIssue(issue);
					}
				}
				userStoryDAO.deleteUserStory(userStory.getUserStoryId());
			}
			/* find and delete all Feedback belong to project */
			List<Feedback> listFeedback = feedbackDAO
					.findFeedbacksByProjectId(projectId);
			for (Feedback feedback : listFeedback) {
				feedbackDAO.deleteFeedback(feedback);
			}
			/* find and delete lastUserStoryIndex belong to project */
			LastUserStoryIndex lastUserStoryIndex = lastUserStoryIndexDAO
					.findLastUserStoryIndexByProjectId(projectId);
			if (lastUserStoryIndex != null) {
				lastUserStoryIndexDAO.delete(lastUserStoryIndex);
			}
			/* find and delete all RetrospectiveResult belong to project */
			List<RetrospectiveResult> listRetrospectiveResult = retroResultDAO
					.findRetrospectiveByProject(projectId);
			for (RetrospectiveResult retrospectiveResult : listRetrospectiveResult) {
				retroResultDAO.deleteRetrospective(retrospectiveResult.getId());
			}

			Project proj = projectDao.findById(projectId);
			/* delete Image and Folder Directory of project */
			this.deleteLogo("P" + proj.getProjectId() + "/"
					+ proj.getImagePath());
			MyAgileFileUtils.deleteFile(imageProjectFolder + "/P"
					+ proj.getProjectId());
			/* delete Project */
			 if( projectDao.delete(proj)){
				Date currentTime = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"EEE, MMM d, yyyy HH:mm:ss");
				dateFormat.setTimeZone(TimeZone.getDefault());
				Object[] detail = {proj.getProjectName(),
						this.utils.getLoggedInMember().getUsername(),
						dateFormat.format(currentTime) };
				try {
					mailService.sendMailToAdmin(Utils.getMessage(
							"myagile.email.removeProjectNotification.Subject",
							null), Utils.getMessage(
							"myagile.email.removeProjectNotification.Content",
							detail));
				} catch (Exception e) {
					return true;
				}
			} else return false;
		} catch (Exception ex) {
		    	logger.error("deleteProjectAndAllContents error: " + ex.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public List<Project> findAllByMemberId(Long memberId)
	{
		return projectDao.findAllByMemberId(memberId);
	}

	@Override
	public boolean setFalseArchivedOfProject() 
	{
		try {
			String hql = "update Project as p set p.isArchived=false where p.isArchived is null";
			sessionFactory.getCurrentSession()
			.createQuery(hql).executeUpdate();
			return true;
		} catch (HibernateException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<Project> findLazyProjects(StateLazyLoading lazyLoadingProject)
	{
		return projectDao.findLazyProjects(lazyLoadingProject);
	}

	@Override
	public List<Project> findLazyByMemberId(Long idUserForProject, StateLazyLoading lazyLoadingUserProject) 
	{
		return projectDao.findLazyByMemberId(idUserForProject,lazyLoadingUserProject);
	}

	@Override
	public int countTotalLazyProjects(StateLazyLoading lazyLoadingProject)
	{
		return projectDao.countTotalLazyProjects(lazyLoadingProject);
	}

	@Override
	public int countTotalLazyByMemberId(Long idUserForProject, StateLazyLoading lazyLoadingUserProject) 
	{
		return projectDao.countTotalLazyByMemberId(idUserForProject,lazyLoadingUserProject);
	}

	@Override
	public List<Project> findByProductOwnerId(Long productOwnerId) 
	{
	    return projectDao.findByProductOwnerId(productOwnerId);
	}
}
