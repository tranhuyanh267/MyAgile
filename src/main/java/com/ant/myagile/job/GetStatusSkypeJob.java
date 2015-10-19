package com.ant.myagile.job;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.primefaces.json.JSONObject;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.utils.Utils;

@Component
@QuartzJob(name = "GetStatusSkypeJob", cronExp = "0 * * * * ?")
public class GetStatusSkypeJob extends QuartzJobBean {
    @Autowired
    MemberService memberService;
    private final PushContext pushContext = PushContextFactory.getDefault().getPushContext();
    public static ConcurrentHashMap<String, SkypeStatus> skypeMembers = new ConcurrentHashMap<String, SkypeStatus>();
    public static String contextPath ;

    /*
     * Get status of Skype account in a every 5 seconds
     */
    @Override
    protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
	for (final Map.Entry<String, SkypeStatus> entry : GetStatusSkypeJob.skypeMembers.entrySet()) {
	    try {
		String account = entry.getKey();
		int oldStatus = entry.getValue().getStatusNum();
		SkypeStatus status = new SkypeStatus();
		int newStatus = Utils.getStatusNumberSkype(account);
		status.setStatusNum(newStatus);
		JSONObject jsonMessage = new JSONObject();
		jsonMessage.put("account", account);
		jsonMessage.put("type", "skype");
		Member member = memberService.findMemberBySkype(account);
		/* Check if change status */
		if (newStatus != oldStatus) {
		    String avatar = "<img class='avatar img-circle' src='"+"/file/?type=small-member-logo&filename=" + member.getAvatar() + "'>";
		    String icon = "<i class='icon-skype-notification-" + status.getStatusType() + " icon-skype'/>";
		    String fullName = "&#160;&#160;<b>" + Utils.checkName(member) + "</b><br/>";

		    if ((newStatus == 2) && (oldStatus != 0)) {
			String strStatus = "<i style='padding:70px;'>is online</i>";
			String divContent = avatar + icon + fullName + strStatus;
			jsonMessage.put("status", "online");
			jsonMessage.put("detail", divContent);
			jsonMessage.put("notice", "true");
			pushContext.push("/skypeNotification", jsonMessage.toString());
		    } else if ((newStatus == 1) && (oldStatus != 0)) {
			String strStatus = "<i style='padding:70px'>is offline</i>";
			String divContent = avatar + icon + fullName + strStatus;
			jsonMessage.put("status", "offline");
			jsonMessage.put("detail", divContent);
			jsonMessage.put("notice", "true");
			pushContext.push("/skypeNotification", jsonMessage.toString());
		    } else {
			jsonMessage.put("status", status.getStatusType());
			jsonMessage.put("notice", "false");
			pushContext.push("/skypeNotification", jsonMessage.toString());
		    }
		    GetStatusSkypeJob.skypeMembers.put(account, status);
		}
	    } catch (Exception e) {
	    }
	}
    }
}
