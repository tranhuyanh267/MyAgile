package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.MemberRole;

public interface MemberRoleDao {
    void addRoleForMember(MemberRole memberRole);
    void updateRoleForMember(MemberRole memberRole);
    List<MemberRole> getRoleByIdMember(Long idMember);
    List<MemberRole> getAdminRole();
}
