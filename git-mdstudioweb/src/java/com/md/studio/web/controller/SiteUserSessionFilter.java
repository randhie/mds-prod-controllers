package com.md.studio.web.controller;
import static com.md.studio.utils.WebConstants.*;
import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.md.studio.domain.SiteUser;
import com.md.studio.domain.UserContext;
import com.md.studio.service.ReferenceDataSvc;

public class SiteUserSessionFilter extends AbstractController implements Filter {
	private static final String C_PIPE = "\\|";
	private ReferenceDataSvc referenceDataSvc;
	

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filter) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpSession session = request.getSession();
		
		SiteUser siteUser = (SiteUser) session.getAttribute(SESSION_SITEUSER);
		if (siteUser != null) {
			UserContext.setIpAddress(getIpAddress(session, request));
			UserContext.setUserId(siteUser.getUserId());
			
			if (siteUser.isAdmin()) {
				UserContext.setIsAdmin(true);
			}
		}
		
		filter.doFilter(request, response);
		UserContext.clearContext();
	}
	
	
	public void setReferenceDataSvc(ReferenceDataSvc referenceDataSvc) {
		this.referenceDataSvc = referenceDataSvc;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
