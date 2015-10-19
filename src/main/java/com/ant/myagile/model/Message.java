package com.ant.myagile.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "Message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Message implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private long messageId;
	
	@Column(columnDefinition="TEXT")
	private String content;
	
	@ManyToOne
    @JoinColumn(name = "senderId")
	private Member sender;
	
	@ManyToOne
    @JoinColumn(name = "recipientId")
	private Member recipient;
	
	private Date sendDate;
	
	private boolean isRead;
	
	public long getMessageId() {
		return messageId;
	}
	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Member getSender() {
		return sender;
	}
	public void setSender(Member sender) {
		this.sender = sender;
	}
	public Member getRecipient() {
		return recipient;
	}
	public void setRecipient(Member recipient) {
		this.recipient = recipient;
	}
	public Date getSendDate() {
		return sendDate;
	}
	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	
}
