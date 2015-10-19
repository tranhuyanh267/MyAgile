package com.ant.myagile.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ant.myagile.dao.MemberDao;
import com.ant.myagile.dao.MemberEmailDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberEmail;
import com.ant.myagile.service.MemberEmailService;

@Service
public class MemberEmailServiceImpl implements MemberEmailService, Serializable{

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private MemberEmailDao memberEmailDao;
	
	@Autowired
	private MemberDao memberDao;

	@Override
	public List<MemberEmail> findMemberEmailByMember(Member member) {
		return memberEmailDao.findMemberEmailByMember(member);
	}

	@Override
	public boolean create(MemberEmail memberEmail) {
		return memberEmailDao.create(memberEmail);
	}

	@Override
	public boolean update(MemberEmail memberEmail) {
		return memberEmailDao.update(memberEmail);
	}

	@Override
	public boolean delete(MemberEmail memberEmail) {
		return memberEmailDao.delete(memberEmail);
	}

	@Override
	public MemberEmail findMemberEmailById(Long emailId) {
		return memberEmailDao.findMemberEmailById(emailId);
	}

	@Override
	public boolean checkExistEmail(String email, Member member) {
		if(member.getUsername().equals(email)){
			return false;
		}
		List<MemberEmail> emailList = new ArrayList<MemberEmail>();
		emailList.addAll(findMemberEmailByMember(member));
		if(emailList.size() == 0){
			return true;
		}
		for(MemberEmail em: emailList){
			if(em.getEmail().equals(email)){
				return false;
			}
		}
		return true;
	}

	@Override
	public MemberEmail findMemberEmailByToken(String token) {
		return memberEmailDao.findMemberEmailByToken(token);
	}

	@Override
	public MemberEmail findMemberEmailByEmail(String email) {
		return memberEmailDao.findMemberEmailByEmail(email);
	}

	@Override
	public boolean checkExistEmail(String email) {
		List<Member> memberList = memberDao.findAll();
		if(memberList != null){
			for(Member member: memberList){
				if(member.isActive()&&member.getUsername().equals(email)){
					return false;
				}
				if(!checkExistActivatedEmail(email,member)){
					return false;
				}
			}
		}	
		return true;
	}
	
	private boolean checkExistActivatedEmail(String email, Member member){
		List<MemberEmail> emailList = new ArrayList<MemberEmail>();
		emailList.addAll(findActivatedMemberEmailByMember(member));
		if(emailList.size() == 0){
			return true;
		}
		for(MemberEmail em: emailList){
			if(em.getEmail().equals(email)){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public List<MemberEmail> findActivatedMemberEmailByMember(Member member) {
		return memberEmailDao.findActivatedMemberEmailByMember(member);
	}

}
