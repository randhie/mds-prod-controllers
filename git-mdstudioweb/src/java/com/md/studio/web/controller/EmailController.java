package com.md.studio.web.controller;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.md.studio.domain.EmailMessage;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonView;
import com.md.studio.service.EmailSenderSvc;
import com.md.studio.service.ServiceException;
import com.md.studio.utils.EmailValidationUtil;

@Controller
public class EmailController extends AbstractController{
	private EmailValidationUtil emailValidationUtil;
	private EmailSenderSvc emailSenderSvc;

	
	@RequestMapping(value="/processEmail.jctl", method=RequestMethod.POST)
	public JsonView processEmail(HttpSession session, HttpServletRequest request, MultipartHttpServletRequest fileRequest,
			EmailMessage emailMessage, BindingResult errors){
		
		checkLogin(session, request);
		
		emailValidationUtil.validate(emailMessage, errors);
		while(errors.hasErrors()) {
			return new JsonErrorView(errors);
		}

		List<MultipartFile> attachments = fileRequest.getFiles("attachments");
		try {
			emailMessage.setUseShawMail(true);
			emailSenderSvc.sendEmail(emailMessage, attachments);
		}
		catch (Exception e) {
			if (e instanceof ServiceException) {
				return new JsonErrorView("EMAIL.ERROR", e.getMessage());
			}
		}
		return new JsonView(new JsonContainer());
	}
	
	
	public void setEmailValidationUtil(EmailValidationUtil emailValidationUtil) {
		this.emailValidationUtil = emailValidationUtil;
	}
	public void setEmailSenderSvc(EmailSenderSvc emailSenderSvc) {
		this.emailSenderSvc = emailSenderSvc;
	}
}
