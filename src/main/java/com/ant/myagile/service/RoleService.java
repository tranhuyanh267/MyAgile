package com.ant.myagile.service;

import com.ant.myagile.model.Role;

public interface RoleService {
	Role getRoleByObject(String strProperty,Object obj);
	Long save(Role role);
	
}
