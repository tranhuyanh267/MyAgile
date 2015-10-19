package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberIssue;

public interface MemberIssueService {
	boolean save(MemberIssue memberIssue);

	boolean delete(MemberIssue memberIssue);

	boolean update(MemberIssue memberIssue);

	boolean checkExist(long memberId, long issueId);

	boolean checkExistIssue(long issueId);

	List<Member> getMembersByIssue(long issueId);

	List<MemberIssue> getMemberIssueByMemberIdAndIssueId(long memberId,
			long issueId);
	public boolean deleteAllByIssue(long idIssue);
}
