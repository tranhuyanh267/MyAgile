package com.ant.myagile.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "Role")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Role implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", nullable=true)
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long roleId;

	private String authority;

	public Long getRoleId() {
		return roleId;
	}
	

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
	
}
