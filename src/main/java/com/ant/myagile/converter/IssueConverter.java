package com.ant.myagile.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ant.myagile.model.Issue;
import com.ant.myagile.service.IssueService;
import com.ant.myagile.utils.SpringContext;

@FacesConverter(forClass = Issue.class, value = "issueConverter")
public class IssueConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) throws ConverterException {
	if ((value == null) || (value.length() == 0)) {
	    return null;
	} else {
	    Long id = Long.parseLong(value);
	    IssueService issueService = SpringContext.getApplicationContext().getBean(IssueService.class);
	    Issue issueTemp = issueService.findIssueById(id);
	    return issueTemp;
	}
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) throws ConverterException {
	if (object == null) {
	    return null;
	}
	if (object instanceof Issue) {
	    Issue is = (Issue) object;
	    if (is.getIssueId() == null) {
		return null;
	    }
	    return is.getIssueId().toString();
	} else {
	    throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Issue.class.getName());
	}
    }

}