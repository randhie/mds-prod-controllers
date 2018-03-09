package com.md.studio.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.md.studio.domain.EventRequest;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonSuccessView;
import com.md.studio.json.JsonView;
import com.md.studio.json.jdu.EventRequestJdu;
import com.md.studio.service.EventRequestSvc;
import com.md.studio.service.ServiceException;
import com.md.studio.utils.EventRequestValidator;

@Controller
public class ContactController extends AbstractController {
	private EventRequestSvc eventRequestSvc;
	private EventRequestValidator eventRequestValidator;

	@RequestMapping(value="/requestEvent.jctl", method=RequestMethod.POST)
	public JsonView processEventRequest(HttpSession session, HttpServletRequest request,
			EventRequest eventRequest, BindingResult errors) {
		
		eventRequestValidator.validate(eventRequest, errors);
		if (errors.hasErrors()) {
			return new JsonErrorView(errors);
		}
		
		try {
			eventRequestSvc.createEventRequest(eventRequest);
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		
		return new JsonSuccessView();
	}
	
	
	@RequestMapping(value="/getAllRequestEvent.jctl", method=RequestMethod.POST)
	public JsonView getEventRequest(HttpSession session, HttpServletRequest request) {

		checkIsAdmin(session, request);

		JsonContainer container = new JsonContainer();
		List<EventRequest> eventRequestList = eventRequestSvc.selectAll();
		EventRequestJdu.buildJson(eventRequestList, container, EventRequestJdu.EVENT_REQUEST_LIST);
		return new JsonView(container);
	}
	
	
	public void setEventRequestSvc(EventRequestSvc eventRequestSvc) {
		this.eventRequestSvc = eventRequestSvc;
	}
	public void setEventRequestValidator(EventRequestValidator eventRequestValidator) {
		this.eventRequestValidator = eventRequestValidator;
	}
}
