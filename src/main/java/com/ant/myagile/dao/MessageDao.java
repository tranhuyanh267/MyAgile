package com.ant.myagile.dao;

import java.util.List;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Message;

public interface MessageDao {
    List<Message> findAll();

    boolean saveMessage(Message message);

    boolean updateMessage(Message message);

    Message findMessageById(long messageId);

    boolean deleteMessage(Message message);

    List<Message> findMessageByMember(long memberId);

    /**
     * Find messages by send date number, sender, recipient. Example: User can
     * choose to show messages history by (1, 7, 30...) days or show all
     * messages
     * 
     * @param dateNumber
     * @param senderId
     * @param recipientId
     * @return All message
     */
    List<Message> findMessageBySendDateNumberAndSenderIdAndRecipientId(String dateNumber, long senderId, long recipientId);

    List<Message> findMessageBySenderAndRecipient(long senderId, long recipientId);

    List<Member> findMembersByTheirUnreadConversation(long memberId);

    /**
     * Show messages by limit number, sender, and recipient Example: Default,
     * the system will show twenty messages.
     * 
     * @param senderId
     * @param recipientId
     * @param limitNumber
     * @return
     */
    List<Message> findMessageBySenderIdAndRecipientIdAndLimitNumber(long senderId, long recipientId, int limitNumber);
}
