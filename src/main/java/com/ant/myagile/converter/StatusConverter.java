package com.ant.myagile.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ant.myagile.model.Status;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.utils.SpringContext;

@FacesConverter(forClass = Status.class, value = "statusConverter")
public class StatusConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) throws ConverterException {
	if ((value == null) || (value.length() == 0)) {
	    return null;
	} else {
	    Long id = Long.parseLong(value);
	    StatusService statusService = SpringContext.getApplicationContext().getBean(StatusService.class);
	    Status statusTemp = statusService.findStatusById(id);
	    return statusTemp;
	}
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) throws ConverterException {
	if (object == null) {
	    return null;
	}
	if (object instanceof Status) {
	    Status o = (Status) object;
	    if (o.getStatusId() == null) {
		return null;
	    }
	    return o.getStatusId().toString();
	} else {
	    throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Status.class.getName());
	}
    }

}
