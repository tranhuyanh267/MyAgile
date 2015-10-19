package com.ant.myagile.dao;

import com.ant.myagile.model.Role;

public interface RoleDao {
	Role getRoleByObject(String strProperty,Object obj);
	Long save(Role role);
}
