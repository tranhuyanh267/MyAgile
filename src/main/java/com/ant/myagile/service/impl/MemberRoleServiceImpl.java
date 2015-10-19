package com.ant.myagile.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ant.myagile.dao.MemberRoleDao;
import com.ant.myagile.model.MemberRole;
import com.ant.myagile.service.MemberRoleService;
import com.ant.myagile.service.RoleService;

@Service
@Transactional
public class MemberRoleServiceImpl implements MemberRoleService{
	@Autowired
	private MemberRoleDao memberRoleDao;
	
	@Autowired
	private RoleService roleService;
	
	@Override
	public void addRoleForMember(MemberRole memberRole) {
		memberRoleDao.addRoleForMember(memberRole);
	}

	@Override
	public List<MemberRole> getRoleByIdMember(Long idMember) {
		return memberRoleDao.getRoleByIdMember(idMember);
	}

	@Override
	public void updateRoleForMember(MemberRole memberRole) {
		memberRoleDao.updateRoleForMember(memberRole);
	}

	@Override
	public boolean isAdministrator(long idMember) {
		List<MemberRole> roleList =getRoleByIdMember(idMember);
		for (MemberRole memberRole : roleList) {
			if(memberRole.getRoleId().equals(roleService.getRoleByObject("authority", "ROLE_ADMIN").getRoleId())){
				return true;
			}
		}
		return false;
	}

	@Override
	public List<MemberRole> getAdminRole() {
		return memberRoleDao.getAdminRole();
	}
}
