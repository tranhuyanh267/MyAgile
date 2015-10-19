package com.ant.myagile.service.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.MemberDao;
import com.ant.myagile.dto.StateLazyLoading;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberEmail;
import com.ant.myagile.model.MemberRole;
import com.ant.myagile.model.Team;
import com.ant.myagile.model.TeamMember;
import com.ant.myagile.service.MailService;
import com.ant.myagile.service.MemberEmailService;
import com.ant.myagile.service.MemberRoleService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;

@Service
@Transactional
public class MemberServiceImpl implements MemberService, Serializable {
	private static final long serialVersionUID = 1L;
	@Autowired
	private MemberDao memberDao;
	@Autowired
	private MemberRoleService memberRoleService;
	@Autowired
	private MailService mailService;
	@Autowired
	private Utils utils;
	@Autowired
	private MemberEmailService memberEmailService;
	private final String imageMemberFolder = MyAgileFileUtils
			.getStorageLocation("myagile.upload.image.member.folder");

	@Override
	public List<TeamMember> findTeamMemberListByTeamId(Long id) {
		return memberDao.findTeamMemberListByTeamId(id);
	}

	@Override
	public void removeMemberFromTeam(Long teamMemberId) {
		memberDao.removeMemberFromTeam(teamMemberId);

	}

	@Override
	public Member getMemberByUsername(String username) {
		return memberDao.getMemberByUsername(username);
	}

	@Override
	public boolean save(Member mem) {
		if (memberDao.save(mem)) {
			Date currentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, MMM d, yyyy HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getDefault());
			Object[] detail = { mem.getUsername(),
					dateFormat.format(currentTime) };
			
			SendMailThread sendMailThread = new SendMailThread(mailService, "sendToAdmin",
					Utils.getMessage(
							"myagile.email.createUserNotification.Subject", null), Utils.getMessage(
									"myagile.email.createUserNotification.Content",
									detail));
			sendMailThread.start();
			return true;
		}
		return false;
	}

	@Override
	public boolean delete(Member member) {
		if (memberDao.delete(member)) {
			Date currentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, MMM d, yyyy HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getDefault());
			Object[] detail = { member.getUsername(),
					this.utils.getLoggedInMember().getUsername(),
					dateFormat.format(currentTime) };
			
			SendMailThread sendMailThread = new SendMailThread(mailService, "sendToAdmin",
					Utils.getMessage(
							"myagile.email.removeUserNotification.Subject", null), Utils.getMessage(
									"myagile.email.removeUserNotification.Content",
									detail));
			sendMailThread.start();
			return true;
		}
		return false;
	}

	@Override
	public boolean update(Member member) {
		return memberDao.update(member);
	}

	@Override
	public String generatePasswordDefault() {
		return memberDao.generatePasswordDefault();
	}

	@Override
	public String memberImageNameProcess(String userName) {
		return MyAgileFileUtils.fileNameProcess(userName);
	}

	@Override
	public void deleteLogo(String filename) {
		MyAgileFileUtils.deleteFile(imageMemberFolder + "/" + filename);
	}

	@Override
	public String renameLogo(String oldFileName, String newFileName) {
		return MyAgileFileUtils.renameFile(imageMemberFolder, oldFileName,
				newFileName);
	}

	@Override
	public List<Member> findMembersInScrumTeamByMemberId(long memberId) {
		return memberDao.findMembersInScrumTeamByMemberId(memberId);
	}

	@Override
	public List<Member> findMembersForChatBoxByMemberId(long memberId) {
		return memberDao.findMembersForChatBoxByMemberId(memberId);
	}

	@Override
	public boolean checkMobile(String mobile) {
		String phonePattern = "\\d*";
		Pattern pattern = Pattern.compile(phonePattern);
		Matcher matcher = pattern.matcher(mobile);
		return matcher.matches();
	}

	@Override
	public boolean saveAndAddRoleMember(Member member, Long roleID) {
		if (memberDao.saveAndAddRoleMember(member, roleID)) {
			Date currentTime = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, MMM d, yyyy HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getDefault());
			Object[] detail = { member.getUsername(),
					dateFormat.format(currentTime) };
			SendMailThread sendMailThread = new SendMailThread(mailService, "sendToAdmin",
					Utils.getMessage(
							"myagile.email.createUserNotification.Subject", null), Utils.getMessage(
									"myagile.email.createUserNotification.Content",
									detail));
			sendMailThread.start();
			return true;
		}
		return false;
	}

	@Override
	public List<Member> findMembersByTeam(Team team) {
		return memberDao.findMembersByTeam(team);
	}
	
	@Override
	public List<Member> findDevelopeMembersByTeam(Team team) {
		return memberDao.findDevelopeMembersByTeam(team);
	}
	
	
	@Override
	public Member findProjectOwner(Long projectId) {
		return this.memberDao.findProjectOwner(projectId);
	}

	@Override
	public Member findMemberById(long memberId) {
		return memberDao.findMemberById(memberId);
	}

	@Override
	public List<Member> findDevelopmentMembersByTeamId(long teamId,
			long excludeMember) {
		List<Member> members = memberDao.findDevelopmentMembersByTeamId(teamId);
		for (int i = 0; i < members.size(); i++) {
			if (members.get(i).getMemberId() == excludeMember) {
				members.remove(i);
			}
		}
		return members;
	}

	@Override
	public List<Member> findDevelopmentMembersByTeamId(long teamId) {
		List<Member> members = memberDao.findDevelopmentMembersByTeamId(teamId);
		return members;
	}

	@Override
	public Member findMemberByToken(String token) {
		return memberDao.findMemberByToken(token);
	}

	@Override
	public List<Member> findRelatedMembersByMemberId(long memberId) {
		return memberDao.findMembersRelatedToMemberId(memberId);
	}

	@Override
	public List<Member> findMembersInTheSameTeamByMemberId(long memberId) {
		return memberDao.findMembersInTheSameTeamByMemberId(memberId);
	}

	@Override
	public Member findMemberBySkype(String accountSkype) {
		return memberDao.findMemberBySkype(accountSkype);
	}

	@Override
	public Member findMemberByEmail(String email) {
		return memberDao.findMemberByEmail(email);
	}

	@Override
	public Member findMemberByLDapUsername(String lDapUsername) {
		return memberDao.findMemberByLDapUsername(lDapUsername);
	}

	@Override
	public Member findMemberByAlternateEmail(String email) {
		MemberEmail memberEmail = memberEmailService
				.findMemberEmailByEmail(email);
		if (memberEmail != null && memberEmail.isActive()) {
			return memberDao.findMemberById(memberEmail.getMember()
					.getMemberId());
		}
		return null;
	}

	@Override
	public List<Member> findAll() {
		return memberDao.findAll();
	}

	@Override
	public boolean checkLockedMember(String email) {
		Member member = findMemberByEmail(email);
		return member.isAccountLocked();
	}

	@Override
	public List<Member> findAdminMembers() {
		List<MemberRole> adminRoleList = memberRoleService.getAdminRole();
		List<Member> adminList = new ArrayList<Member>();
		for (MemberRole adminRole : adminRoleList) {
			if(!memberDao.findMemberById(adminRole.getMemberId()).isAccountLocked())
				adminList.add(memberDao.findMemberById(adminRole.getMemberId()));
		}
		return adminList;
	}

	@Override
	public List<Member> findMemberLazy(StateLazyLoading lazyLoadingMember) {
		return memberDao.findMemberLazy(lazyLoadingMember);
	}

	@Override
	public int countTotalMemberLazy(StateLazyLoading lazyLoadingMember) {
		return memberDao.countTotalMemberLazy(lazyLoadingMember);
	}

	@Override
	public List<Member> getAllActiveMembers() 
	{
	    List<Member> allActiveMembers = memberDao.getAllActiveMembers();
	    return allActiveMembers;
	}
}
