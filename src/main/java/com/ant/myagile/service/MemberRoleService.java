package com.ant.myagile.service;
import java.util.List;

import com.ant.myagile.model.MemberRole;


public interface MemberRoleService {
	void addRoleForMember(MemberRole memberRole);
	void updateRoleForMember(MemberRole memberRole);
	List<MemberRole> getRoleByIdMember(Long idMember);
	boolean isAdministrator(long idMember);
	List<MemberRole> getAdminRole();
}
