package com.md.studio.web.controller;
import static com.md.studio.utils.WebConstants.*;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.md.studio.domain.SiteUser;
import com.md.studio.domain.UserContext;
import com.md.studio.event.SiteUserEvent;
import com.md.studio.event.SiteUserEventPublisher;
import com.md.studio.event.SiteUserEventType;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonView;
import com.md.studio.service.ServiceException;
import com.md.studio.service.SiteUserInfoSvc;

@Controller
public class SiteUserSessionController extends AbstractController {
	private SiteUserInfoSvc siteUserInfoSvc;
	private SiteUserEventPublisher siteUserEventPublisher;

	@RequestMapping(value="/login", method=RequestMethod.POST)
	public JsonView login(HttpSession session, HttpServletRequest request,
			@RequestParam(value="userName") String userName,
			@RequestParam(value="passCode") String passCode) {
		
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(passCode)) {
			return new JsonErrorView("LOGIN.ERROR", "Username and / or password invalid");
		}
		
		try {
			SiteUser siteUser = siteUserInfoSvc.getUserByEmail(userName);
			if (siteUser == null) {
				return new JsonErrorView("LOGIN.ERROR", "Username and / or password invalid");
			}
			boolean isPassCodeValid = siteUserInfoSvc.isPassCodeValid(siteUser.getEmailAddress(), passCode);
			if (isPassCodeValid) {
				session.setAttribute(SESSION_SITEUSER, siteUser);
				
				siteUser.setLastLogin(new Date());
				siteUser.setLoginIpAddress(getIpAddress(session, request));
				siteUser.setLastLoginBrowser(getBrowser(session, request));
				siteUserInfoSvc.updateLastLogin(siteUser);
				
				SiteUserEvent siteUserEvent = new SiteUserEvent();
				siteUserEvent.setEventType(SiteUserEventType.CLEAN_SITEUSER_AUTHUPLOADS);
				siteUserEvent.setEventObject(siteUser);
				siteUserEvent.setUserId(siteUser.getUserId());
				siteUserEventPublisher.publishEvent(siteUserEvent);
			}
			else {
				return new JsonErrorView("LOGIN.ERROR", "Username and / or password invalid");
			}
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		JsonContainer container = new JsonContainer();
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/logout", method=RequestMethod.POST)
	public JsonView logout(HttpSession session, HttpServletRequest request) {
		session.removeAttribute(SESSION_SITEUSER);
		session.getAttribute(SESSION_PROCESSED_PHOTOLIST);
		UserContext.clearContext();
		
		JsonContainer container = new JsonContainer();
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/checkLoginSession.jctl", method=RequestMethod.POST)
	public JsonView checkLoginSession(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		return new JsonView(container);
	}
	
	public void setSiteUserInfoSvc(SiteUserInfoSvc siteUserInfoSvc) {
		this.siteUserInfoSvc = siteUserInfoSvc;
	}


	public void setSiteUserEventPublisher(
			SiteUserEventPublisher siteUserEventPublisher) {
		this.siteUserEventPublisher = siteUserEventPublisher;
	}
}
