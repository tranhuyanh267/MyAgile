package com.ant.myagile.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PreDestroy;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Message;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.service.MessageService;
import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.Utils;

@Component("onlineSessionBean")
@Scope("session")

public class OnlineSessionBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @Autowired
    private OnlineMembersBean onlineMembersBean;
    @Autowired
    private MemberService memberService;
    @Autowired
    private Utils utils;
    @Autowired
    private MessageService messageService;

    private static Logger logger = Logger.getLogger(OnlineSessionBean.class);

    private List<Member> relatedMembers;
    private List<Member> membersInScrumTeam;
    private Member loggedInMember;
    private String privateChannel;
    private final PushContext pushContext = PushContextFactory.getDefault()
	    .getPushContext();
    private boolean flag = true;
    public static String contextPath ;
    private List<Member> userWithUnreadConversation;

	private static final String MSG_CHAT = "chat";
	private static final String MSG_TYPING_STATUS = "typing";
	private static final String MSG_OLD_CHAT = "old-chat";
	

    public List<Member> getMembersInScrumTeam() {
	return membersInScrumTeam;
    }

    public void setMembersInScrumTeam(List<Member> membersInScrumTeam) {
	this.membersInScrumTeam = membersInScrumTeam;
    }

    public Member getLoggedInMember() {
	return loggedInMember;
    }

    public String getPrivateChannel() {
    	if(privateChannel == null || privateChannel.isEmpty()) {
    		privateChannel = Utils.generateSecretString(loggedInMember.getUsername(), loggedInMember.getPassword());
    	}
    	return privateChannel;
    }

    public void setPrivateChannel(String privateChannel) {
    	this.privateChannel = privateChannel;
    }

    public List<Member> getRelatedMembers() {
	return relatedMembers;
    }

    public void setRelatedMembers(List<Member> relatedMembers) {
	this.relatedMembers = relatedMembers;
    }

	public void setRelatedMembers() {
	this.relatedMembers = memberService.findRelatedMembersByMemberId(utils.getLoggedInMember().getMemberId());
    }

	public boolean getStatus() {
	return true;
    }

    public List<Member> getUserWithUnreadConversation() {
		return userWithUnreadConversation;
	}

	public void setUserWithUnreadConversation(
			List<Member> userWithUnreadConversation) {
		this.userWithUnreadConversation = userWithUnreadConversation;
	}

	public boolean isFlag() {
	return flag;
    }

    public void setFlag(boolean flag) {
	this.flag = flag;
    }


    /**
     * This function use to notify who online in system,notification will push to all member related with login user
     * @param type:online or offline
     * @param color: color code (eg:#fffff)
     */
    public void notification(String type, String color) {
	String avatar = "<img class='avatar img-circle' src='"+"/file/?type=small-member-logo&filename=" + loggedInMember.getAvatar() + "'>";
	String icon = "&#160;<i class='icon-ok-sign' style='color:" + color + "font-size:20px;'>&#160;</i>";
	String name = "&#160;&#160;<b>" + Utils.checkName(loggedInMember) + "</b><br/>";
	String status = "<i style='padding:70px;'>is " + type + "</i>";
	String divContent = avatar + icon + name + status;
	for (Member m : relatedMembers) {
	    if(OnlineMembersBean.onlineMembers.containsKey(m.getUsername())) {
		pushContext.push("/" + m.getUsername(), divContent);
	    }
	}

	for (Member m : membersInScrumTeam) {
	    if(OnlineMembersBean.onlineMembers.containsKey(m.getUsername())) {
		pushContext.push("/" + m.getUsername(), divContent);
	    }
	}
    }

    public void on() {
	onlineMembersBean.addOnlineMember(loggedInMember);
    }

    @PreDestroy
    public void off() {
	notification("offline", "#737477;");
	onlineMembersBean.removeOfflineMember(loggedInMember);
    }

    public void initial() {
		loggedInMember = utils.getLoggedInMember();
		privateChannel = Utils.generateSecretString(loggedInMember.getUsername(), loggedInMember.getPassword());
		userWithUnreadConversation = messageService.findMembersByTheirUnreadConversation(loggedInMember.getMemberId());

		if (!FacesContext.getCurrentInstance().isPostback()) {
	    try {
			onlineMembersBean.addOnlineMember(loggedInMember);
			relatedMembers = memberService.findRelatedMembersByMemberId(loggedInMember.getMemberId());
			membersInScrumTeam = memberService.findMembersForChatBoxByMemberId(loggedInMember.getMemberId());
				if (this.flag) {
				    notification("online", "#27AE60;");
				    setFlag(false);
				}
		    } catch (Exception e) { 
		    	
		    }
		}
    }

    public String showTitle(Member member) {
	try {
	    if (member.getFirstName().length() > 0 || member.getLastName().length() > 0) {
		return member.getLastName() + " " + member.getFirstName() + " (" + member.getUsername() + ")";
	    } else {
		return member.getUsername();
	    }
	} catch (Exception e) {
	    return member.getUsername();
	}
    }

    public String showName(Member member) {
		try {
		    if (member.getFirstName().length() > 0 || member.getLastName().length() > 0) {
			return member.getLastName() + " " + member.getFirstName();
		    } else {
			return member.getUsername();
		    } 
		} catch (Exception e) {
		    return member.getUsername();
		}
    }

    public void sendMessage(Member recipient) throws InterruptedException, ExecutionException {
        try {
            String content = JSFUtils.getRequestParameter("content");
            String recipientChannel = Utils.generateSecretString(recipient.getUsername(), recipient.getPassword());
            JSONObject obj = new JSONObject();
            obj.put("msgType", OnlineSessionBean.MSG_CHAT);
            obj.put("sender", loggedInMember.getMemberId());
            obj.put("recipient", recipient.getMemberId());
            obj.put("content", content);
            obj.put("groupDate", "");
            obj.put("isRead", false);
            obj.put("sendDate", Utils.toLongDate(new Date()));
            String message = obj.toString();

            pushContext.push("/im" + recipientChannel, message);

            // Save message
            Message messageObj = new Message();
            messageObj.setContent(content);
            messageObj.setSendDate(new Date());
            messageObj.setRead(false);
            messageObj.setRecipient(recipient);
            messageObj.setSender(loggedInMember);
            messageService.saveMessage(messageObj);

        } catch (JSONException e) {
            logger.warn(e);
        }
    }
    
    public boolean checkHasUnreadMessageFromUser(Member member) {
    	boolean result = false;
    	for(int i = 0; i < userWithUnreadConversation.size(); i++) {
    		if(member.getMemberId() == userWithUnreadConversation.get(i).getMemberId()) {
    			result = true;
    			break;
    		}
    	}
    	return result;
    }
    
    public void updateUnreadConversations() {
    	userWithUnreadConversation = messageService.findMembersByTheirUnreadConversation(loggedInMember.getMemberId());
    }

    public void updateMessageStatus() {
        String sender = JSFUtils.getRequestParameter("sender");
        try {
            List<Message> messages = messageService.findMessageBySenderAndRecipient(Long.parseLong(sender), loggedInMember.getMemberId());
            for (Message ms : messages) {
                if (!ms.isRead()) {
                    ms.setRead(true);
                    boolean result = messageService.updateMessage(ms);
                    if (result) {
                        for (int i = 0; i < userWithUnreadConversation.size(); i++) {
                            if (Long.parseLong(sender) == userWithUnreadConversation.get(i).getMemberId()) {
                                userWithUnreadConversation.remove(i);
                                pushContext.push("/sttu" + privateChannel, "unread-updated");
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(e);
        }
    }

	public void sendTypingStatus(Member recipient) {
		try {
			String recipientChannel = Utils.generateSecretString(recipient.getUsername(), recipient.getPassword());
			JSONObject obj = new JSONObject();
			obj.put("msgType", OnlineSessionBean.MSG_TYPING_STATUS);
			obj.put("recipient", loggedInMember.getMemberId());
			String message = obj.toString();
			pushContext.push("/im" + recipientChannel, message);
		} catch (JSONException e) {}
	}
	
    public void loadMessageByDate(){
	String sender = JSFUtils.getRequestParameter("sender");
	String numberDate = JSFUtils.getRequestParameter("numberDate");
	try {
		List<Message> messages = messageService.findMessageBySendDateNumberAndSenderIdAndRecipientId(numberDate, Long.parseLong(sender), loggedInMember.getMemberId());
		List<String> list = new ArrayList<String>();
		List<JSONObject> listObject = new ArrayList<JSONObject>();
		for (Message msg : messages) {
			String date = "";
		    if(list.size() == 0){
		    	list.add(Utils.toShortDate(msg.getSendDate()));
		    	date = Utils.toShortDate(msg.getSendDate());
		    }
		    else{
		    	if(list.contains(Utils.toShortDate(msg.getSendDate()))){
		    		date = "";
		    	}
		    	else{
		    		list.add(Utils.toShortDate(msg.getSendDate()));
		    		date = Utils.toShortDate(msg.getSendDate());
		    	}
		    }
		    
			JSONObject obj = new JSONObject();
			obj.put("msgType", OnlineSessionBean.MSG_CHAT);
			obj.put("sender", msg.getSender().getMemberId());
			obj.put("recipient", msg.getRecipient().getMemberId());
			obj.put("groupDate",date);
			obj.put("content", msg.getContent());
			obj.put("isRead",msg.isRead());
			obj.put("sendDate", Utils.toLongDate(msg.getSendDate()));
			listObject.add(obj);
		}
		JSONObject listChat = new JSONObject();
		listChat.put("msgType", "list-chat");
		listChat.put("content", listObject);
		listChat.put("sender", loggedInMember.getMemberId());
		listChat.put("recipient", Long.parseLong(sender));
		
		pushContext.push("/im" + privateChannel, listChat.toString());
	}catch (JSONException e) {}
    }
    
    public void loadConversation(){
    	String sender = JSFUtils.getRequestParameter("sender");
    	String limitNumber = JSFUtils.getRequestParameter("limitNumber");
    	try {
    		List<Message> messages = messageService.findMessageBySenderIdAndRecipientIdAndLimitNumber(Long.parseLong(sender), loggedInMember.getMemberId(), Integer.valueOf(limitNumber));
    		List<String> list = new ArrayList<String>();
    		List<JSONObject> listObject = new ArrayList<JSONObject>();
    		for (Message msg : messages) {
    	    	String date = "";
    		    if(list.size() == 0){
    		    	list.add(Utils.toShortDate(msg.getSendDate()));
    		    	date = Utils.toShortDate(msg.getSendDate());
    		    }
    		    else{
    		    	if(list.contains(Utils.toShortDate(msg.getSendDate()))){
    		    		date = "";
    		    	}
    		    	else{
    		    		list.add(Utils.toShortDate(msg.getSendDate()));
    		    		date = Utils.toShortDate(msg.getSendDate());
    		    	}
    		    }
    		JSONObject obj = new JSONObject();
    		obj.put("msgType", OnlineSessionBean.MSG_OLD_CHAT);
    		obj.put("sender", msg.getSender().getMemberId());
    		obj.put("recipient", msg.getRecipient().getMemberId());
    		obj.put("groupDate",date);
    		obj.put("content", msg.getContent());
    		obj.put("isRead",msg.isRead());
    		obj.put("sendDate", Utils.toLongDate(msg.getSendDate()));
    		listObject.add(obj);
    		
    	    }
    		
    		JSONObject listChat = new JSONObject();
    		listChat.put("msgType", "list-chat");
    		listChat.put("content", listObject);
    		listChat.put("sender", loggedInMember.getMemberId());
    		listChat.put("recipient", Long.parseLong(sender));
    		
    		pushContext.push("/old" + privateChannel, listChat.toString());
    		pushContext.push("/old" + privateChannel, "end");
    		
    	} catch (JSONException e) {}
    }

    /**
     * Send typing status to recipient when user typing on chat box.
     */
    public void sendTypingMessage() {
        try {
            long recipientId = Long.parseLong(JSFUtils.getRequestParameter("recipient"));
            Member recipient = memberService.findMemberById(recipientId);
            String recipientChannel = Utils.generateSecretString(recipient.getUsername(), recipient.getPassword());
            String msgType = JSFUtils.getRequestParameter("msgType");
            boolean content = Boolean.parseBoolean(JSFUtils.getRequestParameter("content"));
            JSONObject obj = new JSONObject();
            obj.put("recipient", recipientId);
            obj.put("sender", loggedInMember.getMemberId());
            obj.put("msgType", msgType);
            obj.put("content", content);
            String message = obj.toString();
            pushContext.push("/im" + recipientChannel, message);
        } catch (Exception e) {
            logger.warn(e);
        }

    }
}
