package com.md.studio.web.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DownloadController {
	private String filePathDirectory;

	@RequestMapping(value="/downloadFile.jctl", method=RequestMethod.GET)
	public void processDownloadFile(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value="fileName", required=false) String fileName) throws IOException {
		
		if (StringUtils.isBlank(fileName)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File Not found");
		}
		
		File fileToDownload = new File(filePathDirectory + "/" + fileName);
		
		if (fileToDownload.exists() & fileToDownload.isFile()) {
			FileInputStream inStream = new FileInputStream(fileToDownload);
			response.addHeader("Content-Disposition", "attachment;filename=" + fileName);	
			response.setStatus(HttpServletResponse.SC_OK);
			
			BufferedOutputStream bout = new BufferedOutputStream(response.getOutputStream());
			int i;
			
			while ((i = inStream.read()) >= 0) {
				bout.write(i);
			}
			bout.flush();
			bout.close();
			
		}
		else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File Not found");
		}
	}

	public void setFilePathDirectory(String filePathDirectory) {
		this.filePathDirectory = filePathDirectory;
	}
}
