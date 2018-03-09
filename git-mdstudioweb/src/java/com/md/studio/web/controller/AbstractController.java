package com.md.studio.web.controller;
import static com.md.studio.utils.WebConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.md.studio.domain.PhotoGatherer;
import com.md.studio.domain.PhotoUploadDirectory;
import com.md.studio.domain.ReferenceData;
import com.md.studio.domain.SiteUser;
import com.md.studio.dto.Photo;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonData;
import com.md.studio.json.JsonView;
import com.md.studio.service.PhotoUploadDirSvc;
import com.md.studio.service.ReferenceDataSvc;
import com.md.studio.web.util.UserNotAuthorizedException;

public abstract class AbstractController {
	public static final String MSG_USERNOTAUTHORIZED = "User not authorized for requested page";
	public static final String HTTPHEADER_ERRMSG = "errmsg";
	private static final String HEADER_XFORWARDEDFOR = "X-Forwarded-For";
	private static final String HEADER_USERAGENT = "user-agent";
	private static final String X_FORWARDED_HEADER_DELIM = ",";
	private static final String AUTH_ALL_FOLDER = "ALL";
	private static final String C_SPACE = " ";	
	
	protected ReferenceDataSvc referenceDataSvc;
	protected PhotoUploadDirSvc photoUploadDirSvc;
	
	
	protected void filterDirectories(HttpSession session, HttpServletRequest request, List<PhotoGatherer> photoGatherer) {
		ReferenceData refData = referenceDataSvc.getRefData(REFTYPE_RESTRICTED, REFCODE_RESTRICTED_VIEW);
		SiteUser siteUser = (SiteUser) session.getAttribute(SESSION_SITEUSER);
		
		if (refData != null) {
			if (siteUser != null) {
				if (StringUtils.isNotBlank(siteUser.getAuthAlbums()) && siteUser.getAuthAlbums().contains(AUTH_ALL_FOLDER)) {
					return;
				}
				else if (StringUtils.isNotBlank(siteUser.getAuthAlbums())) {
					if (StringUtils.isNotBlank(refData.getRefValue())) {
						
						List<String> restrictedDirectories = refData.getParsePipeRefValue();
						StringBuilder filteredRestrictedDirectories = new StringBuilder();
						
						for (String resDir: restrictedDirectories) {
							if (siteUser.getAuthAlbums().contains(resDir)) {
								continue;
							}
							filteredRestrictedDirectories.append(resDir.trim());
							filteredRestrictedDirectories.append(C_SPACE);
						}
						restrictedDirectories.removeAll(restrictedDirectories);
						
						Iterator<PhotoGatherer> pg = photoGatherer.iterator();
						while (pg.hasNext()) {
							if (filteredRestrictedDirectories.toString().contains(pg.next().getDirectory())) {
								pg.remove();
							}
						}
						filteredRestrictedDirectories = null;
					}
				}
				else {
					Iterator<PhotoGatherer> pg = photoGatherer.iterator();
					while (pg.hasNext()) {
						if (StringUtils.isNotBlank(refData.getRefValue()) && refData.getRefValue().contains(pg.next().getDirectory())) {
							pg.remove();
						}
					}
				}
			}
			else {
				Iterator<PhotoGatherer> pg = photoGatherer.iterator();
				while (pg.hasNext()) {
					if (StringUtils.isNotBlank(refData.getRefValue()) && refData.getRefValue().contains(pg.next().getDirectory())) {
						pg.remove();
					}
				}
			}
		}
		
	}
	
	
	
	protected String getIpAddress(HttpSession session, HttpServletRequest request) {
		String forwardedFor = request.getHeader(HEADER_XFORWARDEDFOR);
		String ipAddress = null;
		
		if (forwardedFor == null || forwardedFor.isEmpty()) {
			ipAddress = request.getRemoteAddr();
		}
		else {
			if (forwardedFor.contains(X_FORWARDED_HEADER_DELIM)) {
				ipAddress = forwardedFor.substring(0, forwardedFor.indexOf(X_FORWARDED_HEADER_DELIM)).trim();
			}
			else {
				ipAddress = forwardedFor.trim();
			}
		}
		return ipAddress;
	}
	
	protected String getBrowser(HttpSession session, HttpServletRequest request) {
		return request.getHeader(HEADER_USERAGENT);
	}
	
	// it will be deprecated
	protected Map<String, List<Photo>> assembleAllowedPhotos(HttpSession session, HttpServletRequest request, Map<String, List<Photo>> photoList) {
		
		Set<String> keys = photoList.keySet();
		Map<String, List<Photo>> processedPhotoList = new HashMap<String, List<Photo>>();
		
		for (String key: keys) {
			processedPhotoList.put(key, assembleAllowedPhotos(session, request, photoList.get(key)));
		}
		
		photoList.clear();
		return processedPhotoList;
	}
	
	// it will be deprecated
	protected List<Photo> assembleAllowedPhotos(HttpSession session, HttpServletRequest request, List<Photo> photoList) {
		SiteUser siteUser = (SiteUser) session.getAttribute(SESSION_SITEUSER);
		ReferenceData refData = referenceDataSvc.getRefData(REFTYPE_RESTRICTED, REFCODE_RESTRICTED_VIEW);
		
		if (siteUser != null) {
			
			if (siteUser.getAuthAlbums().contains(AUTH_ALL_FOLDER)) {
				return photoList;
			}
			
			if (refData != null) {
				if (!StringUtils.isBlank(refData.getRefValue()) &&
						refData.getParsePipeRefValue() != null && 
						!refData.getParsePipeRefValue().isEmpty()) {
					List<Photo> rawPhotoList = new ArrayList<Photo>();
					rawPhotoList.addAll(photoList);
					
					for (Photo photos: photoList) {
						if (refData.getRefValue().contains(photos.getDirectoryName())) {
							if (StringUtils.isBlank(siteUser.getAuthAlbums())) {
								rawPhotoList.remove(photos);
								continue;
							}
							else if (siteUser.getAuthAlbums().contains(photos.getDirectoryName())) {
								continue;
							}
							rawPhotoList.remove(photos);
						}
					}
					return rawPhotoList;
				}
				else {
					return photoList;
				}
			}
		}
		return removeRestrictedFolders(refData,  session, photoList);
	}
	
	// it will be deprecated
	private List<Photo> removeRestrictedFolders(ReferenceData refData, HttpSession session, List<Photo> photoList) {
		if (photoList != null && refData != null) {
			List<Photo> rawPhotoList = new ArrayList<Photo>();
			rawPhotoList.addAll(photoList);
			
			if (StringUtils.isBlank(refData.getRefValue())) {
				return photoList;
			}
			
			for (Photo photo: photoList) {
				if (refData.getParsePipeRefValue().size() >= 1) {
					for (String refValues: refData.getParsePipeRefValue()) {
						if (photo.getDirectoryName().contains(refValues)) {
							rawPhotoList.remove(photo);
						}	
					}	
				}
				else {
					if (photo.getDirectoryName().contains(refData.getRefValue())) {
						rawPhotoList.remove(photo);
					}
				}
			}	
			photoList.clear();
			return rawPhotoList;
		}
		return photoList;
	}
	
	
	protected boolean isPhotoAllowed( HttpSession session, String photoDirectory) {
		SiteUser siteUser = (SiteUser) session.getAttribute(SESSION_SITEUSER);
		ReferenceData restrictedFolderRef = referenceDataSvc.getRefData(REFTYPE_RESTRICTED, REFCODE_RESTRICTED_VIEW);
		boolean isAllowed = true;
		if (siteUser != null) {
			
			if (siteUser.getAuthAlbums().contains(AUTH_ALL_FOLDER)) {
				return isAllowed;
			}
			
			if (restrictedFolderRef != null && !StringUtils.isBlank(restrictedFolderRef.getRefValue())) {
				for (String restrictedFolder: restrictedFolderRef.getParsePipeRefValue()) {
					if (StringUtils.isBlank(siteUser.getAuthAlbums())) {
						if (restrictedFolder.contains(photoDirectory)) {
							isAllowed = false;
						}
					}
					else if (siteUser.getAuthAlbums().contains(photoDirectory) && photoDirectory.equals(restrictedFolder)) {
						isAllowed = true;
					}
					else if (restrictedFolder.equals(photoDirectory)) {
						isAllowed =  false;	
					}
				}
			}
			return isAllowed;
		}
		
		if (restrictedFolderRef != null && !StringUtils.isBlank(restrictedFolderRef.getRefValue())){
			if (restrictedFolderRef.getParsePipeRefValue().isEmpty()) {
				if (!restrictedFolderRef.getRefValue().contains("\\|")) {
					if (photoDirectory.equals(restrictedFolderRef.getRefValue())) {
						isAllowed = false;
					}
				}
			}
			
			for (String restrictedFolder: restrictedFolderRef.getParsePipeRefValue()) {
				if (photoDirectory.equals(restrictedFolder)) {
					isAllowed =  false;	
				}	
			}
		}

		return isAllowed;
	}
	
	protected List<PhotoUploadDirectory> filterUploadDirectory(HttpSession session, List<PhotoUploadDirectory> photoUploadDirectory) {
		SiteUser siteUser = (SiteUser) session.getAttribute(SESSION_SITEUSER);
		if (siteUser == null) {
			return new ArrayList<PhotoUploadDirectory>();
		}
		else if (photoUploadDirectory == null || photoUploadDirectory.size() == 0) {
			return new ArrayList<PhotoUploadDirectory>();
		}
		else if (StringUtils.isBlank(siteUser.getAuthUploads())) {
			return new ArrayList<PhotoUploadDirectory>();
		}
		else if (siteUser.getAuthUploads().contains(AUTH_ALL_FOLDER)) {
			return photoUploadDirectory;
		}
		
		List<PhotoUploadDirectory> filteredList = new ArrayList<PhotoUploadDirectory>();
		ArrayList<String> authUploads = new ArrayList<String>();
		
		if (!siteUser.getAuthUploads().contains(",")) {
			authUploads.add(siteUser.getAuthUploads());
		}
		else {
			String[] authUploadsArray = siteUser.getAuthUploads().split(",");
			for (String auth: authUploadsArray) {
				authUploads.add(auth);
			}
			
		}
		
		for (PhotoUploadDirectory pud: photoUploadDirectory) {
			for (String au: authUploads) {
				if (au.equals(Long.toString(pud.getUploadId()))) {
					filteredList.add(pud);
				}
			}
		}
		photoUploadDirectory.clear();
		return filteredList;
	}
	
	protected void checkAuthUpload(HttpServletRequest request, HttpSession session, Long photoUploadId) {
		SiteUser siteUser = (SiteUser) session.getAttribute(SESSION_SITEUSER);
		if (siteUser.getAuthUploads().contains(AUTH_ALL_FOLDER)) {
			return;
		}
		
		PhotoUploadDirectory pud = photoUploadDirSvc.getPhotoUploadDir(photoUploadId);
		if (siteUser.getAuthUploads().contains(Long.toString(pud.getUploadId()))) {
			return;
		}
		throw new UserNotAuthorizedException();
		
	}
	
	protected void checkLoginSession(HttpSession session, HttpServletRequest request, JsonData container){
		if (session.getAttribute(SESSION_SITEUSER) != null) {
			container.put(SITEUSER_ISLOGIN, true);
			container.put(SESSION_SITEUSER, (SiteUser)session.getAttribute(SESSION_SITEUSER));
		}
		else {
			container.put(SITEUSER_ISLOGIN, false);
		}
	}
	
	protected void checkLogin(HttpSession session, HttpServletRequest request){
		if (session.getAttribute(SESSION_SITEUSER) != null) {
			return;
		}
		throw new UserNotAuthorizedException();
	}
	
	protected void checkIsAdmin(HttpSession session, HttpServletRequest request) {
		if (session.getAttribute(SESSION_SITEUSER) != null) {
			SiteUser siteUser = (SiteUser) session.getAttribute(SESSION_SITEUSER);
			if (siteUser.isAdmin()) {
				return;	
			}
		}
		throw new UserNotAuthorizedException();
	}
	
	public JsonView unauthorizedUser(){
		JsonContainer container = new JsonContainer();
		container.put(SITEUSER_ISLOGIN, false);
		return new JsonView(container);
	}
	
	
	@ExceptionHandler(UserNotAuthorizedException.class)
	public void handlerUnauthorizedLocation(HttpServletResponse response) throws IOException{
		response.setHeader(HTTPHEADER_ERRMSG, MSG_USERNOTAUTHORIZED);
		response.sendError(HttpServletResponse.SC_NOT_FOUND, MSG_USERNOTAUTHORIZED);
	}

	public void setReferenceDataSvc(ReferenceDataSvc referenceDataSvc) {
		this.referenceDataSvc = referenceDataSvc;
	}
	public void setPhotoUploadDirSvc(PhotoUploadDirSvc photoUploadDirSvc) {
		this.photoUploadDirSvc = photoUploadDirSvc;
	}
}
