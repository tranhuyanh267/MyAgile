package com.ant.myagile.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ant.myagile.model.Attachment;
import com.ant.myagile.service.AttachmentService;
import com.ant.myagile.utils.SpringContext;

@FacesConverter(forClass = Attachment.class)
public class AttachmentConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent component, String value) throws ConverterException {
		if ((value == null) || (value.length() == 0)) {
			return null;
		} else {
			Long id = Long.parseLong(value);
			AttachmentService attachmentService = SpringContext.getApplicationContext().getBean(AttachmentService.class);
			Attachment attachmentTemp = attachmentService.findAttachmentById(id);
			return attachmentTemp;
		}
	}

	@Override
	public String getAsString(FacesContext facesContext, UIComponent component, Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		if (object instanceof Attachment) {
			Attachment at = (Attachment) object;
			if (at.getAttachmentId() == null) {
				return null;
			}
			return at.getAttachmentId().toString();
		} else {
			throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Attachment.class.getName());
		}
	}
}
