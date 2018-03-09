package com.md.studio.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import com.md.studio.domain.SiteUser;
import com.md.studio.dto.SignupDto;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonSuccessView;
import com.md.studio.json.JsonView;
import com.md.studio.service.ServiceException;
import com.md.studio.service.SignupUserSvc;
import com.md.studio.utils.SiteUserValidation;

@Controller
public class SignupAndActivateController extends AbstractController {
	private static final String AUTHORIZED_ALBUM_NONE = "NONE";
	private SignupUserSvc signupUserSvc;

	@RequestMapping(value="/signup", method=RequestMethod.POST)
	public JsonView processSignup(HttpSession session, HttpServletRequest request, 
			SignupDto signupDto, BindingResult errors) {
		
		SiteUserValidation.validateSignupForm(errors, signupDto);
		if (errors.hasErrors()) {
			return new JsonErrorView(errors);
		}
		
		SiteUser siteUser = new SiteUser();
		siteUser.setEmailAddress(signupDto.getEmailAddress());
		siteUser.setFirstName(signupDto.getFirstName());
		siteUser.setLastName(signupDto.getLastName());
		siteUser.setPasscode(signupDto.getPasscode());
		siteUser.setSignupIpAddress(getIpAddress(session, request));
		siteUser.setSignupBrowser(getBrowser(session, request));
		siteUser.setAuthAlbums(AUTHORIZED_ALBUM_NONE);
		
		try {
			signupUserSvc.createUser(siteUser, signupDto.getInviteesCode());
			return new JsonSuccessView();
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
	}
	
	@RequestMapping(value="/activate", method=RequestMethod.POST)
	public JsonView activateUser(HttpSession session, HttpServletRequest request, 
			@RequestParam(value="activationCode", required=false) String activationCode) {
		
		try {
			signupUserSvc.activateUser(activationCode);
			return new JsonSuccessView();
		}
		catch(ServiceException se){
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
	}
	
	public void setSignupUserSvc(SignupUserSvc signupUserSvc) {
		this.signupUserSvc = signupUserSvc;
	}
}