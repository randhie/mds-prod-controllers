package com.md.studio.web.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.googlecode.sardine.model.Response;
import com.md.studio.domain.EventInfo;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonView;
import com.md.studio.json.jdu.EventInfoJdu;
import com.md.studio.service.EventInfoSvc;
import com.md.studio.service.ServiceException;

@Controller
public class EventInfoController extends AbstractController {
	private static final String EVENTINFOLIST = "eventInfoList";
	private static final String EVENTINFO = "eventInfo";
	private static final String CHAR_ENCODING = ";charset=\"UTF-8\"";
	private EventInfoSvc eventInfoSvc;
	
	@RequestMapping(value="/getAllEventInfo.jctl", method=RequestMethod.POST)
	public JsonView getAllEventInfo(HttpSession session, HttpServletRequest request,
			@RequestParam(value="page", required=false) Integer page,
			@RequestParam(value="limit", required=false) Integer limit) {
		
		if (limit == null) {
			limit = 1000;
		}
		
		if (page == null) {
			page = 0;
		}
		
		int nextPage = limit * page;
		
		List<EventInfo> eventInfoList = eventInfoSvc.getAllEventInfo(limit + 1, nextPage);
		JsonContainer container = new JsonContainer();
		if (eventInfoList != null) {
			if (limit < eventInfoList.size()) {
				container.put("hasMore", true);
				eventInfoList.remove(eventInfoList.size() - 1);
			}
			else {
				container.put("hasMore", false);
			}
		}
		
		EventInfoJdu.buildJson(eventInfoList, container, EVENTINFOLIST);
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/getEventInfo.jctl", method=RequestMethod.POST)
	public JsonView getEventInfo (HttpSession session, HttpServletRequest request,
			@RequestParam(value="eventId", required=false) long eventId) {
		
		JsonContainer container = new  JsonContainer();
		try {
			EventInfo eventInfo = eventInfoSvc.getEventInfo(eventId);
			EventInfoJdu.buildJson(eventInfo, container, EventInfoJdu.EVENT_INFO);
		}
		catch (ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/getEventInfoFile.jctl", method=RequestMethod.POST)
	public void getEventInfoFile (HttpSession session, HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="eventId", required=false) long eventId) {
		
		try {
			EventInfo eventInfo = eventInfoSvc.getEventInfo(eventId);
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG + CHAR_ENCODING);
			response.setStatus(HttpServletResponse.SC_OK);
			
			if (eventInfo.getEventFile() == null) {
				return;
			}
			
			ServletOutputStream out = response.getOutputStream();
			out.write(eventInfo.getEventFile());
		}
		catch (ServiceException se) {
			response.setHeader(se.getErrorCode(), se.getMessage());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
//			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch (IOException io) {
			response.setHeader("INTERNAL.ERROR", io.getMessage());
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		
//		return new JsonView(container);
	}
	
	
	
	
	@RequestMapping(value="/processEventInfo.jctl", method=RequestMethod.POST)
	public JsonView processEventInfo(HttpSession session, HttpServletRequest request, MultipartHttpServletRequest multipartRequest,
			EventInfo eventInfo, BindingResult errors) {
		
		checkIsAdmin(session, request);
		
		if (StringUtils.isBlank(eventInfo.getEventName())) {
			errors.rejectValue("eventName", "REQUIRED", "Required");
		}
		else if (StringUtils.isBlank(eventInfo.getDescription())) {
			errors.rejectValue("description", "REQUIRED", "Required");
		}
		else if (eventInfo.getEventDate() == null) {
			errors.rejectValue("eventDate", "REQUIRED", "Required");	
		}
		
		if (errors.hasErrors()) {
			return new JsonErrorView(errors);	
		}
		
		try {
			
			if (multipartRequest != null && multipartRequest.getAttribute("eventFile") != null) {
				MultipartFile eventFile = multipartRequest.getFile("eventFile");
				eventInfo.setEventFile(eventFile.getBytes());
			}
			eventInfoSvc.createEvent(eventInfo);
		}
		catch (ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch(IOException io) {
			return new JsonErrorView("JAVA.ERROR", io.getMessage());
		}
		JsonContainer container = new JsonContainer();
		return new JsonView(container);
	}
	
	@RequestMapping(value="/updateEventInfo.jctl", method=RequestMethod.POST)
	public JsonView updateEventInfo (HttpSession session, HttpServletRequest request,  MultipartHttpServletRequest multipartRequest,
			EventInfo eventInfo, BindingResult errors) {
		
		checkIsAdmin(session, request);
		
		if (StringUtils.isBlank(eventInfo.getEventName())) {
			errors.rejectValue("eventName", null, "Required");
		}
		else if (StringUtils.isBlank(eventInfo.getDescription())) {
			errors.rejectValue("eventDescription", null, "Required");
		}
		else if (eventInfo.getEventDate() == null) {
			errors.rejectValue("eventDate", null, "Required");	
		}
		
		if (errors.hasErrors()) {
			return new JsonErrorView(errors);	
		}
		
		try {
			
			if (multipartRequest != null && multipartRequest.getAttribute("eventFile") != null) {
				MultipartFile eventFile = multipartRequest.getFile("eventFile");
				eventInfo.setEventFile(eventFile.getBytes());
			}
			eventInfoSvc.updateEvent(eventInfo);
		}
		catch (ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch(IOException io) {
			return new JsonErrorView("JAVA.ERROR", io.getMessage());
		}
		JsonContainer container = new JsonContainer();
		return new JsonView(container);
	}
	
	@RequestMapping(value="/removeEventInfo.jctl", method=RequestMethod.POST)
	public JsonView removeEventInfo (HttpSession session, HttpServletRequest request,
			@RequestParam(value="eventId", required=false) Long eventId) {
		
		checkIsAdmin(session, request);
		
		if (eventId == null || eventId == 0) {
			return new JsonErrorView("EVENTID.NOTFOUND", "Event Not Found.");
		}
		
		JsonContainer container = new JsonContainer();
		EventInfo eventInfo = eventInfoSvc.getEventInfo(eventId);
		if (eventInfo != null) {
			eventInfoSvc.removeEvent(eventInfo);
		}
		return new JsonView(container);
	}
	
	
	public void setEventInfoSvc(EventInfoSvc eventInfoSvc) {
		this.eventInfoSvc = eventInfoSvc;
	}
}
