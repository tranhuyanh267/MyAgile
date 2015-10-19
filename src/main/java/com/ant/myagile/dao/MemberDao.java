package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;

public interface MemberDao {
    boolean save(Member member);

    boolean delete(Member member);

    boolean update(Member member);

    void removeMemberFromTeam(Long teamMemberId);

    boolean saveAndAddRoleMember(Member member, Long roleID);

    Member getMemberByUsername(String username);

    Member findMemberByLDapUsername(String lDapUsername);

    Member findMemberById(long memberId);

    Member findProjectOwner(Long projectId);

    Member findMemberByToken(String token);

    Member findTeamOwnerByTeamId(long teamId);

    Member findMemberByEmail(String email);

    Member findMemberBySkype(String accountSkype);

    List<TeamMember> findTeamMemberListByTeamId(Long id);

    List<Member> findMembersByTeam(Team team);

    List<Member> findDevelopmentMembersByTeamId(long teamId);

    List<Member> findMembersRelatedToMemberId(long memberId);

    List<Member> findMembersInTheSameTeamByMemberId(long memberId);

    String generatePasswordDefault();

    List<Member> findMembersInScrumTeamByMemberId(long memberId);

    List<Member> findMembersForChatBoxByMemberId(long memberId);

    List<Member> findAll();

    List<Member> findDevelopeMembersByTeam(Team team);

    List<Member> findMemberLazy(StateLazyLoading lazyLoadingMember);

    int countTotalMemberLazy(StateLazyLoading lazyLoadingMember);
    
    List<Member> getAllActiveMembers();

}
