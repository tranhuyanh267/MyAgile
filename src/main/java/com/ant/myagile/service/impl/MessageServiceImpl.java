package com.ant.myagile.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.dao.MessageDao;
import com.ant.myagile.model.Member;
import com.ant.myagile.model.Message;
import com.ant.myagile.service.MessageService;

@Service
@Transactional
public class MessageServiceImpl implements MessageService{

	@Autowired
	private MessageDao messageDao;
	
	@Override
	public List<Message> findAll() {
		return messageDao.findAll();
	}

	@Override
	public boolean saveMessage(Message message) {
		return messageDao.saveMessage(message);
	}

	@Override
	public boolean updateMessage(Message message) {
		return messageDao.updateMessage(message);
	}

	@Override
	public Message findMessageById(long messageId) {
		return messageDao.findMessageById(messageId);
	}

	@Override
	public boolean deleteMessage(Message message) {
		return messageDao.deleteMessage(message);
	}

	@Override
	public List<Message> findMessageByMember(long messageId) {
		return messageDao.findMessageByMember(messageId);
	}

	@Override
	public List<Message> findMessageBySenderAndRecipient(long senderId,
			long recipientId) {
		
		return messageDao.findMessageBySenderAndRecipient(senderId, recipientId);
	}

	@Override
	public List<Member> findMembersByTheirUnreadConversation(long memberId) {
		return messageDao.findMembersByTheirUnreadConversation(memberId);
	}

	@Override
	public List<Message> findMessageBySendDateNumberAndSenderIdAndRecipientId(String dateNumber, Long senderId, Long recipientId) {
		return messageDao.findMessageBySendDateNumberAndSenderIdAndRecipientId(dateNumber, senderId, recipientId);
	}

	@Override
	public List<Message> findMessageBySenderIdAndRecipientIdAndLimitNumber(
			long senderId, long recipientId, int limitNumber) {
		return messageDao.findMessageBySenderIdAndRecipientIdAndLimitNumber(senderId, recipientId, limitNumber);
	}

}
