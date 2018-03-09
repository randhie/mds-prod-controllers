package com.md.studio.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.md.studio.domain.PhotoGatherer;
import com.md.studio.domain.PhotoInfo;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonView;
import com.md.studio.json.jdu.PhotoGathererJdu;
import com.md.studio.service.PhotoGathererSvc;
import com.md.studio.service.ServiceException;

@Controller
public class PhotoGathererController extends AbstractController {
	private static final String ERROR_CODE_FOLDER_NOTFOUND = "FOLDER.NOTFOUND";
	private static final String ERROR_MSG_FOLDER_NOTFOUND = "Folder Not Found";
	private PhotoGathererSvc photoGathererSvc;
	
	
	@RequestMapping(value="/getAllPhotoCategories.jctl", method=RequestMethod.POST)
	public JsonView getAllPhotoCategories(HttpSession session, HttpServletRequest request,
			@RequestParam(value="categoryType") Integer categoryType) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		try {
			List<String> photoCategories = photoGathererSvc.getAllPhotoCategory(categoryType);	
			if (photoCategories != null && !photoCategories.isEmpty()) {
				container.put("photoCategories", photoCategories);
			}
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/getAllPhotoCoversByCategory.jctl", method=RequestMethod.POST)
	public JsonView getAllPhotoCoversByCategory(HttpSession session, HttpServletRequest request,
			@RequestParam(value="category", required=false) String category) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		try {
			List<PhotoGatherer> photoGathererList = photoGathererSvc.getAllCovers(category);
			if (photoGathererList != null && !photoGathererList.isEmpty()) {
				filterDirectories(session, request, photoGathererList);
				PhotoGathererJdu.buildJson(container, photoGathererList, "photoGathererList", false);
			}
		}
		catch (ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return new JsonView(container);
	}
	
	@RequestMapping(value="/getAllPhotoss.jctl", method=RequestMethod.POST)
	public JsonView getAllPhotos(HttpSession session, HttpServletRequest request,
			@RequestParam(value="category", required=false) String category,
			@RequestParam(value="directory", required=false) String directory,
			@RequestParam(value="page", required=false) Integer page,
			@RequestParam(value="limit", required=false) Integer limit) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		container.put("isAllowedToView", false);
		
		if (isPhotoAllowed(session, directory)) {
			try {
				if (page == null) {
					page = 0;
				}
				if (limit == null) {
					limit = 0;
				}
				
				if (page > 0) {
					page --;
				}
				
				int offset = page * limit;
				
				PhotoGatherer photoGatherer = photoGathererSvc.getAllPhotos(category, directory, offset, limit + 1, page, false);
				if (photoGatherer != null) {
					container.put("isAllowedToView", true);
					container.put("hasMore", false);
					
					if (photoGatherer.getPhotoInfoList() != null && !photoGatherer.getPhotoInfoList().isEmpty()) {
						if (limit != 0 && photoGatherer.getPhotoInfoList().size() > limit) {
							int lmit = limit;
							photoGatherer.getPhotoInfoList().remove(lmit);
							container.put("hasMore", true);
						}
					}
					PhotoGathererJdu.buildJson(container, photoGatherer, "photoGatherer", false);	
				}
			}
			catch (ServiceException se) {
				return new JsonErrorView(se.getErrorCode(), se.getMessage());
			}
			catch (Exception e) {
				e.printStackTrace();
			}	
		}
		
		return new JsonView(container);
	}
	

	@RequestMapping(value="/getAllPreviewPhotos.jctl", method=RequestMethod.POST)
	public JsonView getAllPreviewPhotos(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		List<PhotoGatherer> photoGatherer = photoGathererSvc.getAllPreviewPhotos();
		PhotoGathererJdu.buildJson(container, photoGatherer, "photoGatherer", false);
		return new JsonView(container);
		
	}
	
	
	@RequestMapping(value="/getPhotoByCategoryType.jctl", method=RequestMethod.POST)
	public JsonView getPhotoByCategoryType(HttpSession session, HttpServletRequest request, 
			@RequestParam(value="category", required=false) String category,
			@RequestParam(value="categoryType", required=false) Integer categoryType,
			@RequestParam(value="photoType[]", required=false) Integer[] photoType,
			@RequestParam(value="onlyList", required=false, defaultValue="true") Boolean onlyList) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		try {
			if (onlyList == null) {
				onlyList = true;
			}
			
			List<PhotoGatherer> photoGatherer = photoGathererSvc.getPhotoGatherer(category, categoryType, photoType);
			PhotoGathererJdu.buildJson(container, photoGatherer, "photoGatherer", onlyList);
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return new JsonView(container);
		
	}
	
	
	@RequestMapping(value="/getPhotoCoversByCategoryId.jctl", method=RequestMethod.POST)
	public JsonView getPhotoCoversByCategoryId(HttpSession session, HttpServletRequest request, 
			@RequestParam(value="categoryId", required=false) Integer categoryId,
			@RequestParam(value="photoType[]", required=false) Integer[] photoType,
			@RequestParam(value="onlyList", required=false, defaultValue="true") Boolean onlyList) {
		
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		try {
			if (onlyList == null) {
				onlyList = true;
			}
			
			List<PhotoGatherer> photoGatherer = photoGathererSvc.getPhotoGatherer(categoryId, photoType);
			PhotoGathererJdu.buildJson(container, photoGatherer, "photoGatherer", onlyList);
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return new JsonView(container);
		
	}
	
	
	public void setPhotoGathererSvc(PhotoGathererSvc photoGathererSvc) {
		this.photoGathererSvc = photoGathererSvc;
	}
}
