package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberIssue;

public interface MemberIssueDao {
	boolean save(MemberIssue memberIssue);

    boolean delete(MemberIssue memberIssue);

    boolean update(MemberIssue memberIssue);
    
    boolean checkExist(long member, long issue);
    
    boolean checkExistIssue(long issue);
    
    List<Member> getMembersByIssue(long issueId);
    
    List<MemberIssue> getMemberIssueByMemberIdAndIssueId(long memberId, long issueId);
    
    boolean deleteAllByIssue(long idIssue);
}
