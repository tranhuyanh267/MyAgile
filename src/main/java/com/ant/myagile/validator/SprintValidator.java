package com.ant.myagile.validator;

import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.managedbean.SprintBean;
import com.ant.myagile.managedbean.WizardBean;
import com.ant.myagile.model.Sprint;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.Utils;

@Component
@Scope("request")
public class SprintValidator implements Validator {
	@Autowired
	WizardBean wizardBean;
	@Autowired
	SprintService sprintService;
	@Autowired
	TeamService teamService;
	@Autowired
	SprintBean sprintBean;

	@Override
	public void validate(FacesContext facesContext, UIComponent uiComponent, Object value) throws ValidatorException {
		if (uiComponent.getClientId().equals("sprintName")) {
			sprintNameValidate(value);
		}
		if (uiComponent.getClientId().equals("startDate")) {
			startDateValidate(value);
		}
		if (uiComponent.getClientId().equals("endDate")) {
			endDateValidate(value);
		}
	}

	/**
	 * Check if sprint name have already existed
	 * 
	 * @param value
	 */
	public void sprintNameValidate(Object value) {
		String sprintName = value.toString();
		if (sprintName != null) {
			String teamName = wizardBean.getFlagTeamId();
			Team team = teamService.findTeamByGroupMail(teamName);
			if (team != null) {
				List<Sprint> sprintList = sprintService.findSprintsByTeamId(team.getTeamId());
				for (Sprint sprint : sprintList) {
					if (sprint.getSprintName().equals(sprintName)) {
						sprintBean.getSprint().setSprintName("");
						throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", sprintName + " has already existed"));
					}
				}
			}
		}
	}

	/**
	 * Check if end date is valid
	 * 
	 * @param value
	 */
	public void endDateValidate(Object value) {
		Sprint sprint = sprintBean.getSprint();
		if (sprint.getDateStart().compareTo((Date) value) >= 0) {
			String startDate = Utils.toShortDate(sprint.getDateStart());
			Object[] params = { startDate };
			throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", Utils.getMessage("myagile.sprint.newedit.EndDateSmall", params)));

		}
	}

	/**
	 * Check if start date is valid
	 * 
	 * @param value
	 */
	public void startDateValidate(Object value) {
		Team team = teamService.findTeamByGroupMail(wizardBean.getFlagTeamId());
		if (team != null) {
			Long teamId = teamService.findTeamByGroupMail(wizardBean.getFlagTeamId()).getTeamId();
			Sprint lastSprint = sprintService.findLastSprintByTeamId(teamId);
			if (lastSprint != null) {
				if (lastSprint.getDateEnd().compareTo((Date) value) >= 0) {
					String endDate = Utils.toShortDate(lastSprint.getDateEnd());
					Object[] params = { endDate };
					throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", Utils.getMessage("myagile.sprint.new.StartDateSmall", params)));
				}
			}
		}
	}
}
