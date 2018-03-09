package com.md.studio.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.md.studio.domain.SiteUser;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonSuccessView;
import com.md.studio.json.JsonView;
import com.md.studio.json.jdu.SiteUserInfoJdu;
import com.md.studio.service.ServiceException;
import com.md.studio.service.SiteUserInfoSvc;
import com.md.studio.utils.PasswordValidationUtil;
import com.md.studio.utils.SvcValidationUtil;

@Controller
public class SiteUserInfoController extends AbstractController {
	private static final String MODEL_SITEUSERINFOLIST = "siteUserInfoList";
	private static final String MODEL_SITEUSERINFO = "siteUserInfo";
	private SiteUserInfoSvc siteUserInfoSvc;
	
	
	@RequestMapping(value="/getAllSiteUser.jctl", method=RequestMethod.POST)
	public JsonView getAllSiteUser(HttpSession session, HttpServletRequest request) {
		checkIsAdmin(session, request);
		
		List<SiteUser> siteUser = siteUserInfoSvc.getAllSiteUser();
		JsonContainer container = new JsonContainer();
		SiteUserInfoJdu.buildJson(container, siteUser, MODEL_SITEUSERINFOLIST, SiteUserInfoJdu.BASIC_USERINFO);
		
		return new JsonView(container);
	}

	
	@RequestMapping(value="/getSiteUser.jctl", method=RequestMethod.POST)
	public JsonView getSiteUser(HttpSession session, HttpServletRequest request,
			@RequestParam(value="userId", required=false) String userId) {
		checkIsAdmin(session, request);
		
		if (StringUtils.isBlank(userId)) {
			return new JsonErrorView("USERID.NOTFOUND", "User ID Not found");
		}
		
		SiteUser siteUser = siteUserInfoSvc.getUserById(userId);
		JsonContainer container = new JsonContainer();
		SiteUserInfoJdu.buildJson(container, siteUser, MODEL_SITEUSERINFO, SiteUserInfoJdu.ALL_USERINFO);
		
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/updateSiteUserPassword.jctl", method=RequestMethod.POST)
	public JsonView updateSiteUserPassword(HttpSession session, HttpServletRequest request,
			@RequestParam(value="userId", required=false) String userId,
			@RequestParam(value="newPassCode", required=false) String newPassCode,
			@RequestParam(value="newPassCodeConfirm", required=false) String newPassCodeConfirm) {
		checkIsAdmin(session, request);
		
		SiteUser siteUser = siteUserInfoSvc.getUserById(userId);
		if (siteUser == null) {
			return new JsonErrorView("USER.NOTFOUND", "Site User not found");
		}
		List<SvcValidationUtil> errors = PasswordValidationUtil.validatePassword(siteUser.getPasscode(), newPassCode, newPassCodeConfirm);
		if (errors != null && !errors.isEmpty()) {
			return new JsonErrorView(errors);
		}
		
		try {
			siteUserInfoSvc.updatePassCodeByAdmin(siteUser, newPassCodeConfirm);
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return new JsonSuccessView();
	}

	
	@RequestMapping(value="/updateAuthorizedAlbum.jctl", method=RequestMethod.POST)
	public JsonView updateAuthorizedAlbum(HttpSession session, HttpServletRequest request,
			@RequestParam(value="userId", required=false) String userId,
			@RequestParam(value="authAlbums", required=false) String authAlbums) {
		checkIsAdmin(session, request);
		
		if (StringUtils.isBlank(userId)) {
			return new JsonErrorView("USERID.NOTFOUND", "User ID Not found");
		}
		
		SiteUser siteUser = siteUserInfoSvc.getUserById(userId);
		try {
			siteUserInfoSvc.updateAuthorizedAlbum(siteUser, authAlbums);
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return new JsonSuccessView();
	}

	
	@RequestMapping(value="/updateSiteUserInfo.jctl", method=RequestMethod.POST)
	public JsonView updateSiteUserInfo(HttpSession session, HttpServletRequest request,
			@RequestParam(value="userId", required=false) String userId,
			@RequestParam(value="emailAddress", required=false) String emailAddress,
			@RequestParam(value="firstName", required=false) String firstName,
			@RequestParam(value="lastName", required=false) String lastName,
			@RequestParam(value="isAdmin", required=false) Boolean isAdmin) {
		checkIsAdmin(session, request);
		
		if (StringUtils.isBlank(userId)) {
			return new JsonErrorView("USERID.NOTFOUND", "User ID Not found");
		}
		
		SiteUser siteUser = new SiteUser();
		siteUser.setUserId(userId);
		siteUser.setEmailAddress(emailAddress);
		siteUser.setFirstName(firstName);
		siteUser.setLastName(lastName);
		siteUser.setAdmin(isAdmin);
		try {
			siteUserInfoSvc.updateSiteUserInfo(siteUser);
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return new JsonSuccessView();
	}

	
	
	public void setSiteUserInfoSvc(SiteUserInfoSvc siteUserInfoSvc) {
		this.siteUserInfoSvc = siteUserInfoSvc;
	}
}
