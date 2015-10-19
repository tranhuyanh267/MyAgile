package com.ant.myagile.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Component;

import com.ant.myagile.service.MemberService;

@Component(value = "checkLockedUserFilterChain")
public class CheckLockedUserFilter implements Filter {

	@Autowired
	MemberService memberService;
	
	public FilterConfig filterConfig;
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principle = authentication.getPrincipal();
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;
		String path = servletRequest.getRequestURI();
		String rootURL = servletRequest.getSession().getServletContext().getContextPath();
		
		if (!(principle instanceof String) && !path.endsWith("/login")) {
			User user = (User) authentication.getPrincipal();
			String username = user.getUsername();
			if (memberService.getMemberByUsername(username).isAccountLocked()) {
				new CookieClearingLogoutHandler("remember_me_cookie")
						.logout(servletRequest, servletResponse, authentication);
				new SecurityContextLogoutHandler().logout(servletRequest, servletResponse, authentication);
				servletResponse.sendRedirect(rootURL + "/login?error=locked");
			} else {
				chain.doFilter(request, response);
			}
		} else {
			chain.doFilter(request, response);
		}
		
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.filterConfig = config;
	}

}
