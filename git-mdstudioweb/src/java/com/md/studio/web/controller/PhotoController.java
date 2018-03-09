package com.md.studio.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.md.studio.dto.Photo;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonView;
import com.md.studio.json.jdu.PhotoJdu;
import com.md.studio.service.PhotoInfoNewSvc;
import com.md.studio.service.ServiceException;

@Controller
public class PhotoController extends AbstractController{
	private static final String ERROR_CODE_FOLDER_NOTFOUND = "FOLDER.NOTFOUND";
	private static final String ERROR_MSG_FOLDER_NOTFOUND = "Folder Not Found";
	private PhotoInfoNewSvc photoInfoNewSvc;
	
	@RequestMapping(value="/getAllCoverPhotos.jctl", method=RequestMethod.POST)
	public JsonView getAllCoverPhotos(HttpSession session, HttpServletRequest request,
			@RequestParam(value="isFiltered", required=false, defaultValue="false") boolean isFiltered){
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		try {
			Map<String, List<Photo>> photoList = photoInfoNewSvc.getAllPhotoCategories();
			if (isFiltered) {
				Map<String, List<Photo>> processedPhotoList = assembleAllowedPhotos(session, request, photoList);
				container.put("photoCoverList", processedPhotoList);
			}
			else {
				Set<String> keys = photoList.keySet();
				container.put("photoCoverList",keys);	
			}
			
			return new JsonView(container);	
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
	}
	
	@RequestMapping(value="/getAllCoverPhotosByParent.jctl", method=RequestMethod.POST)
	public JsonView getAllCoverPhotosByParent(HttpSession session, HttpServletRequest request,
			@RequestParam(value="parentDirectory") String parentDirectory) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		try {
			List<Photo> coverPhotoList = photoInfoNewSvc.getAllPhotoCover(parentDirectory);
			if (coverPhotoList == null || coverPhotoList.isEmpty()) {
				coverPhotoList = new ArrayList<Photo>();
			}
			
			List<Photo> processedPhotoList = assembleAllowedPhotos(session, request, coverPhotoList);
			PhotoJdu.buildJson(processedPhotoList, container);
			container.removeJsonAttribs("captionName");
			container.removeJsonAttribs("albumDescription");
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		return new JsonView(container);
	}
	
	@RequestMapping(value="/getAllSlidePhotos.jctl", method=RequestMethod.POST)
	public JsonView getAllSlidePhotos(HttpSession session, HttpServletRequest request) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		try {
			List<Photo> photoList = photoInfoNewSvc.getSlidePhotos();
			PhotoJdu.buildJson(photoList, container);
			return new JsonView(container);	
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		
	}
	
	@RequestMapping(value="/getAllPreviewNewPhotos.jctl", method=RequestMethod.POST)
	public JsonView getAllPreviewNewPhotos(HttpSession session, HttpServletRequest request) {

		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		try {
			List<Photo> photoList = photoInfoNewSvc.getPreviewNewPhotos();
			PhotoJdu.buildJson(photoList, container);
			return new JsonView(container);	
		}
		catch(ServiceException se){
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
	}
	
	@RequestMapping(value="/getAllPhotos.jctl", method=RequestMethod.POST)
	public JsonView getAllPhotosFromDir(HttpSession session, HttpServletRequest request,
			@RequestParam(value="directoryName") String directoryName) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		if (StringUtils.isBlank(directoryName)) {
			return new JsonErrorView(ERROR_CODE_FOLDER_NOTFOUND, ERROR_MSG_FOLDER_NOTFOUND);
		}
		String directoryFinal = StringUtils.removeStart(directoryName, "_");
		directoryName = StringUtils.replaceOnce(directoryFinal, "_", "/");
		
		//directory format : /parent/child/
		/*List<Photo> photoList = photoInfoSvc.getAllPhotos(directoryName);*/
		List<Photo> photoList = photoInfoNewSvc.getAllPhotosByDirectory(directoryName);
		
		List<Photo> processedPhotos = assembleAllowedPhotos(session, request, photoList);
		PhotoJdu.buildJson(processedPhotos, container);
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/getAllPortfolio.jctl", method=RequestMethod.POST)
	public JsonView getAllPortfolio(HttpSession session, HttpServletRequest request) {

		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		try {
			List<Photo> photoList = photoInfoNewSvc.getAllPortfolio();
			PhotoJdu.buildJson(photoList, container);
			return new JsonView(container);	
		}
		catch(ServiceException se){
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
	}
	
	
	@RequestMapping(value="/getPortfolioByDirectory.jctl", method=RequestMethod.POST)
	public JsonView getPortfolioByDirectory(HttpSession session, HttpServletRequest request,
			@RequestParam(value="parentChildFolder", required=false) String parentChildFolder) {

		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		try {
			List<Photo> photoList = photoInfoNewSvc.getPortfolioByDirectory(parentChildFolder);
			PhotoJdu.buildJson(photoList, container);
			return new JsonView(container);	
		}
		catch(ServiceException se){
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
	}
	

	public void setPhotoInfoNewSvc(PhotoInfoNewSvc photoInfoNewSvc) {
		this.photoInfoNewSvc = photoInfoNewSvc;
	}
}
