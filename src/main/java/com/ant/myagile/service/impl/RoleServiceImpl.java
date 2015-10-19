package com.ant.myagile.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ant.myagile.dao.RoleDao;
import com.ant.myagile.model.Role;
import com.ant.myagile.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService{

	@Autowired
	RoleDao roleDao;
	
	@Override
	public Role getRoleByObject(String strProperty, Object obj) {
		return roleDao.getRoleByObject(strProperty, obj);
	}

	@Override
	public Long save(Role role) {
		return roleDao.save(role);
	}

	
}
