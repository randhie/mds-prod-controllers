package com.md.studio.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonView;
import com.md.studio.service.RefreshManagerSvc;

@Controller
public class PhotoRefreshMgtController extends AbstractController{
	private static final String PHOTOSVC = "photoInfoNewSvc";
	private static final String REFERENCEDATASVC = "referenceDataSvc";
	private static final String EMAILTEMPLATESVC = "emailTemplateSvc";
	private static final String PHOTOGATHERERSVC = "photoGathererSvc";
	private RefreshManagerSvc refreshManagerSvc;
	
	@RequestMapping(value="/refreshPhotoSvc.pics")
	public JsonView refreshPhotoInfoSvc(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		refreshManagerSvc.processRefreshRequest(PHOTOSVC);
		
		return new JsonView(container);
	}
	
	@RequestMapping(value="/refreshReferenceData.jctl")
	public JsonView refreshReferenceDataSvc(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		refreshManagerSvc.processRefreshRequest(REFERENCEDATASVC);
		
		return new JsonView(container);
	}
	
	@RequestMapping(value="/refreshEmailTemplates.jctl")
	public JsonView refreshEmailTemplateSvc(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		refreshManagerSvc.processRefreshRequest(EMAILTEMPLATESVC);
		
		return new JsonView(container);
	}
	
	@RequestMapping(value="/photoGathererRefreshData.jctl")
	public JsonView refreshPhotoGathererSvc(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		refreshManagerSvc.processRefreshRequest(PHOTOGATHERERSVC);
		
		return new JsonView(container);
	}
	
	
	
	public void setRefreshManagerSvc(RefreshManagerSvc refreshManagerSvc) {
		this.refreshManagerSvc = refreshManagerSvc;
	}
}
