package com.md.studio.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.md.studio.domain.PhotoUploadDirectory;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonView;
import com.md.studio.json.jdu.PhotoUploadDirectoryJdu;
import com.md.studio.service.PhotoUploadDirSvc;
import com.md.studio.service.PhotoUtilSvc;
import com.md.studio.service.ServiceException;

@Controller
public class PhotoUploadController extends AbstractController {
	private PhotoUtilSvc photoUtilSvc;
	
	@RequestMapping(value="/getAllValidPhotoUploadDir.jctl", method=RequestMethod.POST)
	public JsonView getAllValidPhotoUploadDir(HttpSession session, HttpServletRequest request) {
		
		checkLogin(session, request);

		List<PhotoUploadDirectory> photoUploadDir = photoUploadDirSvc.getValidPhotoUploadDir();
		List<PhotoUploadDirectory> filteredUploadDir = filterUploadDirectory(session, photoUploadDir);
		if (filteredUploadDir == null) {
			filteredUploadDir = new ArrayList<PhotoUploadDirectory>();
		}
		
		JsonContainer container = new JsonContainer();
		PhotoUploadDirectoryJdu.buildJson(filteredUploadDir, container, PhotoUploadDirectoryJdu.PHOTO_UPLOAD_DIRECTORY_LIST);
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/processUploadFile.jctl", method=RequestMethod.POST)
	public JsonView processUploadFile(HttpSession session, HttpServletRequest request,
			MultipartHttpServletRequest fileRequest,
			@RequestParam(value="directory", required=false) Long photoUploadId) {
		
		
		checkLogin(session, request);
		
		if (fileRequest == null) {
			return new JsonErrorView("FILE.NOTFOUND", "Cannot find any file to upload");
		}
		else if (photoUploadId == null) {
			return new JsonErrorView("AUTHDIR.NOTFOUND", "Authorized Directory to upload not found");
		}
		
		checkAuthUpload(request, session, photoUploadId);

		JsonContainer container = new JsonContainer();
		try {
			List<MultipartFile> photos = fileRequest.getFiles("fileUploaded");
			photoUtilSvc.uploadPhoto(photos, photoUploadId);
		}
		catch(ServiceException se) {
			return new JsonErrorView(se.getErrorCode(), se.getMessage());
		}
		
		return new JsonView(container);
	}
	
	
	@RequestMapping(value="/createPhotoUploadDir.jctl", method=RequestMethod.POST)
	public JsonView createPhotoUploadDir(HttpSession session, HttpServletRequest request,
			PhotoUploadDirectory photoUploadDir, BindingResult errors){
		
		return null;
	}
	
	
	
	
	
	public void setPhotoUtilSvc(PhotoUtilSvc photoUtilSvc) {
		this.photoUtilSvc = photoUtilSvc;
	}
}
