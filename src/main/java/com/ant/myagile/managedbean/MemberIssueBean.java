package com.ant.myagile.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.KanbanIssue;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.MemberIssue;
import com.ant.myagile.service.KanbanHistoryService;
import com.ant.myagile.service.KanbanIssueService;
import com.ant.myagile.service.MemberIssueService;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.StatusService;
import com.ant.myagile.utils.JSFUtils;

@Component("memberIssueBean")
@Scope("session")
public class MemberIssueBean implements Serializable {
	private static final long serialVersionUID = 1L;

	@Autowired
	private MemberIssueService memberIssueService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private KanbanIssueService kanbanIssueService;
	@Autowired
	private StatusService statusService;
	@Autowired
	private KanbanHistoryService kanbanHistoryService;
	
	private final String idScriptView = "script";
	
	public void assignMemberForIssue() {
		try {
			long issueId = Long.parseLong(JSFUtils
					.getRequestParameter("ISSUE_ID"));
			long memberId = Long.parseLong(JSFUtils
					.getRequestParameter("MEMBER_ID"));
			String boxIssueId = JSFUtils.getRequestParameter("BOXISSUE_ID");
			
			if (!memberIssueService.checkExist(memberId, issueId)) {
				Member member = memberService.findMemberById(memberId);
				KanbanIssue kanbanIssue = kanbanIssueService.findKanbanIssueById(issueId);

				MemberIssue memberIssue = new MemberIssue(member, kanbanIssue);
				memberIssueService.save(memberIssue);
				//update history when asign team member
				kanbanHistoryService.historyForAssignMemberForKanbanIssue(member,kanbanIssue);
				
				//update task in cell in kanban
				RequestContext.getCurrentInstance().update(boxIssueId);
				RequestContext.getCurrentInstance().update(idScriptView);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public void unassignMemberIssue() {
		try {
			long memberId = Long.parseLong(JSFUtils
					.getRequestParameter("MEMBER_ID"));
			long issueId = Long.parseLong(JSFUtils
					.getRequestParameter("ISSUE_ID"));
			String boxIssueId = JSFUtils
					.getRequestParameter("BOXISSUE_ID");
			List<MemberIssue> memberIssueList = new ArrayList<MemberIssue>();
			memberIssueList = memberIssueService
					.getMemberIssueByMemberIdAndIssueId(memberId, issueId);
			KanbanIssue kanbanIssueById = kanbanIssueService.findKanbanIssueById(issueId);
			if (!memberIssueList.isEmpty()) {
				for (MemberIssue memberIssue : memberIssueList) {
					memberIssueService.delete(memberIssue);
					//update history when unasign team member
					kanbanHistoryService.historyForUnAssignMemberForKanbanIssue(memberIssue.getMember(),kanbanIssueById);
				}
				//update task in cell in kanban
				RequestContext.getCurrentInstance().update(boxIssueId);
				RequestContext.getCurrentInstance().update(idScriptView);
			}
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public boolean checkExistIssue(long issueId) {
		return memberIssueService.checkExistIssue(issueId);
	}

	public List<Member> getMembersByIssue(long issueId) {
		return memberIssueService.getMembersByIssue(issueId);
	}

	public String getDefaultAvatar(long issueId) {
		List<Member> memberList = memberIssueService.getMembersByIssue(issueId);
		if (memberList == null || memberList.size() <= 0) {
			return "/resources/img/user.png";
		} else if(memberList.size() > 1){
			return "/resources/img/users.png";
		} else {
			return "/file/?type=small-member-logo&amp;filename="+memberList.get(0).getAvatar();
		}
	}
	
	public boolean issueHasTwoMembers(long issueId) {
		return getMembersByIssue(issueId).size() == 2;
	}
}
