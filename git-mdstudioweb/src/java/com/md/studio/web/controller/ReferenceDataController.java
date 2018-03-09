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

import com.md.studio.domain.ReferenceData;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonView;
import com.md.studio.json.jdu.ReferenceDataJdu;
import com.md.studio.service.ReferenceDataSvc;
import com.md.studio.service.ServiceException;

@Controller
public class ReferenceDataController extends AbstractController {
	private static final String MODEL_REFDATALIST = "referenceDataList";
	private static final String MODEL_REFDATA = "referenceData";
	
	@RequestMapping(value="/processNewRefData.jctl", method=RequestMethod.POST)
	public JsonView processNewRefData(HttpSession session, HttpServletRequest request, ReferenceData referenceData, BindingResult errors) {
		checkIsAdmin(session, request);
		
		try {
			referenceDataSvc.create(referenceData);
		}
		catch (ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return new JsonView(new JsonContainer());
	}
	
	@RequestMapping(value="/updateRefData.jctl", method=RequestMethod.POST)
	public JsonView updateRefData(HttpSession session, HttpServletRequest request, ReferenceData referenceData, BindingResult errors) {
		checkIsAdmin(session, request);
		
		try {
			referenceDataSvc.update(referenceData);
		}
		catch (ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return new JsonView(new JsonContainer());
	}
	
	
	@RequestMapping(value="/getAllRefData.jctl", method=RequestMethod.POST)
	public JsonView getAllRefData(HttpSession session, HttpServletRequest request) {
		checkIsAdmin(session, request);
		
		List<ReferenceData> refData = referenceDataSvc.getAll();
		JsonContainer container = new JsonContainer();
		ReferenceDataJdu.buildJson(container, refData, MODEL_REFDATALIST);
		return new JsonView(container);
	}
	
	@RequestMapping(value="/getRefData.jctl", method=RequestMethod.POST)
	public JsonView getRefData(HttpSession session, HttpServletRequest request,
		@RequestParam(value="refType", required=false) String refType,
		@RequestParam(value="refCode", required=false) String refCode) {
		checkIsAdmin(session, request);
		
		JsonContainer container = new JsonContainer();
		if (StringUtils.isBlank(refCode)) {
			List<ReferenceData> refData = referenceDataSvc.getByRefType(refType);
			ReferenceDataJdu.buildJson(container, refData, MODEL_REFDATALIST);
		}
		else if (StringUtils.isNotBlank(refType) && StringUtils.isNotBlank(refCode)) {
			ReferenceData refData = referenceDataSvc.getRefData(refType, refCode);
			ReferenceDataJdu.buildJson(container, refData, MODEL_REFDATA);
			
		}
		
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/removeRefData.jctl", method=RequestMethod.POST)
	public JsonView deleteRefData(HttpSession session, HttpServletRequest request,
			@RequestParam(value="refType", required=false) String refType,
			@RequestParam(value="refCode", required=false) String refCode) {
		checkIsAdmin(session, request);
		
		if (StringUtils.isNotBlank(refType) || StringUtils.isNotBlank(refCode)) {
			return new JsonErrorView("REFATTRIB.NOTFOUND", "Please provide reference attributes.");
		}
		
		ReferenceData referenceData = referenceDataSvc.getRefData(refType, refCode);
		if (referenceData != null) {
			referenceDataSvc.remove(referenceData);
		}
		
		return new JsonView(new JsonContainer());
	}
	
	
}
