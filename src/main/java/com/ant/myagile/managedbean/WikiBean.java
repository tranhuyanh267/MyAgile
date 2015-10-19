package com.ant.myagile.managedbean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.model.Wiki;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.service.WikiService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("wikiBean")
@Scope("session")
public class WikiBean 
{
    	private static final Logger LOGGER = Logger.getLogger(WikiBean.class);
    	
	private Team team;
	private String allTopics;
	private List<Team> teamList;
	private List<TeamMember> teamMemberList;
	private Attachment att;
	private List<Attachment> attachmentList;
	private Wiki wiki;
	private Long idTeam = 0L;
	private String contentWiki;
	private Boolean preventAccessFromUrl = true;
	private String contentWikiEdit;


	@Autowired
	Utils utils;
	@Autowired
	TeamService teamService;
	@Autowired
	MemberService memberService;

	@Autowired
	AttachmentService attachmentService;

	@Autowired
	WikiService wikiService;

	@PostConstruct
	public void init()
	{
		wiki = new Wiki();
		team = null;
	}
	
	public void redirectWiki()
	{
	    try {
			String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/wiki";
			FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
		} catch (IOException e) {
		}
	}
	
	public String getWikiHTML(long teamId)
	{
		return wikiService.convertToHtml(teamId);
	}
	
	public void setData() throws IOException 
	{
		FacesContext.getCurrentInstance().getExternalContext().redirect("wiki/edit");
	}
	
	public void checkExistWiki()
	{
		if(preventAccessFromUrl)
		{
			String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/wiki";
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
			} catch (IOException e) {
				
			}
		}
	}
	
	public void addNewWiki()
	{
	    try {
			String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/wiki/add";
			FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
		} catch (IOException e) {
		    LOGGER.error("addNewWiki error: " + e.getMessage());
		}
	}
	
	public void saveWiki()
	{
	    try 
	    {
		Wiki wk = new Wiki();
		wk.setLastModifyDate(new Date());
		wk.setLastModifyMember(this.utils.getLoggedInMember());
		wk.setContent(this.contentWiki);
		wk.setTeam(this.team);
		//check wiki exist before save
		if(!preventAccessFromUrl)
		{
			wikiService.add(wk);
			wiki =  wk;
			preventAccessFromUrl = true;
		}
		//redirect wiki
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/wiki";
		FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
	    } catch (IOException e) {
		    LOGGER.error("saveWiki error: " + e.getMessage());
	    }
	}
	
	public void updateWiki()
	{
	    try 
	    {
		this.wiki.setLastModifyDate(new Date());
		this.wiki.setLastModifyMember(this.utils.getLoggedInMember());
		wikiService.update(this.wiki);
		//redirect wiki
		String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/wiki";
		FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath);
	    } catch (IOException e) {
		LOGGER.error("updateWiki error: " + e.getMessage());
	    }
	}
	
	public void initPreview() 
	{
		try {
			this.teamMemberList = new ArrayList<TeamMember>();
			setTeamList(this.teamService.findTeamsByMemberAndOwner(this.utils.getLoggedInMember()));
			
			if (this.teamList != null && this.teamList.size()>0) 
			{
				if (this.team == null)
				{
					this.team = this.teamList.get(0);
					this.wiki = wikiService.getByTeamId(this.team.getTeamId());
					this.teamMemberList = this.memberService
								.findTeamMemberListByTeamId(this.team.getTeamId());
				}
			}
			
			attachmentList = new ArrayList<Attachment>();
			
			if (this.team == null){
				this.wiki = new Wiki();
			} else if(null != team.getTeamId()){
				// generate attachment list
				if (attachmentService.findAttachmentByWiki(this.wiki) != null){
					attachmentList = attachmentService.findAttachmentByWiki(this.wiki);
				}
			}
			
			if(this.wiki != null){
				this.contentWikiEdit = this.wiki.getContent();
			} else {
				this.contentWikiEdit = "";
			}
		} catch (Exception e) {
		    LOGGER.error("initPreview error: " + e.getMessage());
		}
		
	}

	/**
	 * Find all teams which are joined by logged in member and teams are created
	 * by logged in member
	 * 
	 * @return List of Team
	 */
	public List<Team> findTeamsByUser() 
	{
		try 
		{
			setTeamList(this.teamService.findTeamsByMemberAndOwner(this.utils.getLoggedInMember()));
		} catch (Exception ex) {
			this.setTeam(new Team());
		}
		
		return this.teamList;
	}

	public void handleTeamChange() 
	{
		try 
		{
			this.idTeam = this.team.getTeamId();
			Team team = teamService.findTeamByTeamId(this.team.getTeamId());
			this.team = team;
			this.teamMemberList = this.memberService.findTeamMemberListByTeamId(this.team.getTeamId());
			this.wiki = wikiService.getByTeamId(idTeam);
			
			if(this.wiki == null)
			{
				Wiki wk = new Wiki();
				wk.setContent("");
				wk.setLastModifyDate(new Date());
				wk.setLastModifyMember(this.utils.getLoggedInMember());
				wk.setTeam(this.team);
				this.wikiService.add(wk);
				this.wiki = wk;
				this.team.setWiki(wk);
			}
			this.contentWikiEdit = this.wiki.getContent();
		} catch (Exception e) {
		    LOGGER.error("handleTeamChange error: " + e.getMessage());
		}
		
		
	}

	public void uploadFile() 
	{
		wiki = wikiService.getByTeamId(team.getTeamId());
		String filename = JSFUtils.getRequestParameter("filename");
		String diskFileName = this.attachmentService.fileNameProcess(FilenameUtils.removeExtension(filename));
		diskFileName = this.attachmentService.replaceFile(filename, diskFileName);
		this.att = new Attachment();
		this.att.setFilename(filename);
		this.att.setDiskFilename(diskFileName);
		this.att.setContainerId((long) this.wiki.getWikiId());
		this.att.setContainerType(Attachment.WIKI_ATTACHMENT);
		this.att.setTemp(false);
		this.att.setCreatedOn(new Date());
		this.att.setAuthor(this.utils.getLoggedInMember());
		this.attachmentService.save(this.att);
		attachmentList.add(att);

		if (this.att.getContainerId() != null) 
		{
			this.att.setTemp(false);
			this.attachmentService.moveAttachmentFile(this.att,
					this.wiki.getWikiId());
		} else {
			this.att.setTemp(true);
		}
	}

	public void deleteAttachmentOfWiki() 
	{
		Attachment deleteAttachment = this.att;
		this.attachmentService.delete(deleteAttachment);
		this.attachmentService.deleteFileInWiki(deleteAttachment.getDiskFilename(), this.wiki.getWikiId());
		this.attachmentList = this.attachmentService.findAttachmentByWiki(this.wiki);
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public String getAllTopics() {
		return allTopics;
	}

	public void setAllTopics(String allTopicrfs) {
		this.allTopics = allTopicrfs;
	}

	public List<Team> getTeamList() {
		return teamList;
	}

	public void setTeamList(List<Team> teamList) 
	{
	    excludeHiddenTeamsForSelection(teamList);
	}

	public List<TeamMember> getTeamMemberList() {
		return teamMemberList;
	}

	public void setTeamMemberList(List<TeamMember> teamMemberList) {
		this.teamMemberList = teamMemberList;
	}

	public Attachment getAtt() {
		return att;
	}

	public void setAtt(Attachment att) {
		this.att = att;
	}

	public List<Attachment> getAttList() {
		return attachmentList;
	}

	public void setAttList(List<Attachment> attList) {
		this.attachmentList = attList;
	}

	public Wiki getWiki() {
		return wiki;
	}

	public void setWiki(Wiki wiki) {
		this.wiki = wiki;
	}

	public Long getIdTeam() {
		return idTeam;
	}

	public void setIdTeam(Long idTeam) {
		this.idTeam = idTeam;
	}

	public String getContentWiki() {
		return contentWiki;
	}

	public void setContentWiki(String contentWiki) {
		this.contentWiki = contentWiki;
	}

	public Boolean getPreventAccessFromUrl() {
		return preventAccessFromUrl;
	}

	public void setPreventAccessFromUrl(Boolean preventAccessFromUrl) {
		this.preventAccessFromUrl = preventAccessFromUrl;
	}

	public String getContentWikiEdit() {
		return contentWikiEdit;
	}

	public void setContentWikiEdit(String contentWikiEdit) {
		this.contentWikiEdit = contentWikiEdit;
	}
	
	private void excludeHiddenTeamsForSelection(List<Team> allTeams)
	{
	    this.teamList = new ArrayList<Team>();
	    
	    if (allTeams != null && allTeams.size() > 0)
	    {
		for (Team team : allTeams) 
		{
		    if (team != null && team.getValidTo() == null) //validTo = null => team is not hidden. 
		    {
			this.teamList.add(team);
		    }
		}
	    }
	}
}
