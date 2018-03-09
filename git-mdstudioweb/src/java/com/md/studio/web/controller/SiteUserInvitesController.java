package com.md.studio.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonView;
import com.md.studio.service.SiteUserInfoSvc;
import com.md.studio.service.SiteUserInviteSvc;

@Controller
public class SiteUserInvitesController extends AbstractController {
	private SiteUserInviteSvc siteUserInviteSvc;
	private SiteUserInfoSvc siteUserInfoSvc;

	public JsonView inviteUser(HttpSession session, HttpServletRequest request, 
			@RequestParam(value="emailAddress") String emailAddress) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);

		
		
		
		return new JsonView(container);
	}
	
	
	
	public void setSiteUserInviteSvc(SiteUserInviteSvc siteUserInviteSvc) {
		this.siteUserInviteSvc = siteUserInviteSvc;
	}
	public void setSiteUserInfoSvc(SiteUserInfoSvc siteUserInfoSvc) {
		this.siteUserInfoSvc = siteUserInfoSvc;
	}
}
