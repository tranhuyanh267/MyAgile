package com.ant.myagile.utils;

import org.springframework.security.core.GrantedAuthority;


public enum MyAgileAuthority implements GrantedAuthority{
	ROLE_USER;
	
	@Override
	public String getAuthority() {
		return name();
	}

}
