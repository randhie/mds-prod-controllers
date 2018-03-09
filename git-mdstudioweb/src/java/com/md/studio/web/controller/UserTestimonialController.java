package com.md.studio.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.md.studio.domain.Testimonials;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonView;
import com.md.studio.json.jdu.UserTestimonialJdu;
import com.md.studio.service.UserTestimonialSvc;
import com.md.studio.utils.ValidationUtil;

@Controller
public class UserTestimonialController extends AbstractController {
	private static final String HAS_MORE = "hasMore";
	private static final String USERTESTIMONIAL = "userTestimonialList";
	private UserTestimonialSvc userTestimonialSvc;
	private ValidationUtil validationUtil;

	@RequestMapping(value="/getAllTestimonial.jctl", method=RequestMethod.POST)
	public JsonView getAllTestimonial(HttpSession session, HttpServletRequest request,
			@RequestParam(value="page", required=false) Integer page,
			@RequestParam(value="limit", required=false) Integer limit) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		if (page == null) {
			page = 1;
		}
		
		if (limit == null) {
			limit = 5000;
		}
		
		int offset = limit * (page - 1);
		List<Testimonials> testimonialList = userTestimonialSvc.getAllByFilter(limit + 1, offset);
		int totalList = testimonialList.size();
		
		if (totalList > limit) {
			testimonialList.remove(totalList-1);
			container.put(HAS_MORE, true);
		}

		UserTestimonialJdu.buildJson(container, testimonialList, USERTESTIMONIAL);
		return new JsonView(container);
	}
	
	@RequestMapping(value="/createTestimonial.jctl", method=RequestMethod.POST)
	public JsonView createTestimonal(HttpSession session, HttpServletRequest request, 
			Testimonials testimonials, BindingResult errors) {
		
		validationUtil.validate(testimonials, errors);
		if (errors.hasErrors()) {
			return new JsonErrorView(errors);
		}
		
		testimonials.setBrowserInfo(getBrowser(session, request));
		testimonials.setIpAddress(getIpAddress(session, request));
		userTestimonialSvc.createTestimonial(testimonials);
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		return new JsonView(container);
	}
	
	@RequestMapping(value="/updateTestimonial.jctl", method=RequestMethod.POST)
	public JsonView updateTestimonial(HttpSession session, HttpServletRequest request, 
			Testimonials testimonials, BindingResult errors) {
		
		validationUtil.validate(testimonials, errors);
		if (errors.hasErrors()) {
			return new JsonErrorView(errors);
		}
		userTestimonialSvc.updateTestimonial(testimonials);
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		return new JsonView(container);
	}
	

	@RequestMapping(value="/removeTestimonial.jctl", method=RequestMethod.POST)
	public JsonView removeTestimonial(HttpSession session, HttpServletRequest request,
			@RequestParam(value="testimonialId", required=false) String testimonialId) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		if (StringUtils.isBlank(testimonialId)) {
			container.put("isSuccess", false);
			container.put("ID.NOTFOUND", "Missing Testimonial ID");
		}
		
		userTestimonialSvc.removeTestimonial(testimonialId);
		return new JsonView(container);
	}
	
	@RequestMapping(value="/removeAllTestimonial.jctl", method=RequestMethod.POST)
	public JsonView removeAllTestimonial(HttpSession session, HttpServletRequest request,
			@RequestParam(value="emailAddress", required=false) String emailAddress) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		if (StringUtils.isBlank(emailAddress)) {
			container.put("isSuccess", false);
			container.put("ID.NOTFOUND", "Missing EmailAddress");
		}
		
		userTestimonialSvc.removeAllTestimonial(emailAddress);
		return new JsonView(container);
	}
	
	
	public void setUserTestimonialSvc(UserTestimonialSvc userTestimonialSvc) {
		this.userTestimonialSvc = userTestimonialSvc;
	}
	public void setValidationUtil(ValidationUtil validationUtil) {
		this.validationUtil = validationUtil;
	}
}
