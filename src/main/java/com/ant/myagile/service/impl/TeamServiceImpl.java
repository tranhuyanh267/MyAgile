package com.ant.myagile.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.TeamDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.HistoryService;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;

@Service
@Transactional
public class TeamServiceImpl implements TeamService 
{
	@Autowired
	private TeamDao teamDao;
	@Autowired
	private Utils utils;
	@Autowired
	private MailService mailService;
	@Autowired
	private HistoryService historyService;
	
	private final String fileUploadTempDir = MyAgileFileUtils.getStorageLocation("myagile.upload.temp.location");
	private final String imageTeamFolder = MyAgileFileUtils.getStorageLocation("myagile.upload.image.team.folder");

	@Override
	public boolean save(Team team) 
	{
		if (this.teamDao.save(team)) 
		{
			this.historyService.saveHistoryForNewTeam(team);
			Date currentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, MMM d, yyyy HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getDefault());
			Object[] detail = { team.getTeamName(),
					this.utils.getLoggedInMember().getUsername(),
					dateFormat.format(currentTime) };
			SendMailThread sendMailThread = new SendMailThread(mailService, "sendToAdmin",
					Utils.getMessage(
							"myagile.email.createTeamNotification.Subject",
							null), Utils.getMessage(
							"myagile.email.createTeamNotification.Content",
							detail));
			sendMailThread.start();
			
		}
		return true;
	}

	@Override
	public boolean update(Team team) 
	{
		this.utils.evict(team);
		Team oldTeam = this.teamDao.findTeamByTeamId(team.getTeamId());
		this.historyService.saveHistoryForUpdateTeam(oldTeam, team);
		this.utils.evict(oldTeam);
		return teamDao.update(team);
	}

	@Override
	public boolean delete(Team team) 
	{
		if (teamDao.delete(team)) 
		{
			Date currentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, MMM d, yyyy HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getDefault());
			Object[] detail = { team.getTeamName(),
					this.utils.getLoggedInMember().getUsername(),
					dateFormat.format(currentTime) };
			
			SendMailThread sendMailThread = new SendMailThread(mailService, "sendToAdmin",
					Utils.getMessage(
							"myagile.email.removeTeamNotification.Subject", null), Utils.getMessage(
									"myagile.email.removeTeamNotification.Content",
									detail));
			sendMailThread.start();
			return true;
		}
		return false;
		
	}

	@Override
	public Team findTeamByTeamId(long teamId) {
		return teamDao.findTeamByTeamId(teamId);
	}

	@Override
	@Deprecated
	public List<Team> findTeamsbyProjectIdAndMemberId(long projectId,
			long memberId) {
		return teamDao.findTeamsbyProjectIdAndMemberId(projectId, memberId);
	}

	@Override
	public List<Team> findTeamsByProjectIdAndMemberId(long projectId,
			long memberId) {
		return teamDao.findTeamsByProjectIdAndMemberId(projectId, memberId);
	}

	@Override
	public List<Team> findTeamsByMember(Member member) {
		return teamDao.findTeamsByMember(member);
	}

	@Override
	public List<Team> findTeamsByOwner(Member owner) {
		return teamDao.findTeamsByOwner(owner);
	}

	@Override
	public List<Team> findTeamsByMemberAndOwner(Member loggedInMember)
	{
		List<Team> teams = new ArrayList<Team>();
		List<Team> teamsMember = teamDao.findTeamsByMember(loggedInMember);
		List<Team> teamsOwner = teamDao.findTeamsByOwner(loggedInMember);
		teams.addAll(teamsMember);
		teams.addAll(teamsOwner);
		return teams;
	}

	@Override
	public List<Team> findTeamsByProjectId(long projectId) {
		return teamDao.findTeamsbyProjectId(projectId);
	}

	@Override
	@Transactional
	public List<Team> findTeamsByProject(Project p) {
		return teamDao.findTeamsByProject(p);
	}

	@Override
	public String teamImageNameProcess(String teamName) 
	{
		return MyAgileFileUtils.fileNameProcess(teamName);
	}

	@Override
	public String makeTeamDir(Team team) 
	{
		File newTeamFolder = new File(imageTeamFolder + "/T" + team.getTeamId());
		if (!newTeamFolder.exists()) {
			newTeamFolder.mkdirs();
		}
		return newTeamFolder.getPath();
	}

	@Override
	public void moveLogo(Team team, String filename) 
	{
		String tempPath = fileUploadTempDir + "/teams/" + filename;
		String filePath = makeTeamDir(team);
		File f = new File(tempPath);
		if (f.exists()) {
			f.renameTo(new File(filePath + "/" + filename));
		}
	}

	@Override
	public void deleteLogo(Team team, String filename)
	{
		MyAgileFileUtils.deleteFile(imageTeamFolder + "/T" + team.getTeamId() + "/" + filename);
	}

	@Override
	public void deleteTempLogo(String filename) 
	{
		MyAgileFileUtils.deleteFile(fileUploadTempDir + "/teams/" + filename);
	}

	@Override
	public String renameLogo(String oldFileName, String newFileName)
	{
		return MyAgileFileUtils.renameFile(fileUploadTempDir + "/teams/",
				oldFileName, newFileName);
	}

	@Override
	public boolean checkMailGroupExisted(Team newTeam)
	{
		return teamDao.checkMailGroupExisted(newTeam);
	}

	@Override
	public boolean checkTeamNameExisted(Team newTeam) 
	{
		return teamDao.checkTeamNameExisted(newTeam);
	}

	@Override
	public int countTeamByProject(Long projectId) 
	{
		return teamDao.countTeamByProject(projectId);
	}

	@Override
	public List<Team> findAllTeam()
	{
		return teamDao.findAllTeam();
	}

	@Override
	public Team findTeamByGroupMail(String mailGroup)
	{
		return teamDao.findTeamByGroupMail(mailGroup);
	}

	@Override
	public List<Team> findAllTeamsByProjectId(long projectId) 
	{
	    return teamDao.findAllTeamsbyProjectId(projectId);
	}
}
