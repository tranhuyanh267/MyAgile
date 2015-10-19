package com.ant.myagile.managedbean;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.service.ProjectService;

@Component("initBean")
@Scope("session")
public class initBean {

	@Autowired
	SessionFactory sessionFactory;
	@Autowired
	ProjectService projectService;
	
	public void initPreview() {
		//project
		projectService.setFalseArchivedOfProject();
	}
}