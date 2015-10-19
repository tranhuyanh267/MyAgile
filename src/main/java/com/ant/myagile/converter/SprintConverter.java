package com.ant.myagile.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ant.myagile.model.Sprint;
import com.ant.myagile.service.SprintService;
import com.ant.myagile.utils.SpringContext;

@FacesConverter(forClass = Sprint.class, value = "sprintConverter")
public class SprintConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) throws ConverterException {
	if ((value == null) || (value.length() == 0)) {
	    return null;
	} else {
	    Long id = Long.parseLong(value);
	    SprintService sprintService = SpringContext.getApplicationContext().getBean(SprintService.class);
	    Sprint sprintTemp = sprintService.findSprintById(id);
	    return sprintTemp;
	}
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) throws ConverterException {
	if (object == null) {
	    return null;
	}
	if (object instanceof Sprint) {
	    Sprint sp = (Sprint) object;
	    if (sp.getSprintId() == null) {
		return null;
	    }
	    return sp.getSprintId().toString();
	} else {
	    throw new IllegalArgumentException("object " + object + " sp of type " + object.getClass().getName() + "; expected type: " + Sprint.class.getName());
	}
    }

}
