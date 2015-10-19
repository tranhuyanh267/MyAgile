package com.ant.myagile.utils;

import java.io.IOException;
import java.util.List;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

public class JSFUtils {
	static Logger log = Logger.getLogger(JSFUtils.class);
	/**
	 * Add message
	 * 
	 * @param id
	 *            : id of input field
	 * @param msg
	 *            : message
	 */
	public static void addErrorMessage(String id, String msg) {
		FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
		FacesContext.getCurrentInstance().addMessage(id, facesMsg);
	}

	public static void addSuccessMessage(String id, String msg) {
		FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
		FacesContext.getCurrentInstance().addMessage(id, facesMsg);
	}

	public static void addWarningMessage(String id, String msg) {
		FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg);
		FacesContext.getCurrentInstance().addMessage(id, facesMsg);
	}

	/**
	 * Get request parameter
	 * 
	 * @param key
	 *            : name of selector
	 */
	public static String getRequestParameter(String key) {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
	}

	/**
	 * Get FacesContext.
	 * 
	 * @return FacesContext
	 */
	public static FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	/**
	 * Method for setting a new object into a JSF managed bean Note: will fail
	 * silently if the supplied object does not match the type of the managed
	 * bean.
	 * 
	 * @param expression
	 *            EL expression
	 * @param newValue
	 *            new value to set
	 */
	public static void setExpressionValue(String expression, Object newValue) {
		FacesContext facesContext = getFacesContext();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);
		Class<?> bindClass = valueExp.getType(elContext);
		if (bindClass.isPrimitive() || bindClass.isInstance(newValue)) {
			valueExp.setValue(elContext, newValue);
		}
	}

	/**
	 * Method for taking a reference to a JSF binding expression and returning
	 * the matching object (or creating it).
	 * 
	 * @param expression
	 *            EL expression
	 * @return Managed object resloveMethodExpression(#{bean.value})
	 */
	public static Object resolveExpression(String expression) {
		FacesContext facesContext = getFacesContext();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);
		return valueExp.getValue(elContext);
	}

	/**
	 * Convenience method for setting the value of a managed bean by name rather
	 * than by expression.
	 * 
	 * @param beanName
	 *            name of managed bean
	 * @param newValue
	 *            new value to set
	 * 
	 */
	public static void setManagedBeanValue(String beanName, Object newValue) {
		StringBuffer buff = new StringBuffer("#{");
		buff.append(beanName);
		buff.append("}");
		setExpressionValue(buff.toString(), newValue);
	}

	public static void setManagedBeanListObject(String beanName, List<?> objectList) {
		StringBuffer buff = new StringBuffer("#{");
		buff.append(beanName);
		buff.append("}");
		setExpressionValue(buff.toString(), objectList);
	}

	/**
	 * Convenience method for resolving a reference to a managed bean by name
	 * rather than by expression.
	 * 
	 * @param beanName
	 *            name of managed bean
	 * @return Managed object
	 */
	public static Object getManagedBeanValue(String beanName) {
		StringBuffer buff = new StringBuffer("#{");
		buff.append(beanName);
		buff.append("}");
		return resolveExpression(buff.toString());
	}

	
	/**
	 * Execute method expression (#{bean.method})
	 * @param expression
	 * @param returnType
	 * @param argTypes
	 * @param argValues
	 * @return Object
	 */
	public static Object resloveMethodExpression(String expression, Class<?> returnType, Class<?>[] argTypes, Object[] argValues) {
		FacesContext facesContext = getFacesContext();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		MethodExpression methodExpression = elFactory.createMethodExpression(elContext, expression, returnType, argTypes);
		return methodExpression.invoke(elContext, argValues);
	}

	/**
	 * Reset a form in xhtml page
	 * 
	 * @param idForm
	 */
	public static void resetForm(String idForm) {
		RequestContext context = RequestContext.getCurrentInstance();
		context.reset(idForm);
	}

	/**
	 * Add call back parameter
	 * 
	 * @param name
	 *            key
	 * @param obj
	 */
	public static void addCallBackParam(String name, Object obj) {
		RequestContext.getCurrentInstance().addCallbackParam(name, obj);
	}

	/**
	 * Check request is post back or not
	 * 
	 * @return true if not post back (ajax request)
	 */
	public static boolean isPostbackRequired() {
		return !FacesContext.getCurrentInstance().isPostback();
	}
	
	
	/**
	 * Redirect to other page
	 * 
	 * @param url
	 */
	public static void redirect(String url){
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(url);
		} catch (IOException e) {
			log.error("url not found");
		}
	}
	
	public static void updateComponent(String id){
		RequestContext.getCurrentInstance().update(id);
	}
}
