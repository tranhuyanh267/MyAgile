package com.ant.myagile.utils;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.service.MemberService;
import com.ant.myagile.utils.ActiveDirectory.Encrypt;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	private static Logger logger = Logger.getLogger(CustomAuthenticationProvider.class);
	@Autowired
	private UserDetailsService userDetails;
	@Autowired
	private Utils utils;
	@Autowired
	private MemberService memberService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName().toString().toLowerCase();
		String password = (String) authentication.getCredentials();

		User user = (User) userDetails.loadUserByUsername(username);
		if (user == null) {
			Member member = memberService.findMemberByAlternateEmail(username);
			if (member != null) {
				user = (User) userDetails.loadUserByUsername(member.getUsername());
			}
		}

		String encodedPassword = utils.encodePassword(password);
		
		Member loginUser = memberService.getMemberByUsername(username);
		
		if (loginUser != null) {
			if (memberService.getMemberByUsername(username).isAccountLocked()) {
				throw new BadCredentialsException("Account is locked");
			}
		}
		
		if (username.contains("@")) {// Login with email
			if (user == null) {// Member not exist
				throw new BadCredentialsException("Not exist member.");
			} else if (!encodedPassword.equals(user.getPassword())) {
				throw new BadCredentialsException("Wrong email password.");
			}
		} else {// Login with LDAP
			String decryptedPassword = "";
			try {
				decryptedPassword = Encrypt.decryptString(password);
			} catch (Exception e) {
				logger.debug(e);
			}
			if (!Utils.generateTokenKey(username).trim().equals(decryptedPassword.trim())) {
				throw new BadCredentialsException("Wrong active directory password.");
			}
		}

		Authentication usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
		return usernamePasswordAuthentication;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
