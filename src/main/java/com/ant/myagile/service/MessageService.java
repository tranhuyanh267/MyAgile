package com.ant.myagile.service;

import java.util.List;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Message;

public interface MessageService {
	 List<Message> findAll();
	
	 boolean saveMessage(Message message);
	
	 boolean updateMessage(Message message);
	
	 Message findMessageById(long messageId);
	
	 boolean deleteMessage(Message message);
	
	 List<Message> findMessageByMember(long messageId);
	 
	 List<Message> findMessageBySenderAndRecipient(long senderId, long recipientId);
	 
	 List<Member> findMembersByTheirUnreadConversation(long memberId);
	 
	 List<Message> findMessageBySendDateNumberAndSenderIdAndRecipientId(String dateNumber, Long senderId, Long recipientId);
	 
	 List<Message> findMessageBySenderIdAndRecipientIdAndLimitNumber(long senderId, long recipientId, int limitNumber);
}
