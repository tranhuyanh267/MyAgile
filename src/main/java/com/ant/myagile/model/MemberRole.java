package com.ant.myagile.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "Memberrole", uniqueConstraints = @UniqueConstraint(columnNames = {
		"roleId", "memberId" }))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MemberRole implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	@Column(name="id")
	private MemberRoleId memberRoleId;
	
	public MemberRole()
	{
		
	}
	public Long getMemberId(){
		return this.memberRoleId.getMemberId();
	}
	public MemberRole(Long roleId,Long memberId)
	{
		this.memberRoleId= new MemberRoleId();
		this.memberRoleId.setMemberId(memberId);
		this.memberRoleId.setRoleId(roleId);
	}

	public MemberRoleId getMemberRoleId() {
		return memberRoleId;
	}

	public void setMemberRoleId(MemberRoleId memberRoleId) {
		this.memberRoleId = memberRoleId;
	}
	
	public void setRoleID(long roleId){
		this.memberRoleId.setRoleId(roleId);
	}
	
	public Long getRoleId(){
		return memberRoleId.getRoleId();
	}
	
}

@Embeddable
class MemberRoleId implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long roleId;

	private Long memberId;

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
}
