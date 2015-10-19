package com.ant.myagile.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ant.myagile.model.Team;
import com.ant.myagile.service.TeamService;
import com.ant.myagile.utils.SpringContext;

@FacesConverter(forClass = Team.class, value = "teamConverter")
public class TeamConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) throws ConverterException {
	if ((value == null) || (value.length() == 0)) {
	    return null;
	} else {
	    Long id = Long.parseLong(value);
	    TeamService teamService = SpringContext.getApplicationContext().getBean(TeamService.class);
	    Team teamTemp = teamService.findTeamByTeamId(id);
	    return teamTemp;
	}
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) throws ConverterException {
	if (object == null) {
	    return null;
	}
	if (object instanceof Team) {
	    Team o = (Team) object;
	    return o.getTeamId().toString();
	} else {
	    throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Team.class.getName());
	}
    }

}
