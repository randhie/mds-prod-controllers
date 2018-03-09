package com.md.studio.web.controller;

import static com.md.studio.utils.WebConstants.*;

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
import com.md.studio.service.ServiceException;
import com.md.studio.service.SiteUserInfoSvc;
import com.md.studio.utils.PasswordValidationUtil;
import com.md.studio.utils.SvcValidationUtil;

@Controller
public class PasswordResetController extends AbstractController {
	private SiteUserInfoSvc siteUserInfo;
	
	@RequestMapping(value="/processResetPw.jctl", method=RequestMethod.POST)
	public JsonView processResetPw(HttpSession session, HttpServletRequest request,
			@RequestParam(value="emailAddress") String emailAddress) {
		
		
		if (StringUtils.isBlank(emailAddress)) {
			return new JsonErrorView("EMAIL.NOTFOUND", "Email not found");
		}
		
		SiteUser siteUser = siteUserInfo.getUserByEmail(emailAddress);
		if (siteUser == null) {
			return new JsonErrorView("CUSTOMER.NOTFOUND", "Customer not found");
		}
		
		siteUserInfo.resetPassCode(siteUser);
		return new JsonSuccessView();
	}

	
	@RequestMapping(value="/processChangePw.jctl", method=RequestMethod.POST)
	public JsonView processChangePw(HttpSession session, HttpServletRequest request,
			@RequestParam(value="currentPw") String currentPw,
			@RequestParam(value="newPw") String newPw,
			@RequestParam(value="newPwConfirm") String newPwConfirm) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);

		
		List<SvcValidationUtil> errors = PasswordValidationUtil.validatePassword(currentPw, newPw, newPwConfirm);
		if (errors != null && errors.size() > 0) {
			return new JsonErrorView(errors);
		}
		
		SiteUser siteUser = (SiteUser) session.getAttribute(SESSION_SITEUSER);
		if (siteUser != null) {
			try {
				boolean isPwValid = siteUserInfo.isPassCodeValid(siteUser.getEmailAddress(), currentPw);	
				if (!isPwValid) {
					return new JsonErrorView("PASSWORD.ERROR", "Password not match");
				}
				siteUserInfo.updatePassCode(siteUser, newPw, currentPw);
			}
			catch(ServiceException se) {
				return new JsonErrorView(se.getErrorCode(), se.getMessage());
			}
		}
		else {
			return new JsonErrorView("LOGIN.REQUIRED", "Login required");
		}
		
		return new JsonView(container);
	}
	
	public void setSiteUserInfo(SiteUserInfoSvc siteUserInfo) {
		this.siteUserInfo = siteUserInfo;
	}
}
