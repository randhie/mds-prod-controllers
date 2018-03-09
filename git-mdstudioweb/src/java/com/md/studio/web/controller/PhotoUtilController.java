package com.md.studio.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonView;
import com.md.studio.service.PhotoUtilSvc;

@Controller
public class PhotoUtilController extends AbstractController{
	private PhotoUtilSvc photoUtilSvc;
	private static final String MSGRESULT = "resultMsg";
	private static final String COUNTTEMPFILES = "countTempFiles";
	
	@RequestMapping(value="/processTempFolders.jctl", method=RequestMethod.POST)
	public JsonView processTempFolders(HttpSession session, HttpServletRequest request,
			@RequestParam(value="category")String category) {
		
		JsonContainer container = new JsonContainer();
		checkIsAdmin(session, request);
		
		String resultMsg = photoUtilSvc.findAndProcessPhotos(category);
		container.put(MSGRESULT, resultMsg);
		return new JsonView(container);
	}
	
	@RequestMapping(value="/processTempSlideShowFolder.jctl", method=RequestMethod.POST)
	public JsonView processTempSlideShowFolder(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		String resultMsg = photoUtilSvc.FindAndProcessSlidePhotos();
		container.put(MSGRESULT, resultMsg);
		return new JsonView(container);
	}
	
	@RequestMapping(value="/countTempFolderFiles.jctl", method=RequestMethod.POST)
	public JsonView countTempFolderFiles(HttpSession session, HttpServletRequest request,
			@RequestParam(value="folderType", required=false) String folderType) {
		JsonContainer container = new JsonContainer();
		checkIsAdmin(session, request);
		
		int countFiles = photoUtilSvc.countAllPhotosToProcess(folderType);
		container.put(COUNTTEMPFILES, countFiles);
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/processPortofolioPhotos.jctl", method=RequestMethod.POST)
	public JsonView processPortfolioPhotos(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkIsAdmin(session, request);
		
		try {
			int totalRecords = photoUtilSvc.cleanupPortfolio();
			container.put("totalRecords", totalRecords);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return new JsonView(container);
	}

	@RequestMapping(value="/processPreviewPhotos.jctl", method=RequestMethod.POST)
	public JsonView processPreviewPhotos(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkIsAdmin(session, request);
		
		int totalRecords = photoUtilSvc.cleanupPreviewPhotos();
		container.put("totalRecords", totalRecords);
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/cleanupData.jctl", method=RequestMethod.POST)
	public JsonView cleanupData(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkIsAdmin(session, request);
		
		photoUtilSvc.cleanupData();
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/cleanPhotoArchives.jctl", method=RequestMethod.POST)
	public JsonView cleanPhotoArchives(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkIsAdmin(session, request);
		
		int totalRecords = photoUtilSvc.cleanupPhotoArchives();
		container.put("totalRecords", totalRecords);
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/getTotalRecordsProcessed.jctl", method=RequestMethod.POST)
	public JsonView getTotalRecordsProcessed(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkIsAdmin(session, request);
		
		int totalRecords = photoUtilSvc.getTotalRecordsProcessed();
		container.put("totalRecords", totalRecords);
		return new JsonView(container);
	}
	
	public void setPhotoUtilSvc(PhotoUtilSvc photoUtilSvc) {
		this.photoUtilSvc = photoUtilSvc;
	}
}
