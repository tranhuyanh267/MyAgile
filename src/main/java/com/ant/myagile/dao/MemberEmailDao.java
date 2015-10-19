package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberEmail;

public interface MemberEmailDao {
	
	boolean create(MemberEmail memberEmail);

	boolean update(MemberEmail memberEmail);

	boolean delete(MemberEmail memberEmail);
	
	List<MemberEmail> findMemberEmailByMember(Member member);

	MemberEmail findMemberEmailById(Long emailId);

	MemberEmail findMemberEmailByToken(String token);	
	
	MemberEmail findMemberEmailByEmail(String email);

	List<MemberEmail> findActivatedMemberEmailByMember(Member member);

}
