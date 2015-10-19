package com.ant.myagile.validator;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.managedbean.WizardBean;
import com.ant.myagile.model.Team;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.Utils;
@Component
@Scope("request")
public class TeamValidator implements Validator {
    @Autowired
    WizardBean wizardBean;
    @Autowired
    TeamService teamService;
    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object value)	throws ValidatorException {
	List<Team> createTeamList = wizardBean.getCreatedTeamList();
	//Get index of input team in list
	String[] clientID = uiComponent.getClientId().split(":");
	int indexComponent = Integer.parseInt(clientID[1]);
	String email = value.toString();
	//Check email format
	if(!Utils.checkEmailFormat(email)) {
	    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", email + " is not a valid email"));
	}
	if (!isExistedTeam(email,indexComponent)) {
	    createTeamList.get(indexComponent).setMailGroup(email);
	}else {
	    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", email + " has already existed"));
	}
    }

    /** Check if group mail is existed
     */
    private boolean isExistedTeam(String email, int indexComponent) {
	return ( isExistedTeamInCreatedTeamList(email, indexComponent) || isExistedTeamInExistTeamList(email));
    }

    /** Check if group mail is existed in create team list
     */
    private boolean isExistedTeamInCreatedTeamList(String email, int indexComponent){
	List<Team> createTeamList =wizardBean.getCreatedTeamList();
	//Check exist team in createTeamList
	for (int i = 0; i < createTeamList.size(); i++) {
	    Team team = createTeamList.get(i);
	    if ( (i == indexComponent) || (team.getMailGroup() == null) ) {
		continue;
	    }
	    if (team.getMailGroup().equals(email)) {
		return true;
	    }
	}
	return false;
    }
    /** Check if group mail is existed in existed team list
     */
    private boolean isExistedTeamInExistTeamList(String email){
	List<Team> existTeamList =teamService.findAllTeam();
	for(Team team:existTeamList) {
	    if(team.getMailGroup().equals(email)) {
		return true;
	    }
	}
	return false;
    }
}
