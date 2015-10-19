package com.ant.myagile.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.service.MemberService;

@Component("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private MemberService memberService;

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		if (username.contains("@")) {
			if (memberService.getMemberByUsername(username) == null) {
				return null;
			} else {
				Member member = memberService.getMemberByUsername(username);
				return buildUserFromUserEntity(member);
			}

		} else {
			if (memberService.findMemberByLDapUsername(username) == null) {
				return null;
			} else {
				Member member = memberService
						.findMemberByLDapUsername(username);
				return buildUserFromUserEntity(member);
			}
		}

	}

	public User buildUserFromUserEntity(Member memberEntity) {
		String username = memberEntity.getUsername();
		String password = memberEntity.getPassword();
		boolean enabled = true;
		boolean accountNonExpired = true;
		boolean credentialsNonExpired = true;
		boolean accountNonLocked = true;

		List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
		grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
		User user = new User(username, password, enabled, accountNonExpired,
				credentialsNonExpired, accountNonLocked, grantedAuths);
		return user;
	}

}
