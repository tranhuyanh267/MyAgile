package com.ant.myagile.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.converter.ClientTimeOffsetConverter;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Project;
import com.ant.myagile.model.RetrospectiveResult;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.ProjectService;
import com.ant.myagile.service.RetrospectiveResultService;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.Utils;

@Component("retroMB")
@Scope("session")
public class RetrospectiveResultBean implements Serializable {
	private static final long serialVersionUID = 1L;
	@Autowired
	private RetrospectiveResultService retroService;
	@Autowired
	private TeamService teamService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private SprintService sprintService;
	@Autowired
	private Utils utils;

	private RetrospectiveResult retro;
	private RetrospectiveResult selectedRetro;
	private String newDes;
	private List<RetrospectiveResult> listAllRetro;
	private String teamID;
	private String projectID;
	private String sprintID;
	private Long selectedId;

	/**
	 * Find all Retrospective Result of a Team in a Sprint. If Team or Sprint is
	 * null, return a null List
	 * 
	 * @return List of RetrospectiveResult
	 */
	public List<RetrospectiveResult> getListAllRetro() {
		if (getProjectID() != null && getTeamID() != null) {
			Team team = this.teamService.findTeamByTeamId(Long.parseLong(getTeamID()));
			Project projectTemp = this.projectService.findProjectById(Long.parseLong(getProjectID()));
			List<RetrospectiveResult> list = this.retroService.findRetrospectiveByTeamAndProject(team, projectTemp);
			ClientTimeOffsetConverter.transformServerTimeToClientTimeretroResult(list);
			this.listAllRetro = list;
		} else {
			this.listAllRetro = new ArrayList<RetrospectiveResult>();
		}
		return this.listAllRetro;
	}

	public void selectedRetro(long id) {
		this.selectedRetro = this.retroService.getRetrospectivetroById(id);
	}

	public void updateRetro() {
		this.retroService.updateRetrospective(getSelectedRetro());
		setSelectedRetro(new RetrospectiveResult());
	}

	public void cancel() {
		this.selectedRetro = new RetrospectiveResult();
		this.retro.setDescription("");
	}

	public void updateStatus() {
		this.retroService.updateStatus(getSelectedRetro());
	}

	/**
	 * Create a new Retrospective Result with information entered in view, then
	 * save it into database.
	 */
	@SuppressWarnings("static-access")
	public void addRetro() {
		boolean flag = true;
		int countTeam = this.teamService.countTeamByProject(Long.parseLong(getProjectID()));
		if (countTeam == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", this.utils.getMessage("myagile.home.ImpedimentNullTeam", null)));
			flag = false;
		}
		Member memberOwner = this.utils.getLoggedInMember();
		RetrospectiveResult retroTemp = new RetrospectiveResult();
		retroTemp.setDescription(this.retro.getDescription());
		retroTemp.setDate(new Date());
		retroTemp.setType("Unhappy");
		retroTemp.setStatus("Unsolved");
		retroTemp.setOwner(memberOwner);
		retroTemp.setProject(this.projectService.findProjectById(Long.parseLong(getProjectID())));
		int countSprint = this.sprintService.countSprintsByTeamId(Long.parseLong(getTeamID()));
		if (countSprint == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", this.utils.getMessage("myagile.home.ImpedimentNullSprint", null)));
			flag = false;
		}
		if (flag) {
			retroTemp.setSprint(this.sprintService.findLastSprintByTeamId(Long.parseLong(getTeamID())));
			this.retroService.addRetrospective(retroTemp);
			this.retro.setDescription("");
		}
	}

	public void reset() {
		this.retro.setDescription("");
		this.retro.setStatus("");
		this.retro.setDate(null);
		this.retro.setType("");
		this.retro.setStatus("");
		this.retro.setSprint(null);
		this.retro.setProject(null);
		this.retro.setOwner(null);
	}

	public List<RetrospectiveResult> findAllRetrospectiveResult() {
		List<RetrospectiveResult> list = this.retroService.findAllRetrospective();
		return list;
	}

	public List<Project> getProjectByTeam(Long teamId) {
		return this.projectService.findProjectsAssignedToTeam(this.teamService.findTeamByTeamId(teamId));
	}

	/**
	 * Delete a Retrospective Result
	 */
	public void deleteRetro() {
		this.retroService.deleteRetrospective(this.selectedRetro.getId());
	}

	/**
	 * Update description of a Retrospective Result
	 */
	public void editRetro() {
		String idTemp = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("des");
		RetrospectiveResult res = this.retroService.getRetrospectivetroById(Long.parseLong(idTemp));
		res.setDescription(this.newDes);
		this.retroService.updateRetrospective(res);
	}

	public void changeRetro(ValueChangeEvent event) {
		setNewDes(event.getNewValue().toString());

	}

	public void startEdit(String description) {
		setNewDes(description);
	}

	/**
	 * Update description, owner and date created of a Retrospective Result
	 * 
	 * @param id
	 *            - ID of RetrospectiveResult, Long number format
	 */
	public void updateRetro(Long id) {
		Member memberOwner = this.utils.getLoggedInMember();
		RetrospectiveResult res = this.retroService.getRetrospectivetroById(id);
		if (getNewDes() != null) {
			res.setDescription(this.newDes);
		}
		res.setOwner(memberOwner);
		res.setDate(new Date());
		this.retroService.updateRetrospective(res);
		setNewDes(null);
	}

	public String getNewDes() {
		return this.newDes;
	}

	public void setNewDes(String newDes) {
		this.newDes = newDes;
	}

	public RetrospectiveResult getSelectedRetro() {
		return this.selectedRetro;
	}

	public void setSelectedRetro(RetrospectiveResult selectedRetro) {
		this.selectedRetro = selectedRetro;
	}

	public RetrospectiveResult getRetro() {
		if (this.retro == null) {
			this.retro = new RetrospectiveResult();
		}
		return this.retro;
	}

	public void setRetro(RetrospectiveResult retro) {
		this.retro = retro;
	}

	public Long getSelectedId() {
		return this.selectedId;
	}

	public void setSelectedId(Long selectedId) {
		this.selectedId = selectedId;
	}

	public String getSprintID() {
		return this.sprintID;
	}

	public void setSprintID(String sprintID) {
		this.sprintID = sprintID;
	}

	public String getProjectID() {

		return this.projectID;
	}

	public void setProjectID(String projectID) {
		this.projectID = projectID;
	}

	public String getTeamID() {
		return this.teamID;
	}

	public void setTeamID(String teamID) {
		this.teamID = teamID;
	}
}
