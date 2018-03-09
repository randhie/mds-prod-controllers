package com.md.studio.web.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.md.studio.domain.PhotoInfo;
import com.md.studio.domain.SiteUserActivityLog;
import com.md.studio.json.JsonContainer;
import com.md.studio.json.JsonErrorView;
import com.md.studio.json.JsonView;
import com.md.studio.service.PhotoGathererSvc;
import com.md.studio.service.SiteUserActivityLogSvc;

@Controller
public class PhotoProxyController extends AbstractController {
	private static final String SLASH = "/";
	private static final String PERM = "permType";
	private static final String PERMSPECIAL = "permSpecialType";
	private static final String SLIDE = "slideType";
	private static final String PREVIEW = "previewType";
	private static final String ADMINMISC = "adminMiscType";
	private static final String PORTFOLIO = "portfolioType";
	private static final String EVENTINFOBANNER = "eventInfoBannerType";
	private static final String CALENDAR = "calendarType";
	private static final String C_UNDERSCORE = "_";
	private static final String JPG_EXTFILE = "jpg";
    private static final String CHAR_ENCODING = ";charset=\"UTF-8\"";

	
	private Map<String, String> locationMap;
	private Map<String, File> fileMap = new HashMap<String, File>();
	private Map<String, byte[]> fileByteMap = new HashMap<String, byte[]>();
	
	private PhotoGathererSvc photoGathererSvc;
	private SiteUserActivityLogSvc siteUserActivityLogSvc;
	
	@RequestMapping(value="/viewPhoto.pics")
	public void viewPhoto(HttpSession session, HttpServletRequest request, 
			HttpServletResponse response,
			@RequestParam(value="directoryType", required=false) String directoryType,
			@RequestParam(value="fileName", required=false) String fileName,
			@RequestParam(value="isThumbnail", required=false, defaultValue="false") String isThumbnail,
			@RequestParam(value="photoId", required=false) String photoId) throws IOException{
	
		if (StringUtils.isBlank(fileName)) {
			response.sendError(HttpStatus.SC_NO_CONTENT, "File name missing");
		}
	
		String[] parseFileName = fileName.trim().split("/"); // Nature/IMG_0663.jpg
		if (parseFileName.length>=3 && !isPhotoAllowed(session, parseFileName[1].trim())) {
			response.sendError(HttpStatus.SC_UNAUTHORIZED, "Unauthorized Access. Please contact admin to access.");
		}
	
		if (StringUtils.isNotBlank(photoId)) {
			
			SiteUserActivityLog log = new SiteUserActivityLog();
			log.setBrowserInfo(getBrowser(session, request));
			log.setIpAddress(getIpAddress(session, request));
			log.setPhotoId(Long.valueOf(photoId));
			log.setUrlAccess(request.getQueryString());
			
			siteUserActivityLogSvc.recordActivityLog(log);
			
			byte[] fileByte;
			ServletOutputStream out = response.getOutputStream();
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG + CHAR_ENCODING );
			response.setStatus(HttpServletResponse.SC_OK);
			
			if (fileByteMap.containsKey(photoId + C_UNDERSCORE + isThumbnail)) {
				fileByte = fileByteMap.get(photoId + C_UNDERSCORE + isThumbnail);
				out.write(fileByte);
			}
			else {
				PhotoInfo photoInfo = photoGathererSvc.getPhoto(Long.valueOf(photoId));
				if (Boolean.parseBoolean(isThumbnail)) {
					out.write(photoInfo.getThumbnailBytes());
					fileByteMap.put(photoId + C_UNDERSCORE + isThumbnail, photoInfo.getThumbnailBytes());
				}
				else {
					out.write(photoInfo.getFileBytes());
					fileByteMap.put(photoId + C_UNDERSCORE + isThumbnail, photoInfo.getFileBytes());
				}
				
				
			}
			
			return;
		}
		
		String keyMap = directoryType + C_UNDERSCORE + fileName ;
		if (!fileMap.isEmpty() &&
				fileMap.containsKey(keyMap)) {
			renderedImage(fileMap.get(keyMap), keyMap, response);
			return;
		}
		
		File file = null;
		if (directoryType.contains(PERM)) {
			file = new File(locationMap.get(PERM) + SLASH + fileName);
			
			if (!file.exists()) {
				file = new File(locationMap.get(PERMSPECIAL) + SLASH + fileName);
			}
		}
		else if (directoryType.contains(SLIDE)){
			file = new File(locationMap.get(SLIDE) + SLASH + fileName);
		}
		else if (directoryType.contains(PREVIEW)) {
			file = new File(locationMap.get(PREVIEW) + SLASH + fileName);
		}
		else if (directoryType.contains(ADMINMISC)) {
			file = new File(locationMap.get(ADMINMISC) + SLASH + fileName);
		}
		else if (directoryType.contains(PORTFOLIO)) {
			file = new File(locationMap.get(PORTFOLIO) + SLASH + fileName);
		}
		else if (directoryType.contains(EVENTINFOBANNER)) {
			file = new File(locationMap.get(EVENTINFOBANNER) + SLASH + fileName);
		}
		else if (directoryType.contains(CALENDAR)) {
			file = new File(locationMap.get(CALENDAR) + SLASH + fileName);
		}
		
		if (file == null || !file.exists()) {
			response.sendError(HttpStatus.SC_NOT_FOUND, "File not found");
			return;
		}
		
		fileMap.put(keyMap, file);
		renderedImage(file, keyMap, response);
		
	}
	
	@RequestMapping(value="/refreshMapPhoto.jctl", method=RequestMethod.POST)
	public JsonView refreshFileMap(HttpSession session, HttpServletRequest request) {
		JsonContainer container = new JsonContainer();
		checkLoginSession(session, request, container);
		
		fileMap.clear();
		if (fileMap.size() == 0) {
			return new JsonView(container);
		}
		else {
			return new JsonErrorView();
		}
	}
	
	private void renderedImage(File file, String MapKey, HttpServletResponse response)  {
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(file);
			
			BufferedImage bufferedImage = ImageIO.read(fileInputStream);
			OutputStream outputStream = response.getOutputStream();
			response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG + CHAR_ENCODING );
			response.setStatus(HttpServletResponse.SC_OK);
			
			ImageIO.write(bufferedImage, JPG_EXTFILE, outputStream);
			outputStream.flush();
			outputStream.close();	
			
			fileInputStream.close();
			bufferedImage.flush();
			file = null;
			
		} catch (FileNotFoundException e1) {
			fileMap.remove(MapKey);
		}
		catch (Exception e) {
			fileMap.clear();
		}
	}

	public void setLocationMap(Map<String, String> locationMap) {
		this.locationMap = locationMap;
	}

	public void setPhotoGathererSvc(PhotoGathererSvc photoGathererSvc) {
		this.photoGathererSvc = photoGathererSvc;
	}
	public void setSiteUserActivityLogSvc(
			SiteUserActivityLogSvc siteUserActivityLogSvc) {
		this.siteUserActivityLogSvc = siteUserActivityLogSvc;
	}
}
