package com.ant.myagile.managedbean;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.job.GetStatusSkypeJob;
import com.ant.myagile.job.SkypeStatus;
import com.ant.myagile.model.Member;
import com.ant.myagile.service.MemberService;

@Component("onlineMembersBean")
@Scope("singleton")
public class OnlineMembersBean implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final Long INTERVAL = Long.valueOf(60 * 1000);
    public static final int MAX_ONLINE_MEMBERS = 100;
    private static final Logger LOGGER = Logger.getLogger(OnlineMembersBean.class);
    public static Map<String, SkypeStatus> staticSkypeStatus;
    public static Map<String, Member> staticOnlineMembers;
    public static Map<String, Member> onlineMembers;
    public Map<String, Long> timeouts;
    private String skypeAccount;

    @Autowired
    private MemberService memberService;

    @PostConstruct
    public void initital() {
	staticSkypeStatus = new Hashtable<String, SkypeStatus>();
	staticOnlineMembers = new Hashtable<String, Member>();
    }

    public static Map<String, Member> getStaticOnlineMembers() {
	return staticOnlineMembers;
    }

    public static void setStaticOnlineMembers(Map<String, Member> staticOnlineMembers) {
	OnlineMembersBean.staticOnlineMembers = staticOnlineMembers;
    }

    public static Map<String, SkypeStatus> getStaticSkypeStatus() {
	return staticSkypeStatus;
    }

    public static void setStaticSkypeStatus(Map<String, SkypeStatus> staticSkypeStatus) {
	OnlineMembersBean.staticSkypeStatus = staticSkypeStatus;
    }

    public OnlineMembersBean() {
	onlineMembers = new Hashtable<String, Member>(MAX_ONLINE_MEMBERS);
	timeouts = new Hashtable<String, Long>(MAX_ONLINE_MEMBERS);
    }

    /**
     * Add new online member to online list and update online list for all view
     * 
     * @param member
     *            - instance of Member object
     */
    public void addOnlineMember(Member member) {
	try {
	    onlineMembers.put(member.getUsername(), member);
	    updateListMemberInJob();
	    staticOnlineMembers.put(member.getUsername(), member);
	    timeouts.put(member.getUsername(), System.currentTimeMillis());
	    updateOnlineMembers();
	    PushContext pushContext = PushContextFactory.getDefault().getPushContext();
	    pushContext.push("/onlineUsers", "{\"memberId\": " + member.getMemberId() + ", \"status\": \"online\"}");
	    /* Pass context path to GetStatusSkypeJob */
	    String contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();

	    GetStatusSkypeJob.contextPath = contextPath;
	    OnlineSessionBean.contextPath = contextPath;

	} catch (Exception e) {
	    LOGGER.warn("add online member ERROR.");
	}
    }

    /**
     * Remove new offline member from online list and update online list for all
     * view
     * 
     * @param member
     *            - instance of Member object
     */
    public void removeOfflineMember(Member member) {
	try {
	    onlineMembers.remove(member.getUsername());
	    updateListMemberInJob();
	    PushContext pushContext = PushContextFactory.getDefault().getPushContext();
	    pushContext.push("/onlineUsers", "{\"memberId\": " + member.getMemberId() + ", \"status\": \"offline\"}");

	} catch (Exception e) {

	}
    }

    public List<Member> getOnlineMembers() {
	return new LinkedList<Member>(onlineMembers.values());
    }

    public void updateOnlineMembers() {
	List<Member> offlineMembers = new LinkedList<Member>();
	for (Member member : onlineMembers.values()) {
	    if (!isMemberOnline(member)) {
		offlineMembers.add(member);
	    }
	}
	for (Member member : offlineMembers) {
	    removeOfflineMember(member);
	}
    }

    /**
     * Check member is online or not
     * 
     * @param member
     *            - instance of Member
     * @return true if online. Otherwise, return false
     */
    public boolean isMemberOnline(Member member) {
	try {
	    if (onlineMembers.containsKey(member.getUsername())) {
		Long lastActionTime = timeouts.get(member.getUsername());
		return INTERVAL.compareTo(System.currentTimeMillis() - lastActionTime) == 1;
	    } else {
		return false;
	    }
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Check Skype status of member's Skype ID
     * 
     * @param id
     *            - ID of member, Long number format
     * @return status of Skype, such as 'Online', 'Away', 'Do not Disturb',
     *         'Invisible', 'Offline'
     */
    public String getSkypeStatusType(Long id) {
	Member member = memberService.findMemberById(id);
	String skypeAccountTemp = member.getSkype();
	if (skypeAccountTemp != null && GetStatusSkypeJob.skypeMembers.containsKey(skypeAccountTemp)) {
	    return GetStatusSkypeJob.skypeMembers.get(skypeAccountTemp).getStatusType();
	} else {
	    return "offline";
	}
    }

    public String getSkypeAccount() {
	return skypeAccount;
    }

    public void setSkypeAccount(String skypeAccount) {
	this.skypeAccount = skypeAccount;
    }

    /**
     * Check and update Skype status of list of related members
     */
    public void updateListMemberInJob() {
	for (Entry<String, Member> entry : onlineMembers.entrySet()) {
	    List<Member> list1 = memberService.findMembersInTheSameTeamByMemberId(memberService.getMemberByUsername(entry.getKey()).getMemberId());
	    List<Member> list2 = memberService.findRelatedMembersByMemberId(memberService.getMemberByUsername(entry.getKey()).getMemberId());
	    for (Member member : list1) {
		if (!GetStatusSkypeJob.skypeMembers.containsKey(member.getSkype()))
		    GetStatusSkypeJob.skypeMembers.put(member.getSkype(), new SkypeStatus());
	    }
	    for (Member member : list2) {
		if (!GetStatusSkypeJob.skypeMembers.containsKey(member.getSkype()))
		    GetStatusSkypeJob.skypeMembers.put(member.getSkype(), new SkypeStatus());
	    }
	}
    }
}
