package com.ant.myagile.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ant.myagile.model.Member;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.utils.SpringContext;

@FacesConverter(forClass = Member.class, value = "memberConverter")
public class MemberConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) throws ConverterException {
	if ((value == null) || (value.length() == 0)) {
	    return null;
	} else {
	    Long id = Long.parseLong(value);
	    MemberService memberService = SpringContext.getApplicationContext().getBean(MemberService.class);
	    Member memberTemp = memberService.findMemberById(id);
	    return memberTemp;
	}
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) throws ConverterException {
	if (object == null) {
	    return null;
	}
	if (object instanceof Member) {
	    Member o = (Member) object;
	    if (o.getMemberId() == null) {
		return null;
	    }
	    return o.getMemberId().toString();
	} else {
	    throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Member.class.getName());
	}
    }

}
