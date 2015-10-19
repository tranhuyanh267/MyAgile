package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.MemberIssueDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberIssue;
import com.ant.myagile.service.MemberIssueService;

@Service
@Transactional
public class MemberIssueServiceImpl implements MemberIssueService{

	@Autowired
	private MemberIssueDao memberIssueDao;
	
	@Override
	public boolean save(MemberIssue memberIssue) {
		return memberIssueDao.save(memberIssue);
	}

	@Override
	public boolean delete(MemberIssue memberIssue) {
		return memberIssueDao.delete(memberIssue);
	}

	@Override
	public boolean update(MemberIssue memberIssue) {
		return memberIssueDao.update(memberIssue);
	}

	@Override
	public boolean checkExist(long memberId, long issueId) {
		return memberIssueDao.checkExist(memberId, issueId);
	}

	@Override
	public boolean checkExistIssue(long issueId) {
		return memberIssueDao.checkExistIssue(issueId);
	}

	@Override
	public List<Member> getMembersByIssue(long issueId) {
		return memberIssueDao.getMembersByIssue(issueId);
	}

	@Override
	public List<MemberIssue> getMemberIssueByMemberIdAndIssueId(long memberId,
			long issueId) {
		return memberIssueDao.getMemberIssueByMemberIdAndIssueId(memberId, issueId);
	}

	@Override
	public boolean deleteAllByIssue(long idIssue) {
		return memberIssueDao.deleteAllByIssue(idIssue);
	}

}
