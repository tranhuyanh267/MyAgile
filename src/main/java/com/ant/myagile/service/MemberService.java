package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;

public interface MemberService 
{
    void removeMemberFromTeam(Long teamMemberId);

    boolean save(Member mem);

    boolean delete(Member member);

    boolean update(Member member);

    void deleteLogo(String filename);

    boolean saveAndAddRoleMember(Member member, Long roleID);

    boolean checkMobile(String mobile);

    Member getMemberByUsername(String username);

    Member findMemberByLDapUsername(String lDapUsername);

    Member findMemberById(long memberId);

    Member findProjectOwner(Long projectId);

    Member findMemberByToken(String token);

    Member findMemberBySkype(String accountSkype);

    Member findMemberByEmail(String email);

    Member findMemberByAlternateEmail(String email);

    List<TeamMember> findTeamMemberListByTeamId(Long id);

    List<Member> findMembersByTeam(Team team);

    List<Member> findDevelopmentMembersByTeamId(long teamId, long excludeMember);

    List<Member> findDevelopmentMembersByTeamId(long teamId);

    List<Member> findRelatedMembersByMemberId(long memberId);

    List<Member> findMembersInTheSameTeamByMemberId(long memberId);

    List<Member> findAdminMembers();

    String generatePasswordDefault();

    boolean checkLockedMember(String email);

    /**
     * Change original file name to format "<strong>filename<strong>_time"
     * 
     * @param userName
     * @return file name was changed
     */
    String memberImageNameProcess(String userName);

    /**
     * Rename file uploaded and move file from temp folder to team's folder
     * 
     * @param oldFileName
     * @param newFileName
     * @return new file name
     */
    String renameLogo(String oldFileName, String newFileName);

    List<Member> findMembersInScrumTeamByMemberId(long memberId);

    List<Member> findMembersForChatBoxByMemberId(long memberId);

    List<Member> findAll();

    List<Member> findDevelopeMembersByTeam(Team team);

    List<Member> findMemberLazy(StateLazyLoading lazyLoadingMember);

    int countTotalMemberLazy(StateLazyLoading lazyLoadingMember);
    
    List<Member> getAllActiveMembers();
}
