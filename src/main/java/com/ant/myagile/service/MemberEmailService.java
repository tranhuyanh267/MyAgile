package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberEmail;

public interface MemberEmailService {
	boolean create(MemberEmail memberEmail);
	boolean update(MemberEmail memberEmail);
	boolean delete(MemberEmail memberEmail);
	List<MemberEmail> findMemberEmailByMember(Member member);
	MemberEmail findMemberEmailById(Long emailId);
	boolean checkExistEmail(String email, Member member);
	boolean checkExistEmail(String email);
	MemberEmail findMemberEmailByToken(String token);
	MemberEmail findMemberEmailByEmail(String email);
	List<MemberEmail> findActivatedMemberEmailByMember(Member member);
	
}
